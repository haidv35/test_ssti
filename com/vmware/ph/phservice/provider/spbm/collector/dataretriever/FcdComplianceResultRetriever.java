package com.vmware.ph.phservice.provider.spbm.collector.dataretriever;

import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.util.DatastoreReader;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import com.vmware.ph.phservice.provider.spbm.collector.util.SpbmUtil;
import com.vmware.vim.binding.pbm.ServerObjectRef;
import com.vmware.vim.binding.pbm.compliance.ComplianceManager;
import com.vmware.vim.binding.pbm.compliance.ComplianceResult;
import com.vmware.vim.binding.vim.fault.InvalidDatastore;
import com.vmware.vim.binding.vim.vslm.ID;
import com.vmware.vim.binding.vim.vslm.vcenter.VStorageObjectManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.core.impl.BlockingFuture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FcdComplianceResultRetriever implements DataRetriever<ComplianceResult> {
  private static final Log log = LogFactory.getLog(FcdComplianceResultRetriever.class);
  
  private final VcClient _vcClient;
  
  private final PbmServiceClient _pbmClient;
  
  ComplianceManager complianceManager = null;
  
  private final int _queryOffset;
  
  private final int _queryLimit;
  
  public FcdComplianceResultRetriever(SpbmCollectorContext collectorContext, int queryOffset, int queryLimit) {
    this._pbmClient = collectorContext.getPbmServiceClient();
    this._vcClient = this._pbmClient.getVcClient();
    this._queryOffset = queryOffset;
    this._queryLimit = queryLimit;
  }
  
  public List<ComplianceResult> retrieveData() {
    List<ComplianceResult> complianceResults = new LinkedList<>();
    try {
      this.complianceManager = this._pbmClient.<ComplianceManager>createStub(this._pbmClient.getServiceInstanceContent().getComplianceManager());
    } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
      log.warn("Error occured while creating complianceManager stub", e);
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    if (this.complianceManager != null) {
      List<ID> fcdIds = getSortedFcdIds();
      fcdIds = PageUtil.pageItems(fcdIds, this._queryOffset, this._queryLimit);
      complianceResults.addAll(getComplianceResults(fcdIds));
    } 
    return complianceResults;
  }
  
  private List<ID> getSortedFcdIds() {
    List<ID> fcdIdList = new LinkedList<>();
    VStorageObjectManager vStorageObjectManager = (VStorageObjectManager)this._vcClient.createMo(this._vcClient.getServiceInstanceContent().getVStorageObjectManager());
    List<ManagedObjectReference> datastoreMoRefs = DatastoreReader.getDatastoreMoRefs(this._vcClient);
    for (ManagedObjectReference dataStoreMoRef : datastoreMoRefs) {
      try {
        ID[] fcdIds = vStorageObjectManager.listVStorageObject(dataStoreMoRef);
        if (fcdIds != null)
          fcdIdList.addAll(Arrays.asList(fcdIds)); 
      } catch (InvalidDatastore e) {
        log.warn("Error occurred while retrieving VStorageObjects", (Throwable)e);
      } 
    } 
    Collections.sort(fcdIdList, new Comparator<ID>() {
          public int compare(ID o1, ID o2) {
            return o1.getId().compareTo(o2.getId());
          }
        });
    return fcdIdList;
  }
  
  private List<ComplianceResult> getComplianceResults(List<ID> fcdIds) {
    List<ComplianceResult> complianceResults = new LinkedList<>();
    String vcUuid = this._pbmClient.getVcClient().getServiceInstanceContent().getAbout().getInstanceUuid();
    ServerObjectRef[] fcdSoRefs = SpbmUtil.createSoRefsForFcds(vcUuid, fcdIds.<ID>toArray(new ID[fcdIds.size()]));
    try {
      BlockingFuture blockingFuture = new BlockingFuture();
      this.complianceManager.fetchComplianceResult(fcdSoRefs, null, (Future)blockingFuture);
      ComplianceResult[] complianceResultArray = (ComplianceResult[])blockingFuture.get();
      if (complianceResultArray != null) {
        if (log.isDebugEnabled())
          log.debug("Retrieved ComplianceResult."); 
        complianceResults.addAll(Arrays.asList(complianceResultArray));
        List<String> fcdIdsWithNoComplianceResult = getFcdSoRefsNotInComplianceResultList(fcdIds, complianceResults);
        List<ComplianceResult> dummyComplianceResults = createDummyComplianceResultsForFcds(vcUuid, fcdIdsWithNoComplianceResult);
        complianceResults.addAll(dummyComplianceResults);
      } 
    } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
      log.warn("Error occured while retrieving ComplianceResult", e);
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return complianceResults;
  }
  
  private List<String> getFcdSoRefsNotInComplianceResultList(List<ID> fcdIds, List<ComplianceResult> complianceResults) {
    List<String> ids = new ArrayList<>(fcdIds.size());
    for (ID fcdId : fcdIds)
      ids.add(fcdId.getId()); 
    if (!complianceResults.isEmpty())
      if (complianceResults.size() != fcdIds.size()) {
        for (ComplianceResult result : complianceResults)
          ids.remove(result.getEntity().getKey()); 
      } else {
        ids = Collections.emptyList();
      }  
    return ids;
  }
  
  private List<ComplianceResult> createDummyComplianceResultsForFcds(String vcUuid, List<String> fcdIds) {
    List<ComplianceResult> complianceResultsWithNullComplianceStatus = Collections.emptyList();
    if (!fcdIds.isEmpty()) {
      List<ServerObjectRef> fcdSoRefs = SpbmUtil.createSoRefsForFcds(vcUuid, fcdIds);
      complianceResultsWithNullComplianceStatus = SpbmUtil.createComplianceResultWithNullComplianceStatus(fcdSoRefs);
    } 
    return complianceResultsWithNullComplianceStatus;
  }
  
  public String getKey(ComplianceResult t) {
    return t.getEntity().getKey();
  }
}

package com.vmware.ph.phservice.provider.spbm.collector.dataretriever;

import com.vmware.ph.phservice.common.vim.vc.util.VirtualMachineReader;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import com.vmware.ph.phservice.provider.spbm.collector.util.SpbmUtil;
import com.vmware.vim.binding.pbm.ServerObjectRef;
import com.vmware.vim.binding.pbm.compliance.ComplianceManager;
import com.vmware.vim.binding.pbm.compliance.RollupComplianceResult;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.core.impl.BlockingFuture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RollupComplianceResultRetriever implements DataRetriever<RollupComplianceResult> {
  private static final Log log = LogFactory.getLog(RollupComplianceResultRetriever.class);
  
  private final PbmServiceClient _pbmClient;
  
  private final int _queryOffset;
  
  private final int _queryLimit;
  
  public RollupComplianceResultRetriever(SpbmCollectorContext collectorContext, int queryOffest, int queryLimit) {
    this._pbmClient = collectorContext.getPbmServiceClient();
    this._queryOffset = queryOffest;
    this._queryLimit = queryLimit;
  }
  
  public List<RollupComplianceResult> retrieveData() {
    List<ManagedObjectReference> vmMoRefs = VirtualMachineReader.getVmMoRefs(this._pbmClient.getVcClient(), this._queryOffset, this._queryLimit);
    List<RollupComplianceResult> rollupComplianceResults = new ArrayList<>(vmMoRefs.size());
    String vcUuid = this._pbmClient.getVcClient().getServiceInstanceContent().getAbout().getInstanceUuid();
    if (!vmMoRefs.isEmpty()) {
      ServerObjectRef[] serverObjectRefs = SpbmUtil.createSoRefsForVms(vmMoRefs);
      rollupComplianceResults.addAll(getRollupComplianceResult(serverObjectRefs));
      List<String> vmIdsWithNoComplianceResult = getVmIdsNotInRollupComplianceResultList(vmMoRefs, rollupComplianceResults);
      List<RollupComplianceResult> dummyRollupComplianceResults = createDummyComplianceResultsForVms(vcUuid, vmIdsWithNoComplianceResult);
      rollupComplianceResults.addAll(dummyRollupComplianceResults);
    } else {
      log.warn("No VMs in the setup!");
    } 
    return rollupComplianceResults;
  }
  
  private List<RollupComplianceResult> getRollupComplianceResult(ServerObjectRef[] entities) {
    List<RollupComplianceResult> rollupcomplianceResultList = Collections.emptyList();
    try {
      ComplianceManager complianceManager = this._pbmClient.<ComplianceManager>createStub(this._pbmClient.getServiceInstanceContent().getComplianceManager());
      BlockingFuture blockingFuture = new BlockingFuture();
      complianceManager.fetchRollupComplianceResult(entities, (Future)blockingFuture);
      RollupComplianceResult[] rollupComplianceResult = (RollupComplianceResult[])blockingFuture.get();
      if (rollupComplianceResult != null) {
        if (log.isDebugEnabled())
          log.debug("Retrieved ComplianceResult."); 
        rollupcomplianceResultList = Arrays.asList(rollupComplianceResult);
      } 
    } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
      log.warn("Error occured while retrieving ComplianceResult using ProfileManager.fetchComplianceResult() API", e);
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return rollupcomplianceResultList;
  }
  
  private List<String> getVmIdsNotInRollupComplianceResultList(List<ManagedObjectReference> vmMoRefs, List<RollupComplianceResult> rollupComplianceResult) {
    List<String> vmIds = new ArrayList<>(vmMoRefs.size());
    for (ManagedObjectReference vmMor : vmMoRefs)
      vmIds.add(vmMor.getValue()); 
    if (!rollupComplianceResult.isEmpty())
      if (rollupComplianceResult.size() != vmMoRefs.size()) {
        for (RollupComplianceResult result : rollupComplianceResult)
          vmIds.remove(result.getEntity().getKey()); 
      } else {
        vmIds = Collections.emptyList();
      }  
    return vmIds;
  }
  
  private List<RollupComplianceResult> createDummyComplianceResultsForVms(String vcUuid, List<String> vmIds) {
    List<RollupComplianceResult> rollupComplianceResultsWithNullComplianceResult = Collections.emptyList();
    if (!vmIds.isEmpty()) {
      List<ServerObjectRef> vmSorList = SpbmUtil.createSorListForVm(vcUuid, vmIds);
      rollupComplianceResultsWithNullComplianceResult = SpbmUtil.createRollupComplianceResultWithNullComplianceResult(vmSorList);
    } 
    return rollupComplianceResultsWithNullComplianceResult;
  }
  
  public String getKey(RollupComplianceResult t) {
    return t.getEntity().getKey();
  }
}

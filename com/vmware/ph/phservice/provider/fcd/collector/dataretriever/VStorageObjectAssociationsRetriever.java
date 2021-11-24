package com.vmware.ph.phservice.provider.fcd.collector.dataretriever;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.vim.binding.vim.vslm.ID;
import com.vmware.vim.binding.vim.vslm.vcenter.RetrieveVStorageObjSpec;
import com.vmware.vim.binding.vim.vslm.vcenter.VStorageObjectAssociations;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeMap;

public class VStorageObjectAssociationsRetriever implements DataRetriever<VStorageObjectAssociations> {
  private final VcClient _vcClient;
  
  private final int _queryOffset;
  
  private final int _queryLimit;
  
  public VStorageObjectAssociationsRetriever(VcClient vcClient, int queryOffset, int queryLimit) {
    this._vcClient = vcClient;
    this._queryOffset = queryOffset;
    this._queryLimit = queryLimit;
  }
  
  public List<VStorageObjectAssociations> retrieveData() {
    FcdDataRetrieverHelper fcdDataRetrieverHelper = new FcdDataRetrieverHelper(this._vcClient);
    TreeMap<ID, ManagedObjectReference> fcdIdToDatastoreMap = fcdDataRetrieverHelper.getSortedFcdIdToDatastoreMap();
    int startInd = this._queryOffset;
    int lastInd = Math.min(this._queryOffset + this._queryLimit, fcdIdToDatastoreMap.keySet().size()) - 1;
    RetrieveVStorageObjSpec[] retrieveVStorageObjSpecs = new RetrieveVStorageObjSpec[lastInd - startInd + 1];
    NavigableSet<ID> fcdIds = fcdIdToDatastoreMap.navigableKeySet();
    int curIndex = 0;
    for (ID id : fcdIds) {
      if (curIndex > lastInd)
        break; 
      if (curIndex >= startInd) {
        RetrieveVStorageObjSpec retrieveVStorageObjSpec = new RetrieveVStorageObjSpec();
        retrieveVStorageObjSpec.setDatastore(fcdIdToDatastoreMap.get(id));
        retrieveVStorageObjSpec.setId(id);
        retrieveVStorageObjSpecs[curIndex - startInd] = retrieveVStorageObjSpec;
      } 
      curIndex++;
    } 
    VStorageObjectAssociations[] vStorageObjectAssociations = fcdDataRetrieverHelper.getVStorageObjectManager().retrieveVStorageObjectAssociations(retrieveVStorageObjSpecs);
    return (vStorageObjectAssociations != null) ? Arrays.<VStorageObjectAssociations>asList(vStorageObjectAssociations) : Collections.<VStorageObjectAssociations>emptyList();
  }
  
  public String getKey(VStorageObjectAssociations t) {
    return t.getId().getId();
  }
}

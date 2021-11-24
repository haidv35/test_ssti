package com.vmware.ph.phservice.provider.fcd.collector.dataretriever;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.vim.binding.vim.fault.FileFault;
import com.vmware.vim.binding.vim.fault.VimFault;
import com.vmware.vim.binding.vim.vslm.ID;
import com.vmware.vim.binding.vim.vslm.VStorageObject;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VStorageObjectRetriever implements DataRetriever<VStorageObject> {
  private static final Log _log = LogFactory.getLog(VStorageObjectRetriever.class);
  
  private final VcClient _vcClient;
  
  private final int _queryOffset;
  
  private final int _queryLimit;
  
  public VStorageObjectRetriever(VcClient vcClient, int queryOffset, int queryLimit) {
    this._vcClient = vcClient;
    this._queryOffset = queryOffset;
    this._queryLimit = queryLimit;
  }
  
  public List<VStorageObject> retrieveData() {
    FcdDataRetrieverHelper fcdDataRetrieverHelper = new FcdDataRetrieverHelper(this._vcClient);
    TreeMap<ID, ManagedObjectReference> fcdIdToDatastoreMap = fcdDataRetrieverHelper.getSortedFcdIdToDatastoreMap();
    int startInd = this._queryOffset;
    int lastInd = Math.min(this._queryOffset + this._queryLimit, fcdIdToDatastoreMap.keySet().size()) - 1;
    List<VStorageObject> vStorageObjectList = new ArrayList<>(lastInd - startInd + 1);
    NavigableSet<ID> fcdIds = fcdIdToDatastoreMap.navigableKeySet();
    int curIndex = 0;
    for (ID id : fcdIds) {
      if (curIndex > lastInd)
        break; 
      if (curIndex >= startInd)
        try {
          vStorageObjectList.add(fcdDataRetrieverHelper.getVStorageObjectManager().retrieveVStorageObject(id, fcdIdToDatastoreMap
                .get(id)));
        } catch (FileFault|com.vmware.vim.binding.vim.fault.InvalidDatastore|com.vmware.vim.binding.vim.fault.NotFound e) {
          if (_log.isDebugEnabled())
            _log.debug("Error occurred while retrieving VStorageObjects", (Throwable)e); 
        }  
      curIndex++;
    } 
    return vStorageObjectList;
  }
  
  public String getKey(VStorageObject t) {
    return t.getConfig().getId().getId();
  }
}

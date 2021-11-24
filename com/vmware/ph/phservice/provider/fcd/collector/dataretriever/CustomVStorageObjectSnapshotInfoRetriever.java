package com.vmware.ph.phservice.provider.fcd.collector.dataretriever;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.CopyUtil;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.fcd.collector.customobject.CustomVStorageObjectSnapshotInfo;
import com.vmware.vim.binding.vim.fault.InvalidDatastore;
import com.vmware.vim.binding.vim.fault.VimFault;
import com.vmware.vim.binding.vim.vslm.ID;
import com.vmware.vim.binding.vim.vslm.VStorageObjectSnapshotInfo;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CustomVStorageObjectSnapshotInfoRetriever implements DataRetriever<CustomVStorageObjectSnapshotInfo> {
  private static final Log log = LogFactory.getLog(CustomVStorageObjectSnapshotInfoRetriever.class);
  
  private final VcClient _vcClient;
  
  private final int _queryOffset;
  
  private final int _queryLimit;
  
  public CustomVStorageObjectSnapshotInfoRetriever(VcClient vcClient, int queryOffset, int queryLimit) {
    this._vcClient = vcClient;
    this._queryOffset = queryOffset;
    this._queryLimit = queryLimit;
  }
  
  public List<CustomVStorageObjectSnapshotInfo> retrieveData() {
    FcdDataRetrieverHelper fcdDataRetrieverHelper = new FcdDataRetrieverHelper(this._vcClient);
    TreeMap<ID, ManagedObjectReference> fcdIdToDatastoreMap = fcdDataRetrieverHelper.getSortedFcdIdToDatastoreMap();
    int startInd = this._queryOffset;
    int lastInd = Math.min(this._queryOffset + this._queryLimit, fcdIdToDatastoreMap.keySet().size()) - 1;
    List<CustomVStorageObjectSnapshotInfo> customVsoSnapshotInfoList = new ArrayList<>(lastInd - startInd + 1);
    NavigableSet<ID> fcdIds = fcdIdToDatastoreMap.navigableKeySet();
    int curIndex = 0;
    for (ID id : fcdIds) {
      if (curIndex > lastInd)
        break; 
      if (curIndex >= startInd)
        try {
          VStorageObjectSnapshotInfo snapshotInfo = fcdDataRetrieverHelper.getVStorageObjectManager().retrieveSnapshotInfo(id, fcdIdToDatastoreMap
              .get(id));
          CustomVStorageObjectSnapshotInfo customVsoSnapshotInfo = new CustomVStorageObjectSnapshotInfo();
          CopyUtil.copyPublicFields(snapshotInfo, customVsoSnapshotInfo);
          customVsoSnapshotInfo.setFcdId(id);
          customVsoSnapshotInfoList.add(customVsoSnapshotInfo);
        } catch (InvalidDatastore|com.vmware.vim.binding.vim.fault.NotFound|com.vmware.vim.binding.vim.fault.FileFault|com.vmware.vim.binding.vim.fault.InvalidState e) {
          log.warn("Error occurred while retrieving VStorageObjectSnapshotInfo", (Throwable)e);
        }  
      curIndex++;
    } 
    return customVsoSnapshotInfoList;
  }
  
  public String getKey(CustomVStorageObjectSnapshotInfo t) {
    return t.getFcdId().getId();
  }
}

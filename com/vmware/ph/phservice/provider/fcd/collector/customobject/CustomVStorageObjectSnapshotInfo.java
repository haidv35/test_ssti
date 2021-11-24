package com.vmware.ph.phservice.provider.fcd.collector.customobject;

import com.vmware.vim.binding.vim.vslm.ID;
import com.vmware.vim.binding.vim.vslm.VStorageObjectSnapshotInfo;

public class CustomVStorageObjectSnapshotInfo extends VStorageObjectSnapshotInfo {
  private static final long serialVersionUID = 1L;
  
  public ID fcdId;
  
  public ID getFcdId() {
    return this.fcdId;
  }
  
  public void setFcdId(ID fcdId) {
    this.fcdId = fcdId;
  }
}

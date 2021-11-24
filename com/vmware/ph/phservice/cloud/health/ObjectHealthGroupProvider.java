package com.vmware.ph.phservice.cloud.health;

import com.vmware.ph.phservice.cloud.dataapp.internal.ProgressReporter;
import com.vmware.ph.phservice.common.Pair;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthGroup;
import java.util.Date;
import java.util.List;

public interface ObjectHealthGroupProvider {
  Pair<VsanClusterHealthGroup, Date> getObjectHealthGroupAndTimestamp(ManagedObjectReference paramManagedObjectReference, boolean paramBoolean, String paramString, ProgressReporter paramProgressReporter);
  
  List<VsanClusterHealthGroup> getCachedHealthGroups(String paramString);
}

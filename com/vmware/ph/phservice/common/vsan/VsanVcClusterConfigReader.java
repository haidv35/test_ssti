package com.vmware.ph.phservice.common.vsan;

import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterMetroConfig;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterMgmtDebugSystem;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterPersistedState;
import com.vmware.vim.vsan.binding.vim.cluster.VsanVcClusterConfigSystem;
import com.vmware.vim.vsan.binding.vim.vsan.ConfigInfoEx;
import com.vmware.vim.vsan.binding.vim.vsan.DataEfficiencyConfig;

public class VsanVcClusterConfigReader {
  public static final ManagedObjectReference VSAN_CLUSTER_CONFIG_SYSTEM_MO_REF = new ManagedObjectReference("VsanVcClusterConfigSystem", "vsan-cluster-config-system");
  
  public static final ManagedObjectReference VSAN_CLUSTER_MGMT_DEBUG_SYSTEM_MO_REF = new ManagedObjectReference("VsanClusterMgmtDebugSystem", "vsan-cluster-mgmt-debug-system");
  
  private VmomiClient _vmomiClient;
  
  public VsanVcClusterConfigReader(VmomiClient vmomiClient) {
    this._vmomiClient = vmomiClient;
  }
  
  public boolean isDedupEnabled(ManagedObjectReference clusterMoRef) throws Exception {
    boolean isDedupEnabled = false;
    VsanVcClusterConfigSystem vsanVcClusterConfigSystem = this._vmomiClient.<VsanVcClusterConfigSystem>createStub(VSAN_CLUSTER_CONFIG_SYSTEM_MO_REF);
    ConfigInfoEx clusterConfigInfoEx = vsanVcClusterConfigSystem.getConfigInfoEx(clusterMoRef);
    if (clusterConfigInfoEx != null) {
      DataEfficiencyConfig dataEfficiencyConfig = clusterConfigInfoEx.getDataEfficiencyConfig();
      if (dataEfficiencyConfig != null)
        isDedupEnabled = dataEfficiencyConfig.isDedupEnabled(); 
    } 
    return isDedupEnabled;
  }
  
  public String getWitnessNodeUuid(ManagedObjectReference clusterMoRef) throws Exception {
    String witnessUuid = null;
    VsanClusterMgmtDebugSystem vsanClusterMgmtDebugSystem = this._vmomiClient.<VsanClusterMgmtDebugSystem>createStub(VSAN_CLUSTER_MGMT_DEBUG_SYSTEM_MO_REF);
    VsanClusterPersistedState vsanClusterPersistedState = vsanClusterMgmtDebugSystem.getPersistedClusterState(clusterMoRef);
    if (vsanClusterPersistedState != null) {
      VsanClusterMetroConfig metroConfig = vsanClusterPersistedState.getMetroConfig();
      if (metroConfig != null)
        witnessUuid = metroConfig.getWitnessNodeUuid(); 
    } 
    return witnessUuid;
  }
}

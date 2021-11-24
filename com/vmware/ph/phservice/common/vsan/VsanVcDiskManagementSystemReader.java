package com.vmware.ph.phservice.common.vsan;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.vim.binding.vim.ClusterComputeResource;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vsan.binding.vim.cluster.VsanVcDiskManagementSystem;
import com.vmware.vim.vsan.binding.vim.vsan.host.DiskMapInfoEx;
import com.vmware.vim.vsan.binding.vim.vsan.host.VsanHostCapability;

public class VsanVcDiskManagementSystemReader {
  public static final ManagedObjectReference VSAN_VC_DISK_MO_REF = new ManagedObjectReference("VimClusterVsanVcDiskManagementSystem", "vsan-disk-management-system");
  
  private final VmomiClient _vsanHealthVmomiClient;
  
  private final VcClient _vcClient;
  
  public VsanVcDiskManagementSystemReader(VmomiClient vsanHealthVmomiClient, VcClient vcClient) {
    this._vsanHealthVmomiClient = vsanHealthVmomiClient;
    this._vcClient = vcClient;
  }
  
  public boolean isAllFlashCluster(ManagedObjectReference clusterMoRef) throws Exception {
    VsanVcDiskManagementSystem vsanDiskManagementSystem = this._vsanHealthVmomiClient.<VsanVcDiskManagementSystem>createStub(VSAN_VC_DISK_MO_REF);
    VsanHostCapability[] hostCapabilities = vsanDiskManagementSystem.retrieveAllFlashCapabilities(clusterMoRef);
    boolean isAllFlash = false;
    for (VsanHostCapability hostCapability : hostCapabilities) {
      if (!hostCapability.isSupported || !hostCapability.isLicensed)
        return false; 
    } 
    ClusterComputeResource cluster = this._vcClient.<ClusterComputeResource>createMo(clusterMoRef);
    ManagedObjectReference[] hostMoRefs = cluster.getHost();
    for (ManagedObjectReference hostMoRef : hostMoRefs) {
      DiskMapInfoEx[] diskMapInfos = vsanDiskManagementSystem.queryDiskMappings(hostMoRef);
      if (diskMapInfos != null)
        for (DiskMapInfoEx diskMapInfoEx : diskMapInfos) {
          if (diskMapInfoEx.isMounted) {
            if (!diskMapInfoEx.isAllFlash)
              return false; 
            isAllFlash = true;
          } 
        }  
    } 
    return isAllFlash;
  }
}

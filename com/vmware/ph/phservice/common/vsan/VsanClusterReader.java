package com.vmware.ph.phservice.common.vsan;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.vim.binding.vim.ClusterComputeResource;
import com.vmware.vim.binding.vim.ComputeResource;
import com.vmware.vim.binding.vim.cluster.ConfigInfoEx;
import com.vmware.vim.binding.vim.vsan.cluster.ConfigInfo;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vsan.binding.vim.cluster.VSANWitnessHostInfo;
import com.vmware.vim.vsan.binding.vim.cluster.VsanCapability;
import com.vmware.vim.vsan.binding.vim.cluster.VsanCapabilitySystem;
import com.vmware.vim.vsan.binding.vim.cluster.VsanVcStretchedClusterSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class VsanClusterReader {
  public static final ManagedObjectReference VSAN_CAPABILITY_SYSTEM_MO_REF = new ManagedObjectReference("VsanCapabilitySystem", "vsan-vc-capability-system");
  
  public static final ManagedObjectReference VSAN_STRETCHED_CLUSTER_SYSTEM_MO_REF = new ManagedObjectReference("VimClusterVsanVcStretchedClusterSystem", "vsan-stretched-cluster-system");
  
  static final String CLUSTER_MO_REF_TYPE = "ClusterComputeResource";
  
  private final VmomiClient _vsanHealthClient;
  
  private VcClient _vcClient;
  
  public VsanClusterReader(VmomiClient vsanHealthClient, VcClient vcClient) {
    this._vsanHealthClient = vsanHealthClient;
    this._vcClient = vcClient;
  }
  
  public boolean isVsanEnabled(ManagedObjectReference clusterMoRef) {
    boolean isVsanEnabled = false;
    if (clusterMoRef != null && clusterMoRef.getType().equals("ClusterComputeResource")) {
      ClusterComputeResource clusterStub = this._vcClient.<ClusterComputeResource>createMo(clusterMoRef);
      ComputeResource.ConfigInfo configInfo = clusterStub.getConfigurationEx();
      if (configInfo instanceof ConfigInfoEx) {
        ConfigInfo vsanConfigInfo = ((ConfigInfoEx)configInfo).getVsanConfigInfo();
        if (vsanConfigInfo != null)
          isVsanEnabled = vsanConfigInfo.getEnabled().booleanValue(); 
      } 
    } 
    return isVsanEnabled;
  }
  
  public List<VsanCapability> getVsanClusterCapabilities(List<ManagedObjectReference> clusterMoRefs) throws Exception {
    Objects.requireNonNull(clusterMoRefs);
    if (clusterMoRefs.isEmpty())
      return Collections.emptyList(); 
    ManagedObjectReference[] clusterMoRefsArray = clusterMoRefs.<ManagedObjectReference>toArray(
        new ManagedObjectReference[clusterMoRefs.size()]);
    VsanCapabilitySystem vsanCapabilitySystem = this._vsanHealthClient.<VsanCapabilitySystem>createStub(VSAN_CAPABILITY_SYSTEM_MO_REF);
    VsanCapability[] vsanCapabilities = vsanCapabilitySystem.getCapabilities(clusterMoRefsArray);
    if (vsanCapabilities == null)
      return Collections.emptyList(); 
    List<VsanCapability> clusterCapabilities = new ArrayList<>();
    for (VsanCapability vsanCapability : vsanCapabilities) {
      boolean isClusterCapability = (vsanCapability.getTarget() != null && "ClusterComputeResource".equals(vsanCapability.getTarget().getType()));
      if (isClusterCapability)
        clusterCapabilities.add(vsanCapability); 
    } 
    return clusterCapabilities;
  }
  
  public List<ManagedObjectReference> getWitnessHosts(List<ManagedObjectReference> clusterMoRefs) throws Exception {
    Objects.requireNonNull(clusterMoRefs);
    if (clusterMoRefs.isEmpty())
      return Collections.emptyList(); 
    List<ManagedObjectReference> results = new ArrayList<>();
    for (ManagedObjectReference clusterMoRef : clusterMoRefs) {
      VSANWitnessHostInfo[] clusterWitnessHosts = getWitnessHosts(clusterMoRef);
      if (clusterWitnessHosts != null)
        for (VSANWitnessHostInfo witnessHost : clusterWitnessHosts) {
          ManagedObjectReference hostMoRef = witnessHost.getHost();
          results.add(hostMoRef);
        }  
    } 
    return results;
  }
  
  public VSANWitnessHostInfo[] getWitnessHosts(ManagedObjectReference clusterMoRef) throws Exception {
    VsanVcStretchedClusterSystem vsanVcStretchedClusterSystem = this._vsanHealthClient.<VsanVcStretchedClusterSystem>createStub(VSAN_STRETCHED_CLUSTER_SYSTEM_MO_REF);
    VSANWitnessHostInfo[] witnessHosts = vsanVcStretchedClusterSystem.getWitnessHosts(clusterMoRef);
    return witnessHosts;
  }
}

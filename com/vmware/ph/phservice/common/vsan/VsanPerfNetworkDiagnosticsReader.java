package com.vmware.ph.phservice.common.vsan;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.vim.binding.vim.fault.InvalidState;
import com.vmware.vim.binding.vim.fault.VsanFault;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vsan.binding.vim.cluster.VsanCapability;
import com.vmware.vim.vsan.binding.vim.cluster.VsanPerfsvcConfig;
import com.vmware.vim.vsan.binding.vim.cluster.VsanVcClusterConfigSystem;
import com.vmware.vim.vsan.binding.vim.vsan.ConfigInfoEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VsanPerfNetworkDiagnosticsReader {
  static final String CLUSTER_MO_REF_TYPE = "ClusterComputeResource";
  
  static final String DIAGNOSTIC_MODE_CAPABILITY = "diagnosticmode";
  
  private final VmomiClient _vsanHealthClient;
  
  private final VsanClusterReader _vsanClusterReader;
  
  public VsanPerfNetworkDiagnosticsReader(VmomiClient vsanHealthClient, VcClient vcClient) {
    this(vsanHealthClient, new VsanClusterReader(vsanHealthClient, vcClient));
  }
  
  VsanPerfNetworkDiagnosticsReader(VmomiClient vsanHealthClient, VsanClusterReader vsanClusterReader) {
    this._vsanHealthClient = vsanHealthClient;
    this._vsanClusterReader = vsanClusterReader;
  }
  
  public List<ManagedObjectReference> getNetworkDiagnosticsEnabledClusters(List<ManagedObjectReference> clusterMoRefs) throws Exception {
    List<VsanCapability> clusterCapabilities = this._vsanClusterReader.getVsanClusterCapabilities(clusterMoRefs);
    if (clusterCapabilities.isEmpty())
      return Collections.emptyList(); 
    VsanVcClusterConfigSystem vsanClusterConfigSystem = this._vsanHealthClient.<VsanVcClusterConfigSystem>createStub(VsanVcClusterConfigReader.VSAN_CLUSTER_CONFIG_SYSTEM_MO_REF);
    List<ManagedObjectReference> networkDiagnosticsEnabledClustersMoRefs = new ArrayList<>();
    for (VsanCapability clusterCapability : clusterCapabilities) {
      if (isNetworkDiagnosticModeEnabled(clusterCapability)) {
        boolean isPerfDiagnosticModeEnabled = isPerfDiagnosticModeEnabled(vsanClusterConfigSystem, clusterCapability
            .getTarget());
        if (isPerfDiagnosticModeEnabled) {
          ManagedObjectReference networkDiagnosticsClusterMoRef = getClusterMoRefByValue(clusterCapability
              .getTarget().getValue(), clusterMoRefs);
          if (networkDiagnosticsClusterMoRef != null)
            networkDiagnosticsEnabledClustersMoRefs.add(networkDiagnosticsClusterMoRef); 
        } 
      } 
    } 
    return networkDiagnosticsEnabledClustersMoRefs;
  }
  
  private ManagedObjectReference getClusterMoRefByValue(String moRefValue, List<ManagedObjectReference> clusterMoRefs) {
    for (ManagedObjectReference clusterMoRef : clusterMoRefs) {
      if (clusterMoRef.getValue().equals(moRefValue))
        return clusterMoRef; 
    } 
    return null;
  }
  
  private static boolean isNetworkDiagnosticModeEnabled(VsanCapability capability) {
    Set<String> moRefCapabilities = (capability.getCapabilities() != null) ? new HashSet<>(Arrays.asList(capability.getCapabilities())) : Collections.<String>emptySet();
    return moRefCapabilities.contains("diagnosticmode");
  }
  
  private static boolean isPerfDiagnosticModeEnabled(VsanVcClusterConfigSystem vsanClusterConfigSystem, ManagedObjectReference clusterMoRef) throws VsanFault, InvalidState {
    ConfigInfoEx configInfoEx = vsanClusterConfigSystem.getConfigInfoEx(clusterMoRef);
    if (configInfoEx == null)
      return false; 
    VsanPerfsvcConfig perfsvcConfig = configInfoEx.getPerfsvcConfig();
    boolean isPerfDiagnosticModeEnabled = (perfsvcConfig != null && perfsvcConfig.isEnabled() && Boolean.TRUE.equals(perfsvcConfig.getDiagnosticMode()));
    return isPerfDiagnosticModeEnabled;
  }
}

package com.vmware.ph.phservice.common.vsan;

import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.common.impl.ClientFutureImpl;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthSummary;
import com.vmware.vim.vsan.binding.vim.cluster.VsanVcClusterHealthSystem;
import com.vmware.vim.vsan.binding.vim.vsan.VsanHealthPerspective;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VsanVcClusterHealthSystemReader {
  public static final ManagedObjectReference VSAN_VC_CLUSTER_HEALTH_SYSTEM_MO_REF = new ManagedObjectReference("VsanVcClusterHealthSystem", "vsan-cluster-health-system");
  
  private static final Log _log = LogFactory.getLog(VsanVcClusterHealthSystem.class);
  
  private final VmomiClient _vsanHealthVmomiClient;
  
  public VsanVcClusterHealthSystemReader(VmomiClient vsanHealthVmomiClient) {
    this._vsanHealthVmomiClient = vsanHealthVmomiClient;
  }
  
  public VsanClusterHealthSummary queryVsanClusterHealthSummary(ManagedObjectReference clusterMoRef, boolean includeObjUuids, String[] fields, boolean fetchFromCache, VsanHealthPerspective perspective, boolean includeDataProtectionHealth) {
    VsanVcClusterHealthSystem healthSystem;
    try {
      healthSystem = this._vsanHealthVmomiClient.<VsanVcClusterHealthSystem>createStub(VSAN_VC_CLUSTER_HEALTH_SYSTEM_MO_REF);
    } catch (Exception e) {
      _log.warn("Unable to get vsan health system stub: " + e.getMessage());
      return null;
    } 
    ClientFutureImpl clientFutureImpl = new ClientFutureImpl();
    healthSystem.queryClusterHealthSummary(clusterMoRef, null, null, 


        
        Boolean.valueOf(includeObjUuids), fields, 
        
        Boolean.valueOf(fetchFromCache), (perspective != null) ? perspective
        .toString() : null, null, 
        
        Boolean.valueOf(includeDataProtectionHealth), (Future)clientFutureImpl);
    VsanClusterHealthSummary healthSummary = null;
    try {
      healthSummary = (VsanClusterHealthSummary)clientFutureImpl.get();
    } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
      ExceptionsContextManager.store(e);
      _log.warn("Unable to read health summary for cluster: " + clusterMoRef
          .getValue() + ", due to: " + e
          .getMessage());
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return healthSummary;
  }
}

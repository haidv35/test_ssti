package com.vmware.ph.phservice.cloud.dataapp.vsan.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.internal.collector.ObjectIdToContextDataFactory;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextProvider;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientBuilder;
import com.vmware.ph.phservice.common.vim.vc.util.ClusterReader;
import com.vmware.ph.phservice.common.vim.vc.util.DatastoreReader;
import com.vmware.ph.phservice.common.vim.vc.util.HostReader;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.common.vsan.VsanClusterReader;
import com.vmware.ph.phservice.common.vsan.VsanHealthClientBuilder;
import com.vmware.ph.phservice.common.vsan.VsanMassCollectorReader;
import com.vmware.ph.phservice.common.vsan.VsanPerfNetworkDiagnosticsReader;
import com.vmware.ph.phservice.provider.common.QueryContext;
import com.vmware.vim.binding.impl.vmodl.KeyAnyValueImpl;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VsanClusterObjectIdToContextDataFactory implements ObjectIdToContextDataFactory {
  public static final String CLUSTER_OBJECT_TYPE = "ClusterComputeResource";
  
  public static final String NETWORK_DIAGNOSTIC_ENABLED_CLUSTER_OBJECT_TYPE = "VsanNetworkDiagnosticClusterComputeResource";
  
  public static final String OBJECT_ID_FOR_ISOLATED_OBJECTS = "3d44a86d-8f05-47c5-b9be-6e753f699fae";
  
  public static final String CONTEXT_OBJECT_KEY = "objects";
  
  private static final Log _log = LogFactory.getLog(VsanClusterObjectIdToContextDataFactory.class);
  
  private final VimContextProvider _vimContextProvider;
  
  public VsanClusterObjectIdToContextDataFactory(VimContextProvider vimContextProvider) {
    this._vimContextProvider = vimContextProvider;
  }
  
  public Map<String, Object> getObjectIdToContextData(String objectType) {
    if (!"ClusterComputeResource".equals(objectType) && 
      !"VsanNetworkDiagnosticClusterComputeResource".equals(objectType)) {
      if (_log.isDebugEnabled())
        _log.debug(
            String.format("No context will be provided for non-supported object types: %s, returning null for getObjectIdToContextData.", new Object[] { objectType })); 
      return null;
    } 
    VimContext vimContext = this._vimContextProvider.getVimContext();
    if (vimContext == null)
      return null; 
    VcClientBuilder vcClientBuilder = vimContext.getVcClientBuilder(true);
    try(VcClient vcClient = vcClientBuilder.build(); 
        
        VmomiClient vsanHealthClient = createVsanHealthClient(vcClient, vimContext)) {
      Map<String, ManagedObjectReference> clusterUuidToMoRef = getFilteredClusterUuidMoRefs(vcClient, vsanHealthClient, objectType);
      Map<String, Object> results = new LinkedHashMap<>();
      for (Map.Entry<String, ManagedObjectReference> entry : clusterUuidToMoRef.entrySet()) {
        String clusterUuid = entry.getKey();
        ManagedObjectReference clusterMoRef = entry.getValue();
        Object clusterContextData = createQueryContext(clusterUuid, Arrays.asList(new ManagedObjectReference[] { clusterMoRef }));
        results.put(clusterUuid, clusterContextData);
      } 
      if ("ClusterComputeResource".equals(objectType)) {
        List<ManagedObjectReference> clusterMoRefs = new ArrayList<>(clusterUuidToMoRef.values());
        List<ManagedObjectReference> isolatedObjectsMoRefs = getIsolatedHostsMoRefs(vcClient, vsanHealthClient, clusterMoRefs);
        isolatedObjectsMoRefs.addAll(
            getIsolatedDatastoreMoRefs(vcClient, clusterMoRefs));
        if (!isolatedObjectsMoRefs.isEmpty()) {
          Object isolatedObjectsContextData = createQueryContext("3d44a86d-8f05-47c5-b9be-6e753f699fae", isolatedObjectsMoRefs);
          results.put("3d44a86d-8f05-47c5-b9be-6e753f699fae", isolatedObjectsContextData);
        } 
      } 
      return results;
    } catch (Exception e) {
      if (_log.isDebugEnabled()) {
        _log.debug("Error occured while retrieving objectIdToContextData, so returning null. ", e);
      } else if (_log.isWarnEnabled()) {
        _log.warn("Error occured while retrieving objectIdToContextData, so returning null: " + e
            .getMessage());
      } 
      return null;
    } 
  }
  
  private static Map<String, ManagedObjectReference> getFilteredClusterUuidMoRefs(VcClient vcClient, VmomiClient vsanHealthClient, String objectType) throws Exception {
    VsanPerfNetworkDiagnosticsReader networkDiagnosticsReader;
    List<ManagedObjectReference> networkDiagnosticsEnabledClusters;
    Map<String, ManagedObjectReference> filteredClusterUuidToMoRef;
    ClusterReader clusterReader = new ClusterReader(vcClient);
    List<ManagedObjectReference> clusterMoRefs = clusterReader.getClusterMoRefs(0, -1);
    VsanMassCollectorReader vsanMassCollectorReader = new VsanMassCollectorReader(vsanHealthClient);
    Map<String, ManagedObjectReference> clusterUuidToMoRef = vsanMassCollectorReader.getClusterUuidToMoRefs(clusterMoRefs);
    switch (objectType) {
      case "ClusterComputeResource":
        return clusterUuidToMoRef;
      case "VsanNetworkDiagnosticClusterComputeResource":
        networkDiagnosticsReader = new VsanPerfNetworkDiagnosticsReader(vsanHealthClient, vcClient);
        networkDiagnosticsEnabledClusters = networkDiagnosticsReader.getNetworkDiagnosticsEnabledClusters(new ArrayList(clusterUuidToMoRef
              .values()));
        filteredClusterUuidToMoRef = new HashMap<>();
        for (Map.Entry<String, ManagedObjectReference> entry : clusterUuidToMoRef.entrySet()) {
          if (networkDiagnosticsEnabledClusters.contains(entry.getValue()))
            filteredClusterUuidToMoRef.put(entry.getKey(), entry.getValue()); 
        } 
        return filteredClusterUuidToMoRef;
    } 
    return Collections.emptyMap();
  }
  
  private static List<ManagedObjectReference> getIsolatedHostsMoRefs(VcClient vcClient, VmomiClient vsanHealthClient, List<ManagedObjectReference> clusterMoRefs) throws Exception {
    List<ManagedObjectReference> isolatedHostMoRefs = new ArrayList<>();
    try {
      ClusterReader clusterReader = new ClusterReader(vcClient);
      ManagedObjectReference[] clustersHostsMoRefs = clusterReader.getClustersHosts(clusterMoRefs);
      Set<String> processedHostsMoRefIds = new HashSet<>();
      for (ManagedObjectReference hostMoRef : clustersHostsMoRefs)
        processedHostsMoRefIds.add(hostMoRef.getValue()); 
      clustersHostsMoRefs = null;
      VsanClusterReader vsanClusterReader = new VsanClusterReader(vsanHealthClient, vcClient);
      List<ManagedObjectReference> witnessHostsMoRefs = vsanClusterReader.getWitnessHosts(clusterMoRefs);
      for (ManagedObjectReference witnessHostMoRef : witnessHostsMoRefs)
        processedHostsMoRefIds.add(witnessHostMoRef.getValue()); 
      witnessHostsMoRefs = null;
      HostReader hostReader = new HostReader(vcClient);
      List<ManagedObjectReference> allHostMoRefs = hostReader.getHostMoRefs();
      for (ManagedObjectReference hostMoRef : allHostMoRefs) {
        if (!processedHostsMoRefIds.contains(hostMoRef.getValue()))
          isolatedHostMoRefs.add(hostMoRef); 
      } 
    } catch (Exception e) {
      if (_log.isDebugEnabled()) {
        _log.debug("Unable to retrieve isloated hosts: ", e);
      } else if (_log.isWarnEnabled()) {
        _log.warn("Unable to retrieve isloated hosts: " + e.getMessage());
      } 
    } 
    return isolatedHostMoRefs;
  }
  
  private static List<ManagedObjectReference> getIsolatedDatastoreMoRefs(VcClient vcClient, List<ManagedObjectReference> clusterMoRefs) {
    List<ManagedObjectReference> isolatedDatastoreMoRefs = new ArrayList<>();
    try {
      ClusterReader clusterReader = new ClusterReader(vcClient);
      List<ManagedObjectReference> clustersDatastoreMoRefs = clusterReader.getClustersDatastores(clusterMoRefs);
      DatastoreReader datastoreReader = new DatastoreReader(vcClient);
      List<ManagedObjectReference> allDatastoreMoRefs = datastoreReader.getDatastoreMoRefs();
      for (ManagedObjectReference datastoreMoRef : allDatastoreMoRefs) {
        if (!clustersDatastoreMoRefs.contains(datastoreMoRef))
          isolatedDatastoreMoRefs.add(datastoreMoRef); 
      } 
    } catch (Exception e) {
      if (_log.isDebugEnabled()) {
        _log.debug("Unable to retrieve isloated datastores: ", e);
      } else if (_log.isWarnEnabled()) {
        _log.warn("Unable to retrieve isloated datastores: " + e.getMessage());
      } 
    } 
    return isolatedDatastoreMoRefs;
  }
  
  private static VmomiClient createVsanHealthClient(VcClient vcClient, VimContext vimContext) {
    VmomiClient vsanHealthVmomiClient = VsanHealthClientBuilder.forVc(vimContext, vcClient).build();
    return vsanHealthVmomiClient;
  }
  
  private static QueryContext createQueryContext(String objectId, List<ManagedObjectReference> objectMoRefs) {
    if (objectMoRefs == null)
      return null; 
    List<Object> objects = new ArrayList();
    for (ManagedObjectReference objectMoRef : objectMoRefs) {
      KeyAnyValueImpl keyAnyValueImpl = new KeyAnyValueImpl();
      keyAnyValueImpl.setKey(objectId);
      keyAnyValueImpl.setValue(objectMoRef);
      objects.add(keyAnyValueImpl);
    } 
    Map<String, List<Object>> objectKeyToObjects = new HashMap<>();
    objectKeyToObjects.put("objects", objects);
    QueryContext queryContext = new QueryContext(objectKeyToObjects);
    return queryContext;
  }
}

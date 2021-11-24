package com.vmware.ph.phservice.common.vim.vc.util;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.vim.internal.vc.pc.VcPropertyCollectorReader;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorReader;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorUtil;
import com.vmware.vim.binding.vim.ClusterComputeResource;
import com.vmware.vim.binding.vim.view.ContainerView;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.query.InvalidProperty;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClusterReader {
  private static final String CLUSTER_MO_REF_TYPE = "ClusterComputeResource";
  
  private static final Log _log = LogFactory.getLog(ClusterReader.class);
  
  private final VcClient _vcClient;
  
  public ClusterReader(VcClient vcClient) {
    this._vcClient = vcClient;
  }
  
  public List<ManagedObjectReference> getClusterMoRefs(int offset, int limit) {
    List<String> clusterPropertyNames = Arrays.asList(new String[] { "summary.totalCpu" });
    VmodlTypeMap vmodlTypeMap = this._vcClient.getVmodlContext().getVmodlTypeMap();
    VcPropertyCollectorReader pcReader = new VcPropertyCollectorReader(this._vcClient);
    ManagedObjectReference containerViewMoRef = pcReader.createContainerView();
    List<Pair<VmodlType, String>> traversalChain = Arrays.asList((Pair<VmodlType, String>[])new Pair[] { new Pair<>(vmodlTypeMap.getVmodlType(ContainerView.class), "view") });
    List<ManagedObjectReference> clusterMoRefs = new ArrayList<>();
    PropertyCollector.FilterSpec dataStoresFilterSpec = PropertyCollectorUtil.createTraversableFilterSpec(vmodlTypeMap
        .getVmodlType("ClusterComputeResource"), clusterPropertyNames, containerViewMoRef, traversalChain, vmodlTypeMap);
    try {
      List<PropertyCollectorReader.PcResourceItem> results = null;
      try {
        results = pcReader.retrieveContent(dataStoresFilterSpec, clusterPropertyNames, offset, limit);
      } catch (InvalidProperty e) {
        if (_log.isDebugEnabled())
          _log.debug("Failed to read clusters: ", (Throwable)e); 
      } 
      if (results != null)
        for (PropertyCollectorReader.PcResourceItem pcResourceItem : results) {
          ManagedObjectReference moRef = (ManagedObjectReference)pcResourceItem.getPropertyValues().get(0);
          clusterMoRefs.add(moRef);
        }  
    } finally {
      pcReader.destroyContainerView(containerViewMoRef);
    } 
    return clusterMoRefs;
  }
  
  public ManagedObjectReference[] getClusterHosts(ManagedObjectReference clusterMoRef) {
    ClusterComputeResource clusterStub = this._vcClient.<ClusterComputeResource>createMo(clusterMoRef);
    ManagedObjectReference[] hostMoRefs = clusterStub.getHost();
    return hostMoRefs;
  }
  
  public ManagedObjectReference[] getClustersHosts(List<ManagedObjectReference> clusterMoRefs) {
    List<ManagedObjectReference> clustersHostsMoRefs = new ArrayList<>();
    for (ManagedObjectReference clusterMoRef : clusterMoRefs) {
      ManagedObjectReference[] hostMoRefs = getClusterHosts(clusterMoRef);
      if (hostMoRefs != null)
        clustersHostsMoRefs.addAll(Arrays.asList(hostMoRefs)); 
    } 
    return clustersHostsMoRefs.<ManagedObjectReference>toArray(
        new ManagedObjectReference[clustersHostsMoRefs.size()]);
  }
  
  public Map<String, List<ManagedObjectReference>> getClustersDatastoresTypeToMoRefs(Set<ManagedObjectReference> clusterMoRefs) {
    DatastoreReader datastoreReader = new DatastoreReader(this._vcClient);
    return getClustersDatastoresTypeToMoRefs(clusterMoRefs, datastoreReader);
  }
  
  public List<ManagedObjectReference> getClustersDatastores(Collection<ManagedObjectReference> clusterMoRefs) {
    List<ManagedObjectReference> clustersDatastoreMoRefs = new ArrayList<>();
    for (ManagedObjectReference clusterMoRef : clusterMoRefs) {
      List<ManagedObjectReference> datastoreMoRefs = getClusterDatastores(clusterMoRef);
      clustersDatastoreMoRefs.addAll(datastoreMoRefs);
    } 
    return clustersDatastoreMoRefs;
  }
  
  public List<ManagedObjectReference> getClusterDatastores(ManagedObjectReference clusterMoRef) {
    ClusterComputeResource clusterStub = this._vcClient.<ClusterComputeResource>createMo(clusterMoRef);
    ManagedObjectReference[] datastoreMoRefs = clusterStub.getDatastore();
    return (datastoreMoRefs != null) ? 
      Arrays.<ManagedObjectReference>asList(datastoreMoRefs) : 
      Collections.<ManagedObjectReference>emptyList();
  }
  
  Map<String, List<ManagedObjectReference>> getClustersDatastoresTypeToMoRefs(Set<ManagedObjectReference> clusterMoRefs, DatastoreReader datastoreReader) {
    Map<String, List<ManagedObjectReference>> result = new HashMap<>();
    Map<ManagedObjectReference, String> datastoreMoRefToType = datastoreReader.getDatastoreMoRefsToTypes();
    List<ManagedObjectReference> clustersDatastoreMoRefs = getClustersDatastores(clusterMoRefs);
    for (Map.Entry<ManagedObjectReference, String> entry : datastoreMoRefToType.entrySet()) {
      ManagedObjectReference datastoreMoRef = entry.getKey();
      String datastoreTypeLowerCased = ((String)entry.getValue()).toLowerCase();
      if (clustersDatastoreMoRefs.contains(datastoreMoRef)) {
        List<ManagedObjectReference> moRefsForType = result.get(datastoreTypeLowerCased);
        if (moRefsForType == null) {
          moRefsForType = new ArrayList<>();
          result.put(datastoreTypeLowerCased, moRefsForType);
        } 
        moRefsForType.add(datastoreMoRef);
      } 
    } 
    return result;
  }
}

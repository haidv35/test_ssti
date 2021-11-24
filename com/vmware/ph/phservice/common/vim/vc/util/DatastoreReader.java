package com.vmware.ph.phservice.common.vim.vc.util;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.vim.internal.vc.pc.VcPropertyCollectorReader;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorReader;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorUtil;
import com.vmware.vim.binding.vim.view.ContainerView;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.query.InvalidProperty;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DatastoreReader {
  private static final Log _log = LogFactory.getLog(DatastoreReader.class);
  
  static final String DATASTORE_TYPE_PROPERTY_NAME = "summary.type";
  
  private VcClient _vcClient;
  
  public DatastoreReader(VcClient vcClient) {
    this._vcClient = vcClient;
  }
  
  @Deprecated
  public static List<ManagedObjectReference> getDatastoreMoRefs(VcClient vcClient) {
    DatastoreReader datastoreReader = new DatastoreReader(vcClient);
    return datastoreReader.getDatastoreMoRefs();
  }
  
  @Deprecated
  public static List<ManagedObjectReference> getDatastoreMoRefs(VcClient vcClient, List<String> datastoreTypesToInclude) {
    DatastoreReader datastoreReader = new DatastoreReader(vcClient);
    return datastoreReader.getDatastoreMoRefs(datastoreTypesToInclude);
  }
  
  public List<ManagedObjectReference> getDatastoreMoRefs() {
    List<String> datastoreTypesToInclude = null;
    return getDatastoreMoRefs(datastoreTypesToInclude);
  }
  
  public List<ManagedObjectReference> getDatastoreMoRefs(List<String> datastoreTypesToInclude) {
    Map<ManagedObjectReference, String> datastoreMoRefsToTypes = getDatastoreMoRefsToTypes();
    List<String> datastoreTypesToIncludeLowerCased = convertTypesToLowerCase(datastoreTypesToInclude);
    List<ManagedObjectReference> datastoreMoRefs = new ArrayList<>();
    for (Map.Entry<ManagedObjectReference, String> entry : datastoreMoRefsToTypes.entrySet()) {
      String dataStoreType = ((String)entry.getValue()).toLowerCase();
      if (datastoreTypesToIncludeLowerCased == null || datastoreTypesToIncludeLowerCased
        .contains(dataStoreType))
        datastoreMoRefs.add(entry.getKey()); 
    } 
    return datastoreMoRefs;
  }
  
  public Map<ManagedObjectReference, String> getDatastoreMoRefsToTypes() {
    List<PropertyCollectorReader.PcResourceItem> results = getDatastores();
    Map<ManagedObjectReference, String> datastoreMoRefsToTypes = new HashMap<>();
    if (results != null)
      for (PropertyCollectorReader.PcResourceItem pcResourceItem : results) {
        ManagedObjectReference moRef = (ManagedObjectReference)pcResourceItem.getPropertyValues().get(0);
        String dataStoreType = (String)pcResourceItem.getPropertyValues().get(1);
        datastoreMoRefsToTypes.put(moRef, dataStoreType);
      }  
    return datastoreMoRefsToTypes;
  }
  
  List<PropertyCollectorReader.PcResourceItem> getDatastores() {
    VmodlTypeMap vmodlTypeMap = this._vcClient.getVmodlContext().getVmodlTypeMap();
    VcPropertyCollectorReader pcReader = new VcPropertyCollectorReader(this._vcClient);
    ManagedObjectReference containerViewMoRef = pcReader.createContainerView();
    List<Pair<VmodlType, String>> traversalChain = Arrays.asList((Pair<VmodlType, String>[])new Pair[] { new Pair<>(vmodlTypeMap.getVmodlType(ContainerView.class), "view") });
    List<String> dataStorePropertyNames = Arrays.asList(new String[] { "summary.type" });
    PropertyCollector.FilterSpec dataStoresFilterSpec = PropertyCollectorUtil.createTraversableFilterSpec(vmodlTypeMap
        .getVmodlType("Datastore"), dataStorePropertyNames, containerViewMoRef, traversalChain, vmodlTypeMap);
    try {
      List<PropertyCollectorReader.PcResourceItem> results = null;
      try {
        results = pcReader.retrieveContent(dataStoresFilterSpec, dataStorePropertyNames, 0, -1);
      } catch (InvalidProperty e) {
        if (_log.isDebugEnabled())
          _log.debug("Failed to read datastores: ", (Throwable)e); 
      } 
      return results;
    } finally {
      pcReader.destroyContainerView(containerViewMoRef);
    } 
  }
  
  private static List<String> convertTypesToLowerCase(List<String> datastoreTypesToInclude) {
    List<String> lowerCaseList = null;
    if (datastoreTypesToInclude != null && !datastoreTypesToInclude.isEmpty()) {
      lowerCaseList = new ArrayList<>(datastoreTypesToInclude.size());
      for (String datastoreType : datastoreTypesToInclude)
        lowerCaseList.add(datastoreType.toLowerCase()); 
    } 
    return lowerCaseList;
  }
}

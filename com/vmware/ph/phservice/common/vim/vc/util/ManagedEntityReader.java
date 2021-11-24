package com.vmware.ph.phservice.common.vim.vc.util;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.vim.internal.vc.pc.VcPropertyCollectorReader;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.VmodlUtil;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorReader;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorUtil;
import com.vmware.vim.binding.vim.ManagedEntity;
import com.vmware.vim.binding.vim.view.ContainerView;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.query.InvalidProperty;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManagedEntityReader {
  private static final String[] MANAGED_ENTITIES_TO_EXCLUDE = new String[] { "ManagedEntity", "AntiAffinityGroup", "ContentLibrary", "ContentLibraryItem", "TagPolicy", "TagPolicyOption", "VirtualDatacenter" };
  
  private final VmodlTypeMap _vmodlTypeMap;
  
  private final VcPropertyCollectorReader _propertyCollectorReader;
  
  public ManagedEntityReader(VcClient vcClient) {
    this._vmodlTypeMap = vcClient.getVmodlContext().getVmodlTypeMap();
    this._propertyCollectorReader = new VcPropertyCollectorReader(vcClient);
  }
  
  public static List<VmodlType> getInventoryManagedEntityVmodlTypes(VmodlTypeMap vmodlTypeMap) {
    List<VmodlType> inventoryManagedEntityVmodlTypes = VmodlUtil.getVmodlTypesAssignableFromClass(ManagedEntity.class, vmodlTypeMap);
    for (String wsdlType : MANAGED_ENTITIES_TO_EXCLUDE)
      inventoryManagedEntityVmodlTypes.remove(vmodlTypeMap.getVmodlType(wsdlType)); 
    return inventoryManagedEntityVmodlTypes;
  }
  
  public List<ManagedObjectReference> getManagedEntityMoRefs(VmodlType vmodlType, int offset, int limit) {
    List<PropertyCollectorReader.PcResourceItem> pcResourceItems = retrieveContent(vmodlType, offset, limit);
    List<ManagedObjectReference> entityMoRefs = new ArrayList<>(pcResourceItems.size());
    for (PropertyCollectorReader.PcResourceItem pcResourceItem : pcResourceItems) {
      List<Object> propertyValues = pcResourceItem.getPropertyValues();
      ManagedObjectReference moRef = (ManagedObjectReference)propertyValues.get(0);
      entityMoRefs.add(moRef);
    } 
    return entityMoRefs;
  }
  
  private List<PropertyCollectorReader.PcResourceItem> retrieveContent(VmodlType vmodlType, int offset, int limit) {
    List<String> pcPropertyNames = Collections.emptyList();
    VmodlType containedVmodlType = this._vmodlTypeMap.getVmodlType(ContainerView.class);
    List<Pair<VmodlType, String>> traversalChain = Collections.singletonList(new Pair<>(containedVmodlType, "view"));
    ManagedObjectReference startRef = this._propertyCollectorReader.createContainerView();
    PropertyCollector.FilterSpec pcFilterSpec = PropertyCollectorUtil.createTraversableFilterSpec(vmodlType, pcPropertyNames, startRef, traversalChain, this._vmodlTypeMap);
    try {
      return this._propertyCollectorReader.retrieveContent(pcFilterSpec, pcPropertyNames, offset, limit);
    } catch (InvalidProperty e) {
      throw new IllegalArgumentException("Invalid property " + e.getName(), e);
    } finally {
      this._propertyCollectorReader.destroyContainerView(startRef);
    } 
  }
}

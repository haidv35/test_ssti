package com.vmware.ph.phservice.common.vmomi.pc;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.ArrayList;
import java.util.List;

public class PropertyCollectorUtil {
  public static PropertyCollector.FilterSpec createMoRefFilterSpec(ManagedObjectReference moRef, List<String> properties, VmodlTypeMap vmodlTypeMap) {
    PropertyCollector.FilterSpec filterSpec = createMoRefsFilterSpec(new ManagedObjectReference[] { moRef }, properties, vmodlTypeMap);
    return filterSpec;
  }
  
  public static PropertyCollector.FilterSpec createMoRefsFilterSpec(ManagedObjectReference[] moRefs, List<String> properties, VmodlTypeMap vmodlTypeMap) {
    List<PropertyCollector.ObjectSpec> objectSpecs = new ArrayList<>();
    for (ManagedObjectReference moRef : moRefs) {
      PropertyCollector.ObjectSpec objectSpec = createMoRefObjectSpec(moRef);
      objectSpecs.add(objectSpec);
    } 
    PropertyCollector.FilterSpec filterSpec = createFilterSpec(objectSpecs
        .<PropertyCollector.ObjectSpec>toArray(new PropertyCollector.ObjectSpec[objectSpecs.size()]), vmodlTypeMap
        .getVmodlType(moRefs[0].getType()), properties);
    return filterSpec;
  }
  
  public static PropertyCollector.FilterSpec createTraversableFilterSpec(VmodlType targetVmodlType, List<String> properties, ManagedObjectReference startMoRef, List<Pair<VmodlType, String>> traversalChain, VmodlTypeMap vmodlTypeMap) {
    PropertyCollector.ObjectSpec objectSpec = createTraversableObjectSpec(startMoRef, traversalChain, vmodlTypeMap);
    PropertyCollector.FilterSpec filterSpec = createFilterSpec(new PropertyCollector.ObjectSpec[] { objectSpec }, targetVmodlType, properties);
    return filterSpec;
  }
  
  private static PropertyCollector.ObjectSpec createTraversableObjectSpec(ManagedObjectReference startMoRef, List<Pair<VmodlType, String>> traversalChain, VmodlTypeMap vmodlTypeMap) {
    PropertyCollector.ObjectSpec objectSpec = new PropertyCollector.ObjectSpec();
    objectSpec.obj = startMoRef;
    objectSpec.setSkip(Boolean.valueOf((traversalChain != null && traversalChain.size() > 0)));
    if (traversalChain != null && traversalChain.size() > 0) {
      PropertyCollector.TraversalSpec previousTraversalLegSpec = null;
      for (int i = 0; i < traversalChain.size(); i++) {
        Pair<VmodlType, String> traversalLeg = traversalChain.get(i);
        PropertyCollector.TraversalSpec traversalLegSpec = new PropertyCollector.TraversalSpec();
        traversalLegSpec.setType(((VmodlType)traversalLeg.getFirst()).getTypeName());
        traversalLegSpec.setPath((String)traversalLeg.getSecond());
        traversalLegSpec.setSkip(Boolean.valueOf((i != traversalChain.size() - 1)));
        if (i == 0) {
          objectSpec.setSelectSet(new PropertyCollector.SelectionSpec[] { (PropertyCollector.SelectionSpec)traversalLegSpec });
        } else if (previousTraversalLegSpec != null) {
          previousTraversalLegSpec.setSelectSet(new PropertyCollector.SelectionSpec[] { (PropertyCollector.SelectionSpec)traversalLegSpec });
        } 
        previousTraversalLegSpec = traversalLegSpec;
      } 
    } 
    return objectSpec;
  }
  
  private static PropertyCollector.FilterSpec createFilterSpec(PropertyCollector.ObjectSpec[] objectSpecs, VmodlType managedObjectVmodlType, List<String> properties) {
    PropertyCollector.FilterSpec filterSpec = new PropertyCollector.FilterSpec();
    filterSpec.objectSet = objectSpecs;
    PropertyCollector.PropertySpec propertySpec = createPropertySpec(managedObjectVmodlType, properties);
    filterSpec.propSet = new PropertyCollector.PropertySpec[] { propertySpec };
    filterSpec.setReportMissingObjectsInResults(Boolean.valueOf(true));
    return filterSpec;
  }
  
  private static PropertyCollector.ObjectSpec createMoRefObjectSpec(ManagedObjectReference moRef) {
    PropertyCollector.ObjectSpec objectSpec = new PropertyCollector.ObjectSpec();
    objectSpec.obj = moRef;
    objectSpec.setSkip(Boolean.valueOf(false));
    return objectSpec;
  }
  
  private static PropertyCollector.PropertySpec createPropertySpec(VmodlType targetVmodelType, List<String> properties) {
    PropertyCollector.PropertySpec propSpec = new PropertyCollector.PropertySpec();
    propSpec.type = targetVmodelType.getTypeName();
    propSpec.setPathSet(properties.<String>toArray(new String[properties.size()]));
    return propSpec;
  }
}

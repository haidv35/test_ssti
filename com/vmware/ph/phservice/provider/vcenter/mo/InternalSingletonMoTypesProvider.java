package com.vmware.ph.phservice.provider.vcenter.mo;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.VmodlUtil;
import com.vmware.ph.phservice.provider.common.vmomi.mo.MoTypesProvider;
import com.vmware.vim.binding.vim.InternalServiceInstanceContent;
import com.vmware.vim.vmomi.core.types.ComplexType;
import com.vmware.vim.vmomi.core.types.DataObjectType;
import com.vmware.vim.vmomi.core.types.ManagedObjectType;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class InternalSingletonMoTypesProvider implements MoTypesProvider {
  private final VmodlTypeMap _vmodlTypeMap;
  
  private final MoTypesProvider _conflictingTypesProvider;
  
  private InternalSingletonMoTypesProvider(VmodlTypeMap vmodlTypeMap, MoTypesProvider conflictingTypesProvider) {
    this._vmodlTypeMap = vmodlTypeMap;
    this._conflictingTypesProvider = conflictingTypesProvider;
  }
  
  static MoTypesProvider forVcClient(VcClient vcClient) {
    VmodlTypeMap vmodlTypeMap = vcClient.getVmodlContext().getVmodlTypeMap();
    return new InternalSingletonMoTypesProvider(vmodlTypeMap, new SingletonMoTypesProvider(vcClient));
  }
  
  public List<ManagedObjectType> getManagedObjectTypes() {
    List<ManagedObjectType> managedObjectTypes = new LinkedList<>();
    DataObjectType internalServiceContentType = (DataObjectType)this._vmodlTypeMap.getVmodlType(InternalServiceInstanceContent.class);
    List<VmodlType> internalMoVmodlTypes = VmodlUtil.getManagedObjectReferenceVmodlTypesInParentTypeProperties((ComplexType)internalServiceContentType);
    Set<ManagedObjectType> conflictingTypes = new HashSet<>(this._conflictingTypesProvider.getManagedObjectTypes());
    for (VmodlType vmodlType : internalMoVmodlTypes) {
      if (!conflictingTypes.contains(vmodlType))
        managedObjectTypes.add((ManagedObjectType)vmodlType); 
    } 
    return managedObjectTypes;
  }
}

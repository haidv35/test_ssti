package com.vmware.ph.phservice.provider.vcenter.mo;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.VmodlUtil;
import com.vmware.ph.phservice.provider.common.vmomi.mo.MoTypesProvider;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.vmomi.core.types.ComplexType;
import com.vmware.vim.vmomi.core.types.DataObjectType;
import com.vmware.vim.vmomi.core.types.ManagedObjectType;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.LinkedList;
import java.util.List;

class SingletonMoTypesProvider implements MoTypesProvider {
  private final VmodlTypeMap _vmodlTypeMap;
  
  SingletonMoTypesProvider(VcClient vcClient) {
    this._vmodlTypeMap = vcClient.getVmodlContext().getVmodlTypeMap();
  }
  
  public List<ManagedObjectType> getManagedObjectTypes() {
    List<ManagedObjectType> managedObjectTypes = new LinkedList<>();
    DataObjectType serviceContentType = (DataObjectType)this._vmodlTypeMap.getVmodlType(ServiceInstanceContent.class);
    List<VmodlType> moVmodlTypes = VmodlUtil.getManagedObjectReferenceVmodlTypesInParentTypeProperties((ComplexType)serviceContentType);
    for (VmodlType vmodlType : moVmodlTypes)
      managedObjectTypes.add((ManagedObjectType)vmodlType); 
    return managedObjectTypes;
  }
}

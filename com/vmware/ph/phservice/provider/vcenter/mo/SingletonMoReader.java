package com.vmware.ph.phservice.provider.vcenter.mo;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.vmomi.VmomiDataProviderUtil;
import com.vmware.ph.phservice.provider.common.vmomi.mo.MoReader;
import com.vmware.vim.binding.vim.InternalServiceInstanceContent;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.binding.vmodl.DataObject;
import com.vmware.vim.binding.vmodl.ManagedObject;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.List;

class SingletonMoReader implements MoReader {
  private static final String NOT_MO_ERROR_PATTERN = "The given VMODL type [%s] in not a managed object type";
  
  private final VcClient _vcClient;
  
  private final DataObject[] _serviceInstanceContents;
  
  private SingletonMoReader(VcClient vcClient, DataObject... serviceInstanceContents) {
    this._vcClient = vcClient;
    this._serviceInstanceContents = serviceInstanceContents;
  }
  
  static SingletonMoReader createPublicSingletonMoReader(VcClient vcClient) {
    ServiceInstanceContent serviceInstanceContent = vcClient.getServiceInstanceContent();
    return new SingletonMoReader(vcClient, new DataObject[] { (DataObject)serviceInstanceContent });
  }
  
  static SingletonMoReader createInternalSingletonMoReader(VcClient vcClient) {
    InternalServiceInstanceContent internalServiceInstanceContent = vcClient.getInternalServiceInstanceContent();
    return new SingletonMoReader(vcClient, new DataObject[] { (DataObject)internalServiceInstanceContent });
  }
  
  public ManagedObject getManagedObject(VmodlType vmodlType) {
    if (vmodlType == null || vmodlType.getKind() != VmodlType.Kind.MANAGED_OBJECT)
      throw new IllegalArgumentException(String.format("The given VMODL type [%s] in not a managed object type", new Object[] { vmodlType })); 
    for (DataObject serviceInstanceContent : this._serviceInstanceContents) {
      ManagedObjectReference moRef = getManagedObjectReference(serviceInstanceContent, vmodlType);
      if (moRef != null)
        return this._vcClient.createMo(moRef); 
    } 
    throw new IllegalStateException("Cannot retrieve managed object of type " + vmodlType);
  }
  
  private ManagedObjectReference getManagedObjectReference(DataObject serviceInstanceContent, VmodlType vmodlType) {
    VmodlTypeMap vmodlTypeMap = this._vcClient.getVmodlContext().getVmodlTypeMap();
    List<Object> moReferenceObjects = VmomiDataProviderUtil.getPropertyValuesForPropertiesOfKind(serviceInstanceContent, vmodlTypeMap, VmodlType.Kind.MOREF);
    for (Object moReferenceObject : moReferenceObjects) {
      if (moReferenceObject == null)
        continue; 
      ManagedObjectReference moRef = (ManagedObjectReference)moReferenceObject;
      VmodlType managedObjectType = vmodlTypeMap.getVmodlType(moRef.getType());
      if (moRef != null && vmodlType.equals(managedObjectType))
        return moRef; 
    } 
    return null;
  }
}

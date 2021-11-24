package com.vmware.ph.phservice.provider.vcenter.mo;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.VmodlUtil;
import com.vmware.ph.phservice.provider.common.vim.pc.VimPcSchemaConverter;
import com.vmware.vim.binding.vim.ServiceInstance;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.binding.vmodl.DataObject;
import com.vmware.vim.vmomi.core.types.ManagedObjectType;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.HashMap;
import java.util.Map;

public class SingletonMoSchemaConverter extends VimPcSchemaConverter {
  public static final String SERVICE_INSTANCE_CONTENT_PROPERTY_PATH = "content";
  
  private final VcClient _vcClient;
  
  public SingletonMoSchemaConverter(VmodlTypeMap vmodlTypeMap, VcClient vcClient) {
    super(vmodlTypeMap);
    this._vcClient = vcClient;
  }
  
  protected Map<VmodlType, Pair<VmodlType, String>> getRetrievalRulesForServiceInstanceMOs() {
    ManagedObjectType serviceInstanceVmodlType = (ManagedObjectType)this._vmodlTypeMap.getVmodlType(ServiceInstance.class);
    ServiceInstanceContent serviceInstanceContent = this._vcClient.getServiceInstanceContent();
    Map<VmodlType, Pair<VmodlType, String>> serviceInstanceContentMoRetrievalRules = VmodlUtil.getRetrievalRulesForDataObjectRelatedManagedObjects((VmodlType)serviceInstanceVmodlType, "content", (DataObject)serviceInstanceContent, this._vmodlTypeMap);
    return serviceInstanceContentMoRetrievalRules;
  }
  
  protected Map<VmodlType, Pair<VmodlType, String>> getRetrievalRulesForInventoryMEs() {
    return new HashMap<>();
  }
}

package com.vmware.ph.phservice.provider.common.vim.pc;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.vim.vc.util.ManagedEntityReader;
import com.vmware.ph.phservice.common.vmomi.VmodlUtil;
import com.vmware.ph.phservice.provider.common.vmomi.pc.PcSchemaConverter;
import com.vmware.vim.binding.vim.ServiceInstance;
import com.vmware.vim.binding.vim.view.ContainerView;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VimPcSchemaConverter extends PcSchemaConverter {
  private static final String CONTAINER_VIEW_VIEW_PROPERTY_PATH = "view";
  
  public VimPcSchemaConverter(VmodlTypeMap vmodlTypeMap) {
    super(vmodlTypeMap);
  }
  
  protected final Map<VmodlType, List<Pair<VmodlType, String>>> loadManagedObjectToRetrievalRulesChain() {
    Map<VmodlType, List<Pair<VmodlType, String>>> retrievalRules = new LinkedHashMap<>();
    Map<VmodlType, Pair<VmodlType, String>> managedObjectToRetrievalRules = getRetrievalRulesForServiceInstanceMOs();
    for (Map.Entry<VmodlType, Pair<VmodlType, String>> moToRetrievalRule : managedObjectToRetrievalRules.entrySet()) {
      VmodlType moVmodlType = moToRetrievalRule.getKey();
      Pair<VmodlType, String> retrievalRule = moToRetrievalRule.getValue();
      if (retrievalRule != null) {
        retrievalRules.put(moVmodlType, Arrays.asList((Pair<VmodlType, String>[])new Pair[] { retrievalRule }));
        continue;
      } 
      retrievalRules.put(moVmodlType, new ArrayList<>());
    } 
    managedObjectToRetrievalRules = getRetrievalRulesForInventoryMEs();
    VmodlType containerViewVmodlType = this._vmodlTypeMap.getVmodlType(ContainerView.class);
    Pair<VmodlType, String> meRetrievalRule = new Pair<>(containerViewVmodlType, "view");
    for (Map.Entry<VmodlType, Pair<VmodlType, String>> moToRetrievalRule : managedObjectToRetrievalRules.entrySet()) {
      VmodlType moVmodlType = moToRetrievalRule.getKey();
      Pair<VmodlType, String> retrievalRule = moToRetrievalRule.getValue();
      if (retrievalRule == null) {
        retrievalRules.put(moVmodlType, 
            
            Arrays.asList((Pair<VmodlType, String>[])new Pair[] { meRetrievalRule }));
        continue;
      } 
      retrievalRules.put(moVmodlType, 
          
          Arrays.asList((Pair<VmodlType, String>[])new Pair[] { meRetrievalRule, retrievalRule }));
    } 
    return retrievalRules;
  }
  
  protected Map<VmodlType, Pair<VmodlType, String>> getRetrievalRulesForServiceInstanceMOs() {
    VmodlType serviceInstanceVmodlType = this._vmodlTypeMap.getVmodlType(ServiceInstance.class);
    return VmodlUtil.getRetrievalRulesForRelatedManagedObjects(
        Arrays.asList(new VmodlType[] { serviceInstanceVmodlType }));
  }
  
  protected Map<VmodlType, Pair<VmodlType, String>> getRetrievalRulesForInventoryMEs() {
    List<VmodlType> inventoryManagedEntityVmodlTypes = ManagedEntityReader.getInventoryManagedEntityVmodlTypes(this._vmodlTypeMap);
    return VmodlUtil.getRetrievalRulesForRelatedManagedObjects(inventoryManagedEntityVmodlTypes);
  }
}

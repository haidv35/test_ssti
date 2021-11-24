package com.vmware.ph.phservice.provider.vcenter.resourcemodel;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.provider.common.vim.pc.VimPcSchemaConverter;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PcResourceModelSchemaConverter extends VimPcSchemaConverter {
  private static final List<String> SERVICE_INSTANCE_RELATED_MANAGED_OBJECTS = Arrays.asList(new String[] { "CustomFieldsManager", "ServiceInstance" });
  
  private static final List<String> INVENTORY_RELATED_MANAGED_OBJECTS_RULES = Arrays.asList(new String[] { 
        "HostAuthenticationManager", "HostCacheConfigurationManager", "HostCertificateManager", "HostCpuSchedulerSystem", "HostDatastoreSystem", "HostDiagnosticSystem", "HostEsxAgentHostManager", "HostFirewallSystem", "HostNetworkSystem", "HostPciPassthruSystem", 
        "HostPowerSystem", "HostStorageSystem", "HostProfile", "ScheduledTask", "EnvironmentBrowser", "VirtualMachineSnapshot" });
  
  private static final Map<String, Map<String, QuerySchema.PropertyInfo>> MANAGED_ENTITY_TO_NON_VMODL_PROPERTIES = new LinkedHashMap<>();
  
  static {
    Map<String, QuerySchema.PropertyInfo> vmPropertyNameToInfo = new HashMap<>();
    vmPropertyNameToInfo.put("virtualDisks", 
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    vmPropertyNameToInfo.put("isNormalVMOrPrimaryFTVM", 
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    vmPropertyNameToInfo.put("isSecondaryVM", 
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    MANAGED_ENTITY_TO_NON_VMODL_PROPERTIES.put("VirtualMachine", vmPropertyNameToInfo);
    Map<String, QuerySchema.PropertyInfo> hostPropertyNameToInfo = new HashMap<>();
    hostPropertyNameToInfo.put("isStandalone", 
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    MANAGED_ENTITY_TO_NON_VMODL_PROPERTIES.put("HostSystem", hostPropertyNameToInfo);
    Map<String, QuerySchema.PropertyInfo> clusterPropertyNameToInfo = new HashMap<>();
    clusterPropertyNameToInfo.put("rps", QuerySchema.PropertyInfo.forNonFilterableProperty());
    clusterPropertyNameToInfo.put("storageTotalB", 
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    clusterPropertyNameToInfo.put("name", QuerySchema.PropertyInfo.forNonFilterableProperty());
    MANAGED_ENTITY_TO_NON_VMODL_PROPERTIES.put("ClusterComputeResource", clusterPropertyNameToInfo);
    Map<String, QuerySchema.PropertyInfo> hostProfilePropertyNameToInfo = new HashMap<>();
    hostProfilePropertyNameToInfo.put("name", 
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    hostProfilePropertyNameToInfo.put("complianceStatus", 
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    hostProfilePropertyNameToInfo.put("entity", 
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    MANAGED_ENTITY_TO_NON_VMODL_PROPERTIES.put("HostProfile", hostProfilePropertyNameToInfo);
  }
  
  public PcResourceModelSchemaConverter(VmodlTypeMap vmodlTypeMap) {
    super(vmodlTypeMap);
  }
  
  public QuerySchema convertSchema(VmodlVersion vmodlVersion) {
    QuerySchema querySchema = super.convertSchema(vmodlVersion);
    if (vmodlVersion == null) {
      Map<String, QuerySchema.ModelInfo> models = querySchema.getModels();
      Map<String, QuerySchema.ModelInfo> newModels = new HashMap<>(models);
      for (Map.Entry<String, Map<String, QuerySchema.PropertyInfo>> entry : MANAGED_ENTITY_TO_NON_VMODL_PROPERTIES.entrySet()) {
        String modelType = entry.getKey();
        QuerySchema.ModelInfo modelInfo = querySchema.getModels().get(modelType);
        QuerySchema.ModelInfo additionalModelInfo = new QuerySchema.ModelInfo(entry.getValue());
        if (modelInfo != null) {
          modelInfo = QuerySchema.ModelInfo.merge(Arrays.asList(new QuerySchema.ModelInfo[] { modelInfo, additionalModelInfo }));
        } else {
          modelInfo = additionalModelInfo;
        } 
        newModels.put(modelType, modelInfo);
      } 
      querySchema = QuerySchema.forModels(newModels);
    } 
    return querySchema;
  }
  
  protected Map<VmodlType, Pair<VmodlType, String>> getRetrievalRulesForServiceInstanceMOs() {
    Map<VmodlType, Pair<VmodlType, String>> managedObjectToRetrievalRules = super.getRetrievalRulesForServiceInstanceMOs();
    Map<VmodlType, Pair<VmodlType, String>> filteredManagedObjectToRetrievalRule = new LinkedHashMap<>();
    for (Map.Entry<VmodlType, Pair<VmodlType, String>> moToRetrievalRule : managedObjectToRetrievalRules.entrySet()) {
      VmodlType moVmodlType = moToRetrievalRule.getKey();
      Pair<VmodlType, String> retrievalRule = moToRetrievalRule.getValue();
      if (retrievalRule == null) {
        filteredManagedObjectToRetrievalRule.put(moVmodlType, retrievalRule);
        continue;
      } 
      if (SERVICE_INSTANCE_RELATED_MANAGED_OBJECTS.contains(moVmodlType
          .getWsdlName()))
        filteredManagedObjectToRetrievalRule.put(moVmodlType, retrievalRule); 
    } 
    return filteredManagedObjectToRetrievalRule;
  }
  
  protected Map<VmodlType, Pair<VmodlType, String>> getRetrievalRulesForInventoryMEs() {
    Map<VmodlType, Pair<VmodlType, String>> managedObjectToRetrievalRules = super.getRetrievalRulesForInventoryMEs();
    Map<VmodlType, Pair<VmodlType, String>> filteredManagedObjectToRetrievalRule = new LinkedHashMap<>();
    for (Map.Entry<VmodlType, Pair<VmodlType, String>> moToRetrievalRule : managedObjectToRetrievalRules.entrySet()) {
      VmodlType moVmodlType = moToRetrievalRule.getKey();
      Pair<VmodlType, String> retrievalRule = moToRetrievalRule.getValue();
      if (retrievalRule == null) {
        filteredManagedObjectToRetrievalRule.put(moVmodlType, retrievalRule);
        continue;
      } 
      if (INVENTORY_RELATED_MANAGED_OBJECTS_RULES.contains(moVmodlType
          .getWsdlName()))
        filteredManagedObjectToRetrievalRule.put(moVmodlType, retrievalRule); 
    } 
    return filteredManagedObjectToRetrievalRule;
  }
}

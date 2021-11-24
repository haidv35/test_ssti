package com.vmware.ph.phservice.provider.common.vmomi.pc;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class PcSchemaConverter {
  protected final VmodlTypeMap _vmodlTypeMap;
  
  private Map<VmodlType, List<Pair<VmodlType, String>>> _managedObjectToRetrievalRules;
  
  public PcSchemaConverter(VmodlTypeMap vmodlTypeMap) {
    this._vmodlTypeMap = vmodlTypeMap;
  }
  
  public QuerySchema convertSchema(VmodlVersion vmodlVersion) {
    Map<String, QuerySchema.ModelInfo> models = new LinkedHashMap<>();
    models.putAll(
        VmodlTypeToQuerySchemaModelInfoConverter.convertVmodlTypesToWsdlNameModelInfos(
          getManagedObjectToRetrievalRulesChain().keySet(), this._vmodlTypeMap, vmodlVersion));
    return QuerySchema.forModels(models);
  }
  
  public final List<Pair<VmodlType, String>> getRetrievalRulesChain(VmodlType moVmodlType) {
    return getManagedObjectToRetrievalRulesChain().get(moVmodlType);
  }
  
  protected abstract Map<VmodlType, List<Pair<VmodlType, String>>> loadManagedObjectToRetrievalRulesChain();
  
  private final Map<VmodlType, List<Pair<VmodlType, String>>> getManagedObjectToRetrievalRulesChain() {
    if (this._managedObjectToRetrievalRules == null)
      this._managedObjectToRetrievalRules = loadManagedObjectToRetrievalRulesChain(); 
    return this._managedObjectToRetrievalRules;
  }
}

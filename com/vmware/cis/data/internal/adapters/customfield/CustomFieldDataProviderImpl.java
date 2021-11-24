package com.vmware.cis.data.internal.adapters.customfield;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.ext.aggregated.AggregatedModelLookup;
import com.vmware.cis.data.internal.provider.ext.aggregated.DefaultAggregatedModels;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.vmomi.client.Client;
import org.apache.commons.lang.Validate;

public final class CustomFieldDataProviderImpl implements DataProvider {
  private final DataProvider _customFieldAssociationDataProvider;
  
  private final DataProvider _customFieldDefDataProvider;
  
  public CustomFieldDataProviderImpl(Client vlsiClient, String serverGuid) {
    VimCustomFieldsManagerRepository customFields = new VimCustomFieldsManagerRepository(vlsiClient);
    this._customFieldAssociationDataProvider = new CustomFieldAssociationDataProvider(customFields);
    AggregatedModelLookup aggregatedModels = DefaultAggregatedModels.getModelLookup();
    this._customFieldDefDataProvider = new CustomFieldDefDataProvider(customFields, serverGuid, aggregatedModels);
  }
  
  public ResultSet executeQuery(Query query) {
    Validate.notNull(query);
    if (forFieldDef(query))
      return this._customFieldDefDataProvider.executeQuery(query); 
    return this._customFieldAssociationDataProvider.executeQuery(query);
  }
  
  public QuerySchema getSchema() {
    QuerySchema fieldsAssocSchema = this._customFieldAssociationDataProvider.getSchema();
    QuerySchema fieldDefSchema = this._customFieldDefDataProvider.getSchema();
    QuerySchema mergedSchema = SchemaUtil.merge(fieldsAssocSchema, fieldDefSchema);
    return mergedSchema;
  }
  
  private boolean forFieldDef(Query query) {
    String model = query.getResourceModels().iterator().next();
    QuerySchema schema = this._customFieldDefDataProvider.getSchema();
    return schema.getModels().containsKey(model);
  }
}

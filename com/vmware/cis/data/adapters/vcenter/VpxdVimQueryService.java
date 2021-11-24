package com.vmware.cis.data.adapters.vcenter;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.QueryService;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.adapters.vmomi.impl.VimDataProvider;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.vmomi.client.Client;
import java.util.Collections;
import org.apache.commons.lang.Validate;

public final class VpxdVimQueryService {
  public static QueryService createQueryService(Client vimClient) {
    Validate.notNull(vimClient);
    DataProvider provider = new VimDataProvider(vimClient);
    QuerySchema schema = provider.getSchema();
    DataProvider dataProvider = new FixedSchemaDataProvider(provider, schema);
    return QueryService.Builder.forProviders(
        Collections.singleton(dataProvider)).build();
  }
  
  private static final class FixedSchemaDataProvider implements DataProvider {
    private final DataProvider _provider;
    
    private final QuerySchema _schema;
    
    FixedSchemaDataProvider(DataProvider provider, QuerySchema schema) {
      assert provider != null;
      assert schema != null;
      this._provider = provider;
      this._schema = schema;
    }
    
    public ResultSet executeQuery(Query query) {
      ResultSet rs = this._provider.executeQuery(query);
      return rs;
    }
    
    public QuerySchema getSchema() {
      return this._schema;
    }
  }
}

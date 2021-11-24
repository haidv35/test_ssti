package com.vmware.cis.data.internal.adapters.vapi.impl;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.adapters.util.vapi.VapiOsgiAwareStubFactory;
import com.vmware.cis.data.internal.adapters.vapi.VapiPropertyValueConverter;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.cis.data.provider.metadata.vapi.Schema;
import com.vmware.cis.data.provider.vapi.ResourceModel;
import com.vmware.cis.data.provider.vapi.ResourceModelTypes;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.std.errors.InvalidArgument;
import com.vmware.vapi.std.errors.Unauthorized;
import java.net.URI;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VapiDataProviderConnection implements DataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(VapiDataProviderConnection.class);
  
  private final VapiOsgiAwareStubFactory _stubFactory;
  
  private final Schema _schemaStub;
  
  private final ResourceModel _resourceModelStub;
  
  private final VapiDataProviderQueryConverter _queryConverter;
  
  private final VapiDataProviderResultSetConverter _resultConverter;
  
  private final URI _uri;
  
  public VapiDataProviderConnection(ApiProvider apiProvider, VapiPropertyValueConverter valueConverter, URI uri, String serverGuid, boolean unqualifyProperties) {
    assert apiProvider != null;
    assert valueConverter != null;
    assert uri != null;
    assert serverGuid != null;
    this._stubFactory = new VapiOsgiAwareStubFactory(apiProvider);
    this._resourceModelStub = this._stubFactory.<ResourceModel>createStub(ResourceModel.class);
    this._schemaStub = this._stubFactory.<Schema>createStub(Schema.class);
    this._queryConverter = new VapiDataProviderQueryConverter(valueConverter, unqualifyProperties);
    this._resultConverter = new VapiDataProviderResultSetConverter(valueConverter, serverGuid, unqualifyProperties);
    this._uri = uri;
  }
  
  public ResultSet executeQuery(Query query) {
    ResourceModelTypes.ResultSet vapiResult;
    Validate.notNull(query);
    ResourceModelTypes.QuerySpec vapiQuery = this._queryConverter.convertQuery(query);
    _logger.trace("Sending query to vAPI data provider: {}", query);
    try {
      vapiResult = this._resourceModelStub.query(vapiQuery);
    } catch (InvalidArgument ex) {
      throw new IllegalArgumentException("Invalid query", ex);
    } catch (Unauthorized ex) {
      _logger.trace("Return empty result because user is unauthorized");
      return ResultSet.Builder.properties(query.getProperties())
        .totalCount(query.getWithTotalCount() ? Integer.valueOf(0) : null)
        .build();
    } 
    _logger.trace("Received response from vAPI data provider: {}", vapiResult);
    return this._resultConverter.convertResultSet(vapiResult, query
        .getProperties(), query
        .getOffset(), query
        .getLimit(), query
        .getWithTotalCount());
  }
  
  public QuerySchema getSchema() {
    try {
      _logger.trace("Requesting schema from vAPI data provider");
      QuerySchema schema = VapiDataProviderSchemaConverter.convertSchema(this._schemaStub.get());
      _logger.trace("Received schema from vAPI data provider: {}", schema);
      return schema;
    } catch (Unauthorized ex) {
      _logger.info("User is unauthorized to query data from {}", this._uri);
      return QuerySchema.EMPTY_SCHEMA;
    } 
  }
  
  public String toString() {
    return "VapiConnection(url=" + this._uri + ")";
  }
}

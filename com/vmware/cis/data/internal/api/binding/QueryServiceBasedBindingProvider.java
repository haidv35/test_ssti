package com.vmware.cis.data.internal.api.binding;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.QueryService;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.ext.ConnectionSupplier;
import com.vmware.cis.data.internal.provider.ext.alias.AliasPropertyDescriptor;
import com.vmware.cis.data.internal.provider.ext.alias.AliasPropertyProviderConnection;
import com.vmware.cis.data.internal.provider.ext.alias.AliasPropertyRepository;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyDescriptor;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyProviderConnection;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyRepository;
import com.vmware.cis.data.internal.provider.profiler.QueryIdLogConfigurator;
import com.vmware.cis.data.internal.provider.util.QueryCommandUtil;
import com.vmware.cis.data.internal.provider.util.QueryQualifier;
import com.vmware.cis.data.provider.DataProvider;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryServiceBasedBindingProvider implements QueryBindingProvider {
  private static final String NESTED_QUERY_BINDING_PREFIX = "nqb-";
  
  private final QueryBindingDescriptor _descriptor;
  
  private final DataProvider _dataProvider;
  
  public QueryServiceBasedBindingProvider(QueryService queryService, QueryBindingDescriptor descriptor) {
    assert queryService != null;
    assert descriptor != null;
    this._descriptor = descriptor;
    this
      ._dataProvider = (new ExtensionConnectionSupplier(toDataProvider(queryService), descriptor)).getConnection();
  }
  
  public Collection<?> fetch(Collection<?> keys) {
    PropertyPredicate predicate;
    ResultSet resultSet;
    assert keys != null;
    assert !keys.isEmpty();
    List<String> selectedProperties = this._descriptor.getPropertiesToSelect();
    Collection<String> resourceModels = QueryQualifier.getFromClause(selectedProperties, null, null);
    if (keys.size() == 1) {
      Object key = keys.iterator().next();
      predicate = new PropertyPredicate("@modelKey", PropertyPredicate.ComparisonOperator.EQUAL, key);
    } else {
      predicate = new PropertyPredicate("@modelKey", PropertyPredicate.ComparisonOperator.IN, keys);
    } 
    Query query = Query.Builder.select(selectedProperties).from(resourceModels).where(new PropertyPredicate[] { predicate }).build();
    try {
      resultSet = this._dataProvider.executeQuery(query);
    } catch (RuntimeException cause) {
      String typeName = this._descriptor.getType().getCanonicalName();
      throw new RuntimeException("Could not fetch query binding: " + typeName, cause);
    } 
    DataProvider countingProvider = QueryIdLogConfigurator.withQueryCounter(this._dataProvider, "nqb-");
    List<?> instances = this._descriptor.map(countingProvider, resultSet.getItems());
    return instances;
  }
  
  private DataProvider toDataProvider(final QueryService queryService) {
    return new DataProvider() {
        public QuerySchema getSchema() {
          return queryService.getSchema();
        }
        
        public ResultSet executeQuery(Query query) {
          return QueryCommandUtil.toCommand(queryService, query).fetch();
        }
      };
  }
  
  private static final class ExtensionConnectionSupplier implements ConnectionSupplier {
    private final DataProvider _dataProvider;
    
    public ExtensionConnectionSupplier(DataProvider dataProvider, QueryBindingDescriptor descriptor) {
      assert dataProvider != null;
      assert descriptor != null;
      this._dataProvider = aliasProvider(relatedProvider(dataProvider, this, descriptor), descriptor);
    }
    
    public DataProvider getConnection() {
      return this._dataProvider;
    }
    
    private static DataProvider aliasProvider(DataProvider dataProvider, QueryBindingDescriptor descriptor) {
      Class<?> bindingType = descriptor.getType();
      List<AliasPropertyDescriptor> aliasDescriptors = descriptor.getAliasPropertyDescriptors();
      AliasPropertyRepository repository = new AliasPropertyRepository(mapByAliasProperty(bindingType, aliasDescriptors));
      return new AliasPropertyProviderConnection(dataProvider, repository);
    }
    
    private static DataProvider relatedProvider(DataProvider dataProvider, ConnectionSupplier supplier, QueryBindingDescriptor descriptor) {
      Class<?> bindingType = descriptor.getType();
      List<RelatedPropertyDescriptor> relatedDescriptors = descriptor.getRelatedPropertyDescriptors();
      RelatedPropertyRepository repository = new RelatedPropertyRepository(mapByRelatedProperty(bindingType, relatedDescriptors));
      return new RelatedPropertyProviderConnection(dataProvider, supplier, repository);
    }
    
    private static Map<String, AliasPropertyDescriptor> mapByAliasProperty(Class<?> bindingType, List<AliasPropertyDescriptor> descriptors) {
      Map<String, AliasPropertyDescriptor> descriptorByAliasProperty = new HashMap<>();
      for (AliasPropertyDescriptor descriptor : descriptors) {
        String property = descriptor.getName();
        AliasPropertyDescriptor oldDescriptor = descriptorByAliasProperty.put(property, descriptor);
        if (oldDescriptor != null && !oldDescriptor.equals(descriptor)) {
          String msg = String.format("'%s' contains multiple alias paths for the same property: %s", new Object[] { bindingType
                .getCanonicalName(), property });
          throw new IllegalArgumentException(msg);
        } 
      } 
      return descriptorByAliasProperty;
    }
    
    private static Map<String, RelatedPropertyDescriptor> mapByRelatedProperty(Class<?> bindingType, List<RelatedPropertyDescriptor> descriptors) {
      Map<String, RelatedPropertyDescriptor> descriptorByRelatedProperty = new HashMap<>();
      for (RelatedPropertyDescriptor descriptor : descriptors) {
        String property = descriptor.getName();
        RelatedPropertyDescriptor oldDescriptor = descriptorByRelatedProperty.put(property, descriptor);
        if (oldDescriptor != null && !oldDescriptor.equals(descriptor)) {
          String msg = String.format("'%s' contains multiple related paths for the same property: %s", new Object[] { bindingType
                .getCanonicalName(), property });
          throw new IllegalArgumentException(msg);
        } 
      } 
      return descriptorByRelatedProperty;
    }
  }
}

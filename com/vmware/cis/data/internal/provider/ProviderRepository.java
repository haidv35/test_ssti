package com.vmware.cis.data.internal.provider;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProviderRepository implements ProviderBySchemaLookup {
  private static Logger _logger = LoggerFactory.getLogger(ProviderRepository.class);
  
  private final Map<String, DataProvider> _providerByProperty;
  
  private final Map<String, DataProvider> _providerByModel;
  
  private final QuerySchema _schema;
  
  private ProviderRepository(Map<String, DataProvider> providerByProperty, Map<String, DataProvider> providerByModel, QuerySchema schema) {
    this._providerByProperty = Collections.unmodifiableMap(providerByProperty);
    this._providerByModel = Collections.unmodifiableMap(providerByModel);
    this._schema = schema;
  }
  
  public static ProviderBySchemaLookup forProviders(Collection<DataProvider> providers) {
    _logger.debug("Creating lookup for providers: {}", providers);
    Map<DataProvider, QuerySchema> schemaByProvider = new LinkedHashMap<>(providers.size());
    for (DataProvider provider : providers) {
      try {
        QuerySchema schema = provider.getSchema();
        schemaByProvider.put(provider, schema);
      } catch (Exception e) {
        String providerAsString = provider.toString();
        _logger.error("Ignore data provider '{}' because it failed", providerAsString, e);
      } 
    } 
    return new ProviderRepository(toProviderByProperty(schemaByProvider), 
        toProviderByModel(schemaByProvider), 
        toSchema(toModelsByName(schemaByProvider)));
  }
  
  public DataProvider getProviderForProperty(String property) {
    Validate.notEmpty(property, "property");
    return getProviderForProperty(this._providerByProperty, property);
  }
  
  public DataProvider getProviderForProperties(Collection<String> properties) {
    Validate.notEmpty(properties, "properties");
    return getProviderForProperties(properties, this._providerByProperty);
  }
  
  private DataProvider getProviderForProperties(Collection<String> properties, Map<String, DataProvider> providerByProperty) {
    DataProvider provider = null;
    for (String property : properties) {
      DataProvider currentProvider = getProviderForProperty(providerByProperty, property);
      if (provider == null)
        provider = currentProvider; 
      if (currentProvider == null || currentProvider != provider)
        return null; 
    } 
    return provider;
  }
  
  private static Map<String, DataProvider> toProviderByProperty(Map<DataProvider, QuerySchema> schemaByProvider) {
    Map<String, DataProvider> providerByProperty = new HashMap<>();
    for (Map.Entry<DataProvider, QuerySchema> e : schemaByProvider.entrySet()) {
      DataProvider provider = e.getKey();
      QuerySchema schema = e.getValue();
      for (Map.Entry<String, QuerySchema.ModelInfo> modelEntry : schema.getModels()
        .entrySet()) {
        String modelName = modelEntry.getKey();
        QuerySchema.ModelInfo modelInfo = modelEntry.getValue();
        registerDataProvider(provider, modelName, modelInfo, providerByProperty);
      } 
    } 
    return providerByProperty;
  }
  
  private static Map<String, DataProvider> toProviderByModel(Map<DataProvider, QuerySchema> schemaByProvider) {
    Map<String, DataProvider> providerByModel = new HashMap<>();
    for (Map.Entry<DataProvider, QuerySchema> e : schemaByProvider.entrySet()) {
      DataProvider provider = e.getKey();
      QuerySchema schema = e.getValue();
      for (Map.Entry<String, QuerySchema.ModelInfo> modelEntry : schema.getModels()
        .entrySet()) {
        String modelName = modelEntry.getKey();
        if (!providerByModel.containsKey(modelName))
          providerByModel.put(modelName, provider); 
      } 
    } 
    return providerByModel;
  }
  
  private static Map<String, Collection<QuerySchema.ModelInfo>> toModelsByName(Map<DataProvider, QuerySchema> schemaByProvider) {
    Map<String, Collection<QuerySchema.ModelInfo>> modelsByName = new HashMap<>();
    for (Map.Entry<DataProvider, QuerySchema> e : schemaByProvider.entrySet()) {
      QuerySchema schema = e.getValue();
      for (Map.Entry<String, QuerySchema.ModelInfo> modelEntry : schema.getModels()
        .entrySet()) {
        String modelName = modelEntry.getKey();
        QuerySchema.ModelInfo modelInfo = modelEntry.getValue();
        Collection<QuerySchema.ModelInfo> models = modelsByName.get(modelName);
        if (models == null) {
          models = new ArrayList<>();
          modelsByName.put(modelName, models);
        } 
        models.add(modelInfo);
      } 
    } 
    return modelsByName;
  }
  
  private static void registerDataProvider(DataProvider provider, String modelName, QuerySchema.ModelInfo modelInfo, Map<String, DataProvider> providerByProperty) {
    for (Map.Entry<String, QuerySchema.PropertyInfo> propertyEntry : modelInfo
      .getProperties().entrySet()) {
      String propertyName = propertyEntry.getKey();
      QuerySchema.PropertyInfo propertyInfo = propertyEntry.getValue();
      registerDataProvider(provider, modelName, propertyName, propertyInfo, providerByProperty);
    } 
  }
  
  private static void registerDataProvider(DataProvider provider, String modelName, String propertyName, QuerySchema.PropertyInfo propertyInfo, Map<String, DataProvider> providerByProperty) {
    if ("@instanceUuid".equals(propertyName) || "@modelKey"
      .equals(propertyName) || "@type"
      .equals(propertyName))
      return; 
    String property = QualifiedProperty.forModelAndSimpleProperty(modelName, propertyName).toString();
    DataProvider old = providerByProperty.put(property, provider);
    if (old != null) {
      String msg = String.format("Provider %s overrides property '%s' defined by provider %s", new Object[] { provider, property, old });
      throw new IllegalArgumentException(msg);
    } 
  }
  
  public QuerySchema getSchema() {
    return this._schema;
  }
  
  private static QuerySchema toSchema(Map<String, Collection<QuerySchema.ModelInfo>> modelsByName) {
    Map<String, QuerySchema.ModelInfo> models = new HashMap<>(modelsByName.size());
    for (Map.Entry<String, Collection<QuerySchema.ModelInfo>> e : modelsByName.entrySet()) {
      QuerySchema.ModelInfo modelInfo = QuerySchema.ModelInfo.merge(e.getValue());
      models.put(e.getKey(), modelInfo);
    } 
    return QuerySchema.forModels(models);
  }
  
  private DataProvider getProviderForProperty(Map<String, DataProvider> providers, String property) {
    if (PropertyUtil.isModelKey(property))
      throw new IllegalArgumentException("Can not determine adapter for model key without resource models"); 
    if (PropertyUtil.isInstanceUuid(property))
      return this._providerByModel.get(
          QualifiedProperty.forQualifiedName(property).getResourceModel()); 
    DataProvider dataProvider = providers.get(
        QualifiedProperty.getRootProperty(property));
    if (dataProvider == null)
      throw new IllegalArgumentException("No data adapter for property " + property); 
    return dataProvider;
  }
  
  public DataProvider getProviderForModel(String model) {
    Validate.notEmpty(model, "model");
    DataProvider dataProvider = this._providerByModel.get(model);
    if (dataProvider == null)
      throw new IllegalArgumentException("There is no registered provider  for model " + model); 
    return dataProvider;
  }
  
  public DataProvider getProviderForModels(Collection<String> models) {
    Validate.notEmpty(models);
    DataProvider currentProvider = null;
    for (String model : models) {
      DataProvider dataProvider = getProviderForModel(model);
      if (currentProvider == null)
        currentProvider = dataProvider; 
      if (currentProvider != dataProvider)
        return null; 
    } 
    return currentProvider;
  }
}

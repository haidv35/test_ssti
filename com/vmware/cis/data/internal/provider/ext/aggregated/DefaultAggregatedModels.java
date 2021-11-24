package com.vmware.cis.data.internal.provider.ext.aggregated;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang.Validate;

public final class DefaultAggregatedModels {
  private static final AggregatedModelLookup DEFAULT_AGGREGATION_MODULES = loadFromClasspath("/default-aggregated-models.properties");
  
  public static AggregatedModelLookup getModelLookup() {
    return DEFAULT_AGGREGATION_MODULES;
  }
  
  private static AggregatedModelLookup loadFromClasspath(String classpathResource) {
    Validate.notEmpty(classpathResource);
    InputStream input = DefaultAggregatedModels.class.getResourceAsStream(classpathResource);
    if (input == null)
      throw new IllegalArgumentException("Could not find classpath resource" + classpathResource); 
    Properties props = new Properties();
    try {
      props.load(input);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Error while loading classpath resource " + classpathResource, ex);
    } finally {
      try {
        input.close();
      } catch (IOException iOException) {}
    } 
    return loadFromProperties(props);
  }
  
  private static AggregatedModelLookup loadFromProperties(Properties props) {
    assert props != null;
    Set<String> propertyNames = props.stringPropertyNames();
    Map<String, Set<String>> childModelsByAggregatedModel = new HashMap<>(propertyNames.size());
    for (String aggregatedModel : propertyNames) {
      Validate.notEmpty(aggregatedModel, "Empty name of aggregated model");
      String value = props.getProperty(aggregatedModel, "").trim();
      Validate.notEmpty(value, "Empty child model list for aggregated model " + aggregatedModel);
      Set<String> childModels = parseModelsFromPropertyValue(value);
      childModelsByAggregatedModel.put(aggregatedModel, childModels);
    } 
    return new MapBasedAggregatedModelLookup(childModelsByAggregatedModel);
  }
  
  private static Set<String> parseModelsFromPropertyValue(String value) {
    assert value != null;
    Set<String> models = new HashSet<>();
    for (String token : value.split(",")) {
      String model = token.trim();
      Validate.notEmpty(model, "Empty name of child model");
      models.add(model);
    } 
    return models;
  }
}

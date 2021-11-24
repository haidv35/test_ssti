package com.vmware.ph.phservice.collector.internal.cdf.mapping;

import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityPatternEvaluator;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityPatternEvaluatorFactory;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.ph.phservice.provider.common.internal.Context;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResourceItemToFreeFormDataMapping implements Mapping<NamedPropertiesResourceItem, Collection<JsonLd>> {
  private static final Log _logger = LogFactory.getLog(ResourceItemToFreeFormDataMapping.class);
  
  private String _idPattern;
  
  private String _resourceType;
  
  private String _dataProperty;
  
  private VelocityPatternEvaluatorFactory _velocityPatternEvaluatorFactory;
  
  public ResourceItemToFreeFormDataMapping(String idPattern, String resourceType, String dataProperty) {
    this(idPattern, resourceType, dataProperty, new VelocityPatternEvaluatorFactory());
  }
  
  public ResourceItemToFreeFormDataMapping(String idPattern, String resourceType, String dataProperty, VelocityPatternEvaluatorFactory velocityPatternEvaluatorFactory) {
    this._idPattern = idPattern;
    this._resourceType = resourceType;
    this._dataProperty = dataProperty;
    this._velocityPatternEvaluatorFactory = velocityPatternEvaluatorFactory;
  }
  
  public Collection<JsonLd> map(NamedPropertiesResourceItem input, Context parentContext) {
    Map<String, Object> resourceItemDictionary = createResourceItemDictionary(input);
    String idValue = evaluateIdPattern(resourceItemDictionary);
    if (idValue == null) {
      _logger.info(
          String.format("Skipping current ResultItem, because its idPattern [%s] was evaluated to null.", new Object[] { this._idPattern }));
      return Collections.emptySet();
    } 
    Set<JsonLd> results = buildJsonLdsFromDataValues(idValue, resourceItemDictionary);
    if (results.isEmpty()) {
      _logger.debug("No data values are available. Creating default empty free-form resource.");
      results = new HashSet<>(1);
      try {
        results.add(createJsonLdWithoutDataValues(idValue));
      } catch (IOException e) {
        _logger.error("Failed to create default empty free-form resource. No free-form data is mapped.");
      } 
    } 
    return Collections.unmodifiableSet(results);
  }
  
  private static Map<String, Object> createResourceItemDictionary(NamedPropertiesResourceItem input) {
    return ResourceItemToPhResourceMapping.createPropertiesMap(input);
  }
  
  private String evaluateIdPattern(Map<String, Object> resourceItemDictionary) {
    VelocityPatternEvaluator velocityPatternEvaluator = this._velocityPatternEvaluatorFactory.create(resourceItemDictionary);
    return velocityPatternEvaluator.evaluateMappingPattern(this._idPattern, this._idPattern);
  }
  
  private Set<JsonLd> buildJsonLdsFromDataValues(String contextId, Map<String, Object> resourceItemDictionary) {
    Object dataValueObj = resourceItemDictionary.get(this._dataProperty);
    if (dataValueObj == null) {
      _logger.warn(
          String.format("Freeform data property [%s] is missing in query result.", new Object[] { this._dataProperty }));
      return Collections.emptySet();
    } 
    if (!Iterable.class.isAssignableFrom(dataValueObj.getClass())) {
      _logger.warn(
          String.format("Freeform data property [%s] should be an iterable of array. Cannot evaluate property.", new Object[] { this._dataProperty }));
      return Collections.emptySet();
    } 
    Iterable<Object[]> dataValuesIterable = (Iterable<Object[]>)dataValueObj;
    Spliterator<Object[]> dataValuesSpliterator = dataValuesIterable.spliterator();
    Set<JsonLd> freeformJsonLds = (Set<JsonLd>)StreamSupport.<Object[]>stream(dataValuesSpliterator, false).map(dataValues -> {
          JsonLd dataResource = null;
          try {
            dataResource = (new JsonLd.Builder()).withType(this._resourceType).withProperty("context_id", contextId).withProperties(convertObjectValuesToJsonPropertiesMap(dataValues)).build();
          } catch (IOException e) {
            _logger.error("Failed to create Json-Ld data object for free-form resource " + this._resourceType + " and context id " + contextId, e);
          } 
          return dataResource;
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    return freeformJsonLds;
  }
  
  private JsonLd createJsonLdWithoutDataValues(String contextId) throws IOException {
    return (new JsonLd.Builder())
      .withType(this._resourceType)
      .withProperty("context_id", contextId)
      .build();
  }
  
  private Map<String, Object> convertObjectValuesToJsonPropertiesMap(Object[] objectValues) {
    Map<String, Object> jsonProperties = new HashMap<>();
    for (int i = 0; i < objectValues.length; i++)
      jsonProperties.put("_" + (i + 1), String.valueOf(objectValues[i])); 
    return jsonProperties;
  }
  
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(obj, this);
  }
}

package com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity;

import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.QueryResultToDictionaryConverter;
import com.vmware.ph.phservice.provider.common.internal.Context;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class VelocityResourceMapping<T> implements Mapping<T, Collection<JsonLd>>, QueryResultToDictionaryConverter<T> {
  public static final Log logger = LogFactory.getLog(VelocityResourceMapping.class);
  
  private static final String ARRAY_ATTRIBUTE_SUFFIX = "__array";
  
  private static final String JSON_OBJECT_ID_PROPERTY_NAME = "id";
  
  private transient Payload.Builder _payloadBuilder = new Payload.Builder();
  
  private transient VelocityPatternEvaluatorFactory _velocityPatternEvaluatorFactory;
  
  private final String _idPattern;
  
  private final Map<String, String> _attributePatterns;
  
  private String _resourceType = "";
  
  private Map<String, String> _relationPatterns = new HashMap<>();
  
  public VelocityResourceMapping(String resourceType, String idPattern, Map<String, String> attributePatterns, Map<String, String> relationPatterns, VelocityPatternEvaluatorFactory velocityPatternEvaluatorFactory) {
    this._resourceType = resourceType;
    this._idPattern = idPattern;
    this._attributePatterns = attributePatterns;
    this._relationPatterns = relationPatterns;
    this._velocityPatternEvaluatorFactory = velocityPatternEvaluatorFactory;
  }
  
  public Payload getPayload() {
    Payload payload = this._payloadBuilder.build();
    if (logger.isTraceEnabled())
      logger.trace(String.format("Returning payload with. Json-Lds total count is %d", new Object[] { Integer.valueOf(payload.getJsons().size()) })); 
    return payload;
  }
  
  public Collection<JsonLd> map(T input, Context parentContext) {
    this._payloadBuilder = new Payload.Builder();
    Map<String, Object> velocityDictionary = createDictionary(input);
    if (velocityDictionary == null)
      return Collections.emptyList(); 
    Map<String, Object> objectsToAdd = new HashMap<>();
    objectsToAdd.putAll((Map<? extends String, ?>)parentContext);
    objectsToAdd.putAll(velocityDictionary);
    VelocityPatternEvaluator velocityPatternEvaluator = this._velocityPatternEvaluatorFactory.create(objectsToAdd, this._payloadBuilder);
    String id = velocityPatternEvaluator.evaluateMappingPattern(this._idPattern, "idPattern");
    Set<JsonLd> mappedResults = Collections.emptySet();
    if (!StringUtils.isBlank(id)) {
      Map<String, Object> attributes = velocityPatternEvaluator.evaluateAttributePatterns(this._attributePatterns);
      Map<String, String> relations = velocityPatternEvaluator.evaluateMultipleMappingPatterns(this._relationPatterns);
      if (isJsonArrayType()) {
        mappedResults = createJsonLdsFromJsonArrayAttribute(attributes, relations);
      } else {
        mappedResults = new HashSet<>(1);
        try {
          mappedResults.add((new JsonLd.Builder())
              
              .withId(id)
              .withType(this._resourceType)
              .withProperties(attributes)
              .withProperties(relations)
              .build());
        } catch (IOException e) {
          logger.error("Failed to create velocity mapping for resource type " + this._resourceType, e);
        } 
      } 
    } 
    return Collections.unmodifiableSet(mappedResults);
  }
  
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(obj, this);
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  
  private boolean isJsonArrayType() {
    return this._resourceType.endsWith("__array");
  }
  
  private Set<JsonLd> createJsonLdsFromJsonArrayAttribute(Map<String, Object> attributes, Map<String, String> relations) {
    if (attributes.isEmpty())
      return Collections.emptySet(); 
    String jsonArrayAttributeKey = attributes.keySet().iterator().next();
    String jsonArrayString = attributes.get(jsonArrayAttributeKey).toString();
    String resourceType = this._resourceType.replace("__array", "");
    JSONArray jsonArray = new JSONArray(jsonArrayString);
    Set<JsonLd> jsonLds = (Set<JsonLd>)StreamSupport.stream(jsonArray.spliterator(), false).map(jsonObject -> createSingleJsonLd(resourceType, (JSONObject)jsonObject, relations)).filter(Objects::nonNull).collect(Collectors.toSet());
    return jsonLds;
  }
  
  private JsonLd createSingleJsonLd(String type, JSONObject jsonObject, Map<String, String> relations) {
    if (StringUtils.isBlank(type)) {
      logger.error("Missing mandatory type for object: " + jsonObject);
      return null;
    } 
    String id = jsonObject.optString("id");
    if (StringUtils.isBlank(id)) {
      logger.error("Missing mandatory 'id' property in object: " + jsonObject);
      return null;
    } 
    Map<String, Object> properties = new HashMap<>(jsonObject.toMap());
    properties.remove("id");
    JsonLd jsonLd = null;
    try {
      jsonLd = (new JsonLd.Builder()).withId(id).withType(type).withProperties(properties).withProperties(relations).build();
    } catch (IOException e) {
      logger.error("Failed to create Json-Ld object for type " + type, e);
    } 
    return jsonLd;
  }
}

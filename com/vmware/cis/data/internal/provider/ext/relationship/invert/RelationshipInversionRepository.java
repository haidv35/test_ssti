package com.vmware.cis.data.internal.provider.ext.relationship.invert;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang.Validate;

public final class RelationshipInversionRepository implements RelationshipInversionLookup {
  private static final RelationshipInversionRepository DEFAULT_REL_INVERSIONS = loadFromClasspath("/default-relationship-inversions.properties");
  
  private final Map<String, Collection<String>> _inversesByRelationship;
  
  public static RelationshipInversionRepository getDefaultRelationshipInversions() {
    return DEFAULT_REL_INVERSIONS;
  }
  
  public RelationshipInversionRepository(Map<String, Collection<String>> inversesByRelationship) {
    assert inversesByRelationship != null;
    this._inversesByRelationship = Collections.unmodifiableMap(inversesByRelationship);
  }
  
  public Collection<String> invert(String property) {
    assert property != null;
    Collection<String> inverses = this._inversesByRelationship.get(property);
    if (inverses == null)
      return Collections.emptyList(); 
    return inverses;
  }
  
  public QuerySchema addInvertibleRelationships(QuerySchema schema) {
    assert schema != null;
    Map<String, QuerySchema.PropertyInfo> infoByProperty = new LinkedHashMap<>();
    for (String rel : this._inversesByRelationship.keySet()) {
      Collection<String> inverses = this._inversesByRelationship.get(rel);
      if (areSupported(schema, inverses))
        infoByProperty.put(rel, QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID)); 
    } 
    QuerySchema relationshipSchema = QuerySchema.forProperties(infoByProperty);
    return SchemaUtil.merge(schema, relationshipSchema);
  }
  
  private static boolean areSupported(QuerySchema schema, Collection<String> properties) {
    assert schema != null;
    assert properties != null;
    for (String property : properties) {
      if (!isSupported(schema, property))
        return false; 
    } 
    return true;
  }
  
  private static boolean isSupported(QuerySchema schema, String property) {
    assert schema != null;
    assert property != null;
    String model = QualifiedProperty.forQualifiedName(property).getResourceModel();
    boolean supported = schema.getModels().containsKey(model);
    return supported;
  }
  
  private static RelationshipInversionRepository loadFromClasspath(String classpathResource) {
    Validate.notEmpty(classpathResource);
    InputStream input = RelationshipInversionRepository.class.getResourceAsStream(classpathResource);
    if (input == null)
      throw new IllegalArgumentException("Could not find classpath resource: " + classpathResource); 
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
  
  private static RelationshipInversionRepository loadFromProperties(Properties props) {
    assert props != null;
    Set<String> relationships = props.stringPropertyNames();
    Map<String, Collection<String>> inversesByRelationship = new LinkedHashMap<>(relationships.size());
    for (String relationship : relationships) {
      validateRelationshipName(relationship);
      String value = props.getProperty(relationship, "").trim();
      Validate.notEmpty(value, "Empty list of inverses for relationship: " + relationship);
      Collection<String> inverses = parseInverses(value);
      inversesByRelationship.put(relationship, inverses);
    } 
    return new RelationshipInversionRepository(inversesByRelationship);
  }
  
  private static Collection<String> parseInverses(String text) {
    assert text != null;
    Set<String> inverses = new LinkedHashSet<>();
    for (String token : text.split(",")) {
      String inverseRelationship = token.trim();
      validateRelationshipName(inverseRelationship);
      inverses.add(inverseRelationship);
    } 
    return inverses;
  }
  
  private static void validateRelationshipName(String relationship) {
    Validate.notEmpty(relationship, "Empty relationship name");
    try {
      QualifiedProperty.forQualifiedName(relationship);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("Invalid relationship name: " + relationship, ex);
    } 
  }
}

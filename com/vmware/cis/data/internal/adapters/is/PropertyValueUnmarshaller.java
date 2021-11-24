package com.vmware.cis.data.internal.adapters.is;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.util.PropertyUtil;
import org.apache.commons.lang.StringUtils;

final class PropertyValueUnmarshaller {
  private static final Unmarshaller STRING_UNMARSHALLER = new StringUnmarshaller();
  
  private static final Unmarshaller INTEGER_UNMARSHALLER = new IntegerUnmarshaller();
  
  private static final Unmarshaller LONG_UNMARSHALLER = new LongUnmarshaller();
  
  private static final Unmarshaller BOOLEAN_UNMARSHALLER = new BooleanUnmarshaller();
  
  private static final Unmarshaller MOREF_UNMARSHALLER = new MoRefUnmarshaller();
  
  public Object unmarshal(String model, String property, String value) {
    Unmarshaller unmarshaller = getUnmarshaller(model, property);
    try {
      return unmarshaller.unmarshal(value);
    } catch (RuntimeException e) {
      String message = String.format("Failed to parse %s to %s for property '%s' of nodel '%s'", new Object[] { value, unmarshaller
            .getType(), property, model });
      throw new RuntimeException(message, e);
    } 
  }
  
  public boolean hasMultipleCardinality(String model, String property) {
    XQueryDefinitions.PropertyDefinition propertyDefinition = XQueryDefinitions.getPropertyDefinition(model, property);
    return propertyDefinition.hasMultipleCardinality();
  }
  
  private Unmarshaller getUnmarshaller(String model, String property) {
    XQueryDefinitions.PropertyDefinition propertyDefinition = XQueryDefinitions.getPropertyDefinition(model, property);
    if (propertyDefinition == null)
      return getDefaultUnmarshaller(property); 
    QuerySchema.PropertyType type = propertyDefinition.getType();
    switch (type) {
      case INT:
        return INTEGER_UNMARSHALLER;
      case LONG:
        return LONG_UNMARSHALLER;
      case BOOLEAN:
        return BOOLEAN_UNMARSHALLER;
      case ID:
        return MOREF_UNMARSHALLER;
      case STRING:
        return STRING_UNMARSHALLER;
    } 
    throw new IllegalArgumentException("Type " + type + " not supported for unmarshalling on property '" + property + "' of model '" + model + "'");
  }
  
  private Unmarshaller getDefaultUnmarshaller(String property) {
    if (PropertyUtil.isModelKey(property))
      return MOREF_UNMARSHALLER; 
    return STRING_UNMARSHALLER;
  }
  
  private static class StringUnmarshaller implements Unmarshaller {
    private StringUnmarshaller() {}
    
    public QuerySchema.PropertyType getType() {
      return QuerySchema.PropertyType.STRING;
    }
    
    public Object unmarshal(String value) {
      return value;
    }
  }
  
  private static class BooleanUnmarshaller implements Unmarshaller {
    private BooleanUnmarshaller() {}
    
    public QuerySchema.PropertyType getType() {
      return QuerySchema.PropertyType.BOOLEAN;
    }
    
    public Object unmarshal(String value) {
      if (StringUtils.isEmpty(value))
        return null; 
      return Boolean.valueOf(Boolean.parseBoolean(value));
    }
  }
  
  private static class IntegerUnmarshaller implements Unmarshaller {
    private IntegerUnmarshaller() {}
    
    public QuerySchema.PropertyType getType() {
      return QuerySchema.PropertyType.INT;
    }
    
    public Object unmarshal(String value) {
      if (StringUtils.isEmpty(value))
        return null; 
      return Integer.valueOf(Integer.parseInt(value));
    }
  }
  
  private static class LongUnmarshaller implements Unmarshaller {
    private LongUnmarshaller() {}
    
    public QuerySchema.PropertyType getType() {
      return QuerySchema.PropertyType.LONG;
    }
    
    public Object unmarshal(String value) {
      if (StringUtils.isEmpty(value))
        return null; 
      return Long.valueOf(Long.parseLong(value));
    }
  }
  
  private static class MoRefUnmarshaller implements Unmarshaller {
    private MoRefUnmarshaller() {}
    
    public QuerySchema.PropertyType getType() {
      return QuerySchema.PropertyType.ID;
    }
    
    public Object unmarshal(String value) {
      if (StringUtils.isEmpty(value))
        return null; 
      return XQueryUtil.toMoR(value);
    }
  }
  
  private static interface Unmarshaller {
    QuerySchema.PropertyType getType();
    
    Object unmarshal(String param1String);
  }
}

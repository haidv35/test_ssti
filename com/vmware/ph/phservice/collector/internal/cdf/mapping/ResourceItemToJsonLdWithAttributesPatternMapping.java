package com.vmware.ph.phservice.collector.internal.cdf.mapping;

import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityPatternEvaluatorFactory;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResourceItemToJsonLdWithAttributesPatternMapping extends ResourceItemToJsonLdMapping implements QueryResultToDictionaryConverter<NamedPropertiesResourceItem> {
  private static final Log _log = LogFactory.getLog(ResourceItemToJsonLdWithAttributesPatternMapping.class);
  
  protected String _idPattern;
  
  protected Map<String, String> _attributePatterns;
  
  public ResourceItemToJsonLdWithAttributesPatternMapping(String forType, String mappingCode, String idPattern, Map<String, String> attributePatterns, VelocityPatternEvaluatorFactory velocityPatternEvaluatorFactory) {
    super(forType, mappingCode, velocityPatternEvaluatorFactory);
    this._idPattern = idPattern;
    this._attributePatterns = attributePatterns;
  }
  
  public Map<String, Object> createDictionary(NamedPropertiesResourceItem input) {
    Object resourceObject = input.getResourceObject();
    if (_log.isTraceEnabled())
      _log.trace("Will create dictionary with properties for mapping Query Service ResourceItem whose resourceObject class is: " + resourceObject
          
          .getClass().getCanonicalName()); 
    if (resourceObject instanceof ManagedObjectReference) {
      ManagedObjectReference moRef = (ManagedObjectReference)resourceObject;
      if (!moRef.getType().equalsIgnoreCase(this._forType)) {
        if (_log.isTraceEnabled())
          _log.trace(
              String.format("Skipping current ResourceItem, because its MOR type is %s, while current resultItemMapping is for type %s.", new Object[] { moRef.getType(), this._forType })); 
        return null;
      } 
    } else if (resourceObject instanceof URI) {
      URI uri = (URI)resourceObject;
      if (!uri.toString().toLowerCase().startsWith(this._forType.toLowerCase())) {
        if (_log.isTraceEnabled())
          _log.trace(
              String.format("Skipping current ResourceItem, because its URI type is %s, while current resultItemMapping is for type %s.", new Object[] { uri, this._forType })); 
        return null;
      } 
    } else {
      if (_log.isTraceEnabled())
        _log.trace("Skipping current ResourceItem, because its resourceObject is not of one of the supported types (ManagedObjectReference or java.net.URI).Its type is: " + resourceObject
            
            .getClass().getCanonicalName()); 
      return null;
    } 
    return createPropertiesMap(input);
  }
  
  private Map<String, Object> createPropertiesMap(NamedPropertiesResourceItem input) {
    Map<String, Object> propertiesMap = new HashMap<>();
    List<String> propertyNames = input.getActualPropertyNames();
    List<Object> propertyValues = input.getActualPropertyValues();
    for (int i = 0; i < propertyNames.size(); i++)
      propertiesMap.put(propertyNames.get(i), propertyValues.get(i)); 
    return propertiesMap;
  }
}

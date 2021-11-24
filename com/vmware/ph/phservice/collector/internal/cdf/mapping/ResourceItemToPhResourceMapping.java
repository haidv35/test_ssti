package com.vmware.ph.phservice.collector.internal.cdf.mapping;

import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityPatternEvaluatorFactory;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityResourceMapping;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResourceItemToPhResourceMapping extends VelocityResourceMapping<NamedPropertiesResourceItem> {
  private static final Log logger = LogFactory.getLog(ResourceItemToPhResourceMapping.class);
  
  private String _forType;
  
  public ResourceItemToPhResourceMapping(String forType, String resourceType, String idPattern, Map<String, String> attributePatterns, Map<String, String> relationPatterns, VelocityPatternEvaluatorFactory vePatternEvaluatorFactory) {
    super(resourceType, idPattern, attributePatterns, relationPatterns, vePatternEvaluatorFactory);
    this._forType = forType;
  }
  
  public Map<String, Object> createDictionary(NamedPropertiesResourceItem input) {
    Object resourceObject = input.getResourceObject();
    if (logger.isTraceEnabled())
      logger.trace("Will create dictionary with properties for mapping data service ResourceItem which resourceObject class is: " + resourceObject
          
          .getClass().getCanonicalName()); 
    if (resourceObject instanceof ManagedObjectReference) {
      ManagedObjectReference moRef = (ManagedObjectReference)resourceObject;
      if (!moRef.getType().equalsIgnoreCase(this._forType)) {
        if (logger.isTraceEnabled())
          logger.trace(
              String.format("Skipping current ResourceItem, because its MOR type is %s, while current resultItemMapping is for type %s.", new Object[] { moRef.getType(), this._forType })); 
        return null;
      } 
    } else if (resourceObject instanceof URI) {
      URI uri = (URI)resourceObject;
      if (!uri.toString().toLowerCase().contains(this._forType.toLowerCase())) {
        if (logger.isTraceEnabled())
          logger.trace(
              String.format("Skipping current ResourceItem, because its URI type is %s, while current resultItemMapping is for type %s.", new Object[] { uri, this._forType })); 
        return null;
      } 
    } else {
      if (logger.isTraceEnabled())
        logger.trace("Skipping current ResourceItem, because its resourceObject is not of one of the supported types (ManagedObjectReference or java.net.URI).Its type is: " + resourceObject
            
            .getClass().getCanonicalName()); 
      return null;
    } 
    return createPropertiesMap(input);
  }
  
  static Map<String, Object> createPropertiesMap(NamedPropertiesResourceItem input) {
    Object resourceObject = input.getResourceObject();
    Map<String, Object> propertiesMap = new HashMap<>();
    propertiesMap.put("resultItem-resourceObject-class", resourceObject.getClass().getCanonicalName());
    if (resourceObject instanceof ManagedObjectReference) {
      ManagedObjectReference moref = (ManagedObjectReference)resourceObject;
      propertiesMap.put("moref-serverGuid", (moref.getServerGuid() == null) ? null : moref.getServerGuid().toUpperCase());
      propertiesMap.put("moref-value", moref.getValue());
    } 
    if (resourceObject instanceof URI) {
      URI uri = (URI)resourceObject;
      propertiesMap.put("resultItem-resourceObject-uri", uri);
    } 
    List<String> propertyNames = input.getActualPropertyNames();
    List<Object> propertyValues = input.getActualPropertyValues();
    for (int i = 0; i < propertyNames.size(); i++)
      propertiesMap.put(propertyNames.get(i), propertyValues.get(i)); 
    return propertiesMap;
  }
}

package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Vmodl1ForeignKeyRouter extends AbstractForeignKeyRouter {
  private static final Logger _logger = LoggerFactory.getLogger(Vmodl1ForeignKeyRouter.class);
  
  protected final List<Object> tryExtractForeignKeysForInstanceId(PropertyPredicate predicate, String targetInstanceId) {
    String propertyName = predicate.getProperty();
    Collection<Object> values = RouterUtils.toCollection(predicate.getComparableValue());
    if (isExplicitVmodl1ForeignKey(propertyName))
      return extractExplicitFKeysForTarget(propertyName, values, targetInstanceId); 
    return tryExtractImplicitFKeysForTarget(values, targetInstanceId);
  }
  
  private List<Object> extractExplicitFKeysForTarget(String propertyName, Collection<Object> values, String targetInstanceId) {
    List<Object> keys = new ArrayList(values.size());
    for (Object value : values) {
      if (!(value instanceof String))
        throw new IllegalArgumentException(String.format("The comparableValue for %s must be string or a collection of strings.", new Object[] { propertyName })); 
      String modelKey = (String)value;
      String instanceId = StringUtils.substringAfterLast(modelKey, ":");
      if (instanceId.isEmpty()) {
        _logger.warn("VMODL1 foreign key {} in unexpected format: {}", propertyName, modelKey);
        continue;
      } 
      if (targetInstanceId.equals(instanceId))
        keys.add(modelKey); 
    } 
    return keys;
  }
  
  private List<Object> tryExtractImplicitFKeysForTarget(Collection<Object> values, String targetInstanceId) {
    List<Object> keys = new ArrayList(values.size());
    for (Object value : values) {
      if (!(value instanceof ManagedObjectReference))
        return null; 
      ManagedObjectReference foreignKey = (ManagedObjectReference)value;
      if (targetInstanceId.equals(foreignKey.getServerGuid()) || 
        Vmodl1ModelKeyRouter.isGlobalResource(foreignKey))
        keys.add(foreignKey); 
    } 
    return keys;
  }
  
  private static boolean isExplicitVmodl1ForeignKey(String propertyName) {
    if (!PropertyUtil.isSpecialProperty(propertyName))
      return QualifiedProperty.forQualifiedName(propertyName).getSimpleProperty()
        .endsWith("/@moId"); 
    return false;
  }
}

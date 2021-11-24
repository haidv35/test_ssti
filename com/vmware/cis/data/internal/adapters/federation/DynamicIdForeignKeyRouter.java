package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.vapi.std.DynamicID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public final class DynamicIdForeignKeyRouter extends AbstractForeignKeyRouter {
  protected List<Object> tryExtractForeignKeysForInstanceId(PropertyPredicate predicate, String targetInstanceId) {
    Collection<Object> values = RouterUtils.toCollection(predicate.getComparableValue());
    List<Object> keys = new ArrayList(values.size());
    for (Object value : values) {
      if (value instanceof DynamicID && isVmodl1Type(((DynamicID)value).getType())) {
        DynamicID foreignKey = (DynamicID)value;
        String instanceId = StringUtils.substringAfterLast(foreignKey.getId(), ":");
        if (StringUtils.isEmpty(instanceId))
          return null; 
        if (targetInstanceId.equals(instanceId))
          keys.add(foreignKey); 
        continue;
      } 
      return null;
    } 
    return keys;
  }
  
  private static boolean isVmodl1Type(String typename) {
    return (typename.indexOf('.') == -1);
  }
}

package com.vmware.cis.data.internal.api.binding;

import com.vmware.cis.data.internal.util.ReflectionUtil;
import com.vmware.cis.data.model.NestedQueryBinding;
import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.Relationship;
import java.lang.reflect.Field;

final class QueryBindingRelationshipValidator {
  public static void validate(Class<?> bindingType) {
    validate(bindingType, bindingType, 3);
  }
  
  private static void validate(Class<?> rootBindingType, Class<?> bindingType, int allowedRelationshipHops) {
    for (Field field : ReflectionUtil.getAllFields(bindingType)) {
      if (!field.isAnnotationPresent((Class)Relationship.class) && field
        .isAnnotationPresent((Class)NestedQueryBinding.class)) {
        validate(rootBindingType, getType(field), allowedRelationshipHops);
      } else if (field.isAnnotationPresent((Class)Relationship.class) && field
        .isAnnotationPresent((Class)NestedQueryBinding.class)) {
        validateRelationshipHops(rootBindingType, field, allowedRelationshipHops);
        validate(rootBindingType, getType(field), 
            calcRemainingRelationshipHops(field, allowedRelationshipHops));
      } else if (field.isAnnotationPresent((Class)Relationship.class) && field
        .isAnnotationPresent((Class)Property.class)) {
        validateRelationshipHops(rootBindingType, field, allowedRelationshipHops);
      } 
    } 
  }
  
  private static void validateRelationshipHops(Class<?> rootBindingType, Field field, int allowedRelationshipHops) {
    String[] relationshipHops = ((Relationship)field.<Relationship>getAnnotation(Relationship.class)).value();
    if (relationshipHops.length > allowedRelationshipHops) {
      String msg = String.format("Too many relationship hops in binding: %s. The allowed maximum level is %d!", new Object[] { rootBindingType
            .getSimpleName(), 
            Integer.valueOf(3) });
      throw new IllegalArgumentException(msg);
    } 
  }
  
  private static int calcRemainingRelationshipHops(Field field, int allowedRelationshipHops) {
    String[] relationshipHops = ((Relationship)field.<Relationship>getAnnotation(Relationship.class)).value();
    return allowedRelationshipHops - relationshipHops.length;
  }
  
  private static Class<?> getType(Field field) {
    Class<?> fieldType = field.getType();
    if (fieldType.isArray())
      fieldType = fieldType.getComponentType(); 
    return fieldType;
  }
}

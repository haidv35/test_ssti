package com.vmware.cis.data.internal.api.binding;

import com.vmware.cis.data.internal.provider.ext.alias.AliasPropertyDescriptor;
import com.vmware.cis.data.internal.provider.ext.alias.AliasPropertyRepository;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyDescriptor;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyRepository;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.internal.util.ReflectionUtil;
import com.vmware.cis.data.model.NestedQueryBinding;
import com.vmware.cis.data.model.QueryBinding;
import com.vmware.cis.data.model.Relationship;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public final class QueryBindingParser {
  public static QueryBindingDescriptor parse(Class<?> resultType) {
    QueryBindingDescriptor descriptor = toDescriptor(resultType);
    QueryBindingRelationshipValidator.validate(resultType);
    return descriptor;
  }
  
  private static QueryBindingDescriptor toDescriptor(Class<?> resultType) {
    validateBindingClass(resultType);
    QueryBinding queryModel = resultType.<QueryBinding>getAnnotation(QueryBinding.class);
    String resourceModel = queryModel.value();
    List<QueryBindingField> queryBindingFields = new ArrayList<>();
    queryBindingFields
      .addAll(collectAliasQueryBindingFields(resourceModel, resultType));
    queryBindingFields.addAll(collectRelatedQueryBindingFields(resourceModel, resultType));
    queryBindingFields.addAll(collectNestedQueryBindingFields(resultType));
    queryBindingFields.addAll(collectRelatedNestedQueryBindingFields(resourceModel, resultType));
    boolean noAnnotatedMembers = queryBindingFields.isEmpty();
    Validate.isTrue(!noAnnotatedMembers, String.format("The provided class '%s' has no annotated fields.", new Object[] { resultType
            .getSimpleName() }));
    return new QueryBindingDescriptor(resultType, resourceModel, queryBindingFields);
  }
  
  private static List<QueryBindingField> collectRelatedNestedQueryBindingFields(String resourceModel, Class<?> resultType) {
    assert resourceModel != null;
    assert resultType != null;
    List<QueryBindingField> queryBindingFields = new ArrayList<>();
    for (Field field : ReflectionUtil.getAllFields(resultType)) {
      if (field.isAnnotationPresent((Class)NestedQueryBinding.class) && (
        !field.isAnnotationPresent((Class)NestedQueryBinding.class) || field
        .isAnnotationPresent((Class)Relationship.class))) {
        QueryBindingField queryBindingField;
        try {
          queryBindingField = getNestedRelatedBindingField(resourceModel, field);
        } catch (RuntimeException ex) {
          throw new IllegalArgumentException(String.format("Invalid related nested query binding property declaration '%s' in class '%s'", new Object[] { field
                  
                  .getName(), resultType.getCanonicalName() }), ex);
        } 
        queryBindingFields.add(queryBindingField);
      } 
    } 
    return queryBindingFields;
  }
  
  private static NestedRelatedBindingField getNestedRelatedBindingField(String resourceModel, Field field) {
    Class<?> relatedPropertyType;
    assert resourceModel != null;
    assert field != null;
    Class<?> fieldType = ReflectionUtil.getType(field);
    QueryBindingDescriptor descriptor = toDescriptor(fieldType);
    QualifiedProperty targetProperty = QualifiedProperty.forModelAndSimpleProperty(descriptor
        .getResourceModel(), "@modelKey");
    String[] relationships = ((Relationship)field.<Relationship>getAnnotation(Relationship.class)).value();
    if (field.getType().isArray()) {
      relatedPropertyType = Object[].class;
    } else {
      relatedPropertyType = Object.class;
    } 
    RelatedPropertyDescriptor relatedPropertyDescriptor = RelatedPropertyDescriptor.of(resourceModel, field.getName(), relatedPropertyType, relationships, targetProperty
        .toString(), true);
    return new NestedRelatedBindingField(field, descriptor, relatedPropertyDescriptor);
  }
  
  private static List<QueryBindingField> collectNestedQueryBindingFields(Class<?> resultType) {
    assert resultType != null;
    List<QueryBindingField> queryBindingFields = new ArrayList<>();
    for (Field field : ReflectionUtil.getAllFields(resultType)) {
      if (field.isAnnotationPresent((Class)NestedQueryBinding.class) && 
        !field.isAnnotationPresent((Class)Relationship.class)) {
        QueryBindingField queryBindingField;
        try {
          queryBindingField = getNestedBindingField(field);
        } catch (RuntimeException ex) {
          throw new IllegalArgumentException(
              String.format("Invalid nested query binding property declaration '%s' in class '%s'", new Object[] { field.getName(), resultType.getCanonicalName() }), ex);
        } 
        queryBindingFields.add(queryBindingField);
      } 
    } 
    return queryBindingFields;
  }
  
  private static NestedBindingField getNestedBindingField(Field field) {
    assert field != null;
    if (field.getType().isArray())
      throw new IllegalArgumentException("Nested binding field cannot be array, unless it is a related nested binding field."); 
    QueryBindingDescriptor descriptor = toDescriptor(field.getType());
    return new NestedBindingField(field, descriptor);
  }
  
  private static List<QueryBindingField> collectRelatedQueryBindingFields(String resourceModel, Class<?> resultType) {
    assert !StringUtils.isEmpty(resourceModel);
    assert resultType != null;
    Map<Field, RelatedPropertyDescriptor> descriptorsByField = RelatedPropertyRepository.collectRelatedPropertyDescriptorByField(resourceModel, resultType, true);
    List<QueryBindingField> queryBindingFields = new ArrayList<>();
    for (Field field : descriptorsByField.keySet()) {
      RelatedPropertyDescriptor descriptor = descriptorsByField.get(field);
      queryBindingFields.add(PropertyBindingField.forRelatedProperty(resultType, field, descriptor));
    } 
    return queryBindingFields;
  }
  
  private static List<QueryBindingField> collectAliasQueryBindingFields(String resourceModel, Class<?> resultType) {
    assert !StringUtils.isEmpty(resourceModel);
    assert resultType != null;
    Map<Field, AliasPropertyDescriptor> descriptorsByField = AliasPropertyRepository.collectAliasPropertyDescriptorByField(resourceModel, resultType, true);
    List<QueryBindingField> queryBindingFields = new ArrayList<>();
    for (Field field : descriptorsByField.keySet()) {
      AliasPropertyDescriptor descriptor = descriptorsByField.get(field);
      queryBindingFields.add(PropertyBindingField.forAliasProperty(resultType, field, descriptor));
    } 
    return queryBindingFields;
  }
  
  private static void validateBindingClass(Class<?> resultType) {
    Validate.isTrue(resultType.isAnnotationPresent((Class)QueryBinding.class), "The provided class '" + resultType
        .getSimpleName() + "' is not annotated with @QueryBinding annotation.");
    QueryBinding queryModel = resultType.<QueryBinding>getAnnotation(QueryBinding.class);
    Validate.isTrue(!StringUtils.isEmpty(queryModel.value()), "The provided class '" + resultType
        .getSimpleName() + "' has an empty resource model definition.");
    Validate.isTrue(ReflectionUtil.hasDefaultConstructor(resultType), "The provided class '" + resultType
        .getSimpleName() + "' has no default constructor.");
  }
}

package com.vmware.cis.data.internal.adapters.customfield;

import com.google.common.base.Predicate;
import com.vmware.cis.data.internal.adapters.vmomi.impl.VlsiClientUtil;
import com.vmware.cis.data.internal.util.QueryMarker;
import com.vmware.vim.binding.vim.CustomFieldsManager;
import com.vmware.vim.binding.vim.ManagedEntity;
import com.vmware.vim.binding.vmodl.ManagedObject;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.core.RequestContext;
import com.vmware.vim.vmomi.core.Stub;
import com.vmware.vim.vmomi.core.impl.RequestContextImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class VimCustomFieldsManagerRepository implements CustomFieldRepository {
  private static final Logger _logger = LoggerFactory.getLogger(VimCustomFieldsManagerRepository.class);
  
  private final Client _vlsiClient;
  
  VimCustomFieldsManagerRepository(Client vlsiClient) {
    Validate.notNull(vlsiClient, "VLSI client");
    this._vlsiClient = vlsiClient;
  }
  
  public Collection<ManagedObjectReference> getEntities(Collection<String> names, String value, boolean exactMatch) {
    Validate.notNull(names);
    Validate.noNullElements(names);
    CustomFieldsManager stub = createCustomFieldsManagerStub();
    Set<ManagedObjectReference> entities = new LinkedHashSet<>();
    for (String fieldName : names)
      entities.addAll(getEntities(stub, fieldName, value, exactMatch)); 
    return entities;
  }
  
  public Collection<ManagedObjectReference> filterEntities(Collection<ManagedObjectReference> entities, Predicate<String> namePredicate, Predicate<String> valuePredicate) {
    Validate.notNull(entities, "entities");
    Validate.notNull(entities, "name predicate");
    Validate.notNull(entities, "value predicate");
    _logger.trace("Filtering entities by custom field value using {} and custom field value using {}: {}", new Object[] { namePredicate, valuePredicate, entities });
    if (entities.isEmpty())
      return entities; 
    Map<Integer, String> fieldNameByKey = getCustomFieldNameByKey(namePredicate);
    List<ManagedObjectReference> filteredEntities = new ArrayList<>();
    for (ManagedObjectReference entityRef : entities) {
      if (acceptEntity(entityRef, namePredicate, valuePredicate, fieldNameByKey))
        filteredEntities.add(entityRef); 
    } 
    return filteredEntities;
  }
  
  public Collection<String> getCustomFieldNames() {
    CustomFieldsManager stub = createCustomFieldsManagerStub();
    CustomFieldsManager.FieldDef[] fields = stub.getField();
    if (fields == null)
      return Collections.emptyList(); 
    List<String> names = new ArrayList<>(fields.length);
    for (CustomFieldsManager.FieldDef fieldDef : fields)
      names.add(fieldDef.getName()); 
    _logger.trace("Custom field names: {}", names);
    return names;
  }
  
  public Collection<CustomFieldsManager.FieldDef> getCustomFieldDefs() {
    CustomFieldsManager stub = createCustomFieldsManagerStub();
    CustomFieldsManager.FieldDef[] fields = stub.getField();
    if (fields == null)
      return Collections.emptyList(); 
    List<CustomFieldsManager.FieldDef> fieldDefsResult = new ArrayList<>(fields.length);
    for (CustomFieldsManager.FieldDef field : fields)
      fieldDefsResult.add(field); 
    return fieldDefsResult;
  }
  
  public String toString() {
    return this._vlsiClient.getBinding().getEndpointUri().toString();
  }
  
  private static Collection<ManagedObjectReference> getEntities(CustomFieldsManager stub, String name, String value, boolean exactMatch) {
    assert stub != null;
    assert name != null;
    if (_logger.isTraceEnabled())
      _logger.trace("CustomFieldsManager.getEntitiesWithCustomFieldAndValue({}, {}, {}) about to be invoked", new Object[] { name, value, 

            
            Boolean.valueOf(exactMatch) }); 
    ManagedObjectReference[] refs = stub.getEntitiesWithCustomFieldAndValue(name, value, 
        Boolean.valueOf(exactMatch));
    if (_logger.isTraceEnabled())
      _logger.trace("CustomFieldsManager.getEntitiesWithCustomFieldAndValue({}, {}, {}) returned {}", new Object[] { name, value, 
            
            Boolean.valueOf(exactMatch), 
            Arrays.toString((Object[])refs) }); 
    if (refs == null)
      return Collections.emptyList(); 
    return Arrays.asList(refs);
  }
  
  private Map<Integer, String> getCustomFieldNameByKey(Predicate<String> namePredicate) {
    assert namePredicate != null;
    CustomFieldsManager stub = createCustomFieldsManagerStub();
    CustomFieldsManager.FieldDef[] fields = stub.getField();
    if (_logger.isTraceEnabled())
      _logger.trace("Filtering field definitions using {}: {}", namePredicate, 
          Arrays.toString((Object[])fields)); 
    if (fields == null)
      return Collections.emptyMap(); 
    Map<Integer, String> fieldNameByKey = new LinkedHashMap<>(fields.length);
    for (CustomFieldsManager.FieldDef fieldDef : fields) {
      assert fieldDef.getName() != null;
      if (namePredicate.apply(fieldDef.getName())) {
        String old = fieldNameByKey.put(Integer.valueOf(fieldDef.getKey()), fieldDef.getName());
        assert old == null;
      } 
    } 
    _logger.trace("Filtered field definitions as Map<FieldKey, FieldName>: {}", fieldNameByKey);
    return fieldNameByKey;
  }
  
  private boolean acceptEntity(ManagedObjectReference entityRef, Predicate<String> namePredicate, Predicate<String> valuePredicate, Map<Integer, String> fieldNameByKey) {
    assert entityRef != null;
    assert namePredicate != null;
    assert valuePredicate != null;
    assert fieldNameByKey != null;
    ManagedObject managedObject = createStub(ManagedObject.class, entityRef);
    if (!(managedObject instanceof ManagedEntity)) {
      _logger.trace("Skip object which is not a managed entity: {}", entityRef);
      return false;
    } 
    ManagedEntity managedEntity = createStub(ManagedEntity.class, entityRef);
    CustomFieldsManager.Value[] customValues = managedEntity.getCustomValue();
    if (customValues == null) {
      _logger.trace("Skip entity with no custom values: {}", entityRef);
      return false;
    } 
    for (CustomFieldsManager.Value customValue : customValues) {
      if (acceptCustomValue(customValue, namePredicate, valuePredicate, fieldNameByKey)) {
        _logger.trace("Accept entity {} with custom values {}", entityRef, 
            Arrays.toString((Object[])customValues));
        return true;
      } 
    } 
    if (_logger.isTraceEnabled())
      _logger.trace("Skip entity '{}' with custom values: {}", entityRef, 
          Arrays.toString((Object[])customValues)); 
    return false;
  }
  
  private static boolean acceptCustomValue(CustomFieldsManager.Value customValue, Predicate<String> namePredicate, Predicate<String> valuePredicate, Map<Integer, String> fieldNameByKey) {
    assert namePredicate != null;
    assert valuePredicate != null;
    assert fieldNameByKey != null;
    if (!(customValue instanceof CustomFieldsManager.StringValue))
      return false; 
    CustomFieldsManager.StringValue customStringValue = (CustomFieldsManager.StringValue)customValue;
    String value = customStringValue.getValue();
    String fieldName = fieldNameByKey.get(Integer.valueOf(customStringValue.getKey()));
    return (fieldName != null && value != null && namePredicate
      
      .apply(fieldName) && valuePredicate
      .apply(value));
  }
  
  private CustomFieldsManager createCustomFieldsManagerStub() {
    ManagedObjectReference ref = new ManagedObjectReference("CustomFieldsManager", "CustomFieldsManager", null);
    return createStub(CustomFieldsManager.class, ref);
  }
  
  private <T extends ManagedObject> T createStub(@Nonnull Class<T> stubBindingClass, @Nonnull ManagedObjectReference ref) {
    T stub = VlsiClientUtil.createStub(this._vlsiClient, stubBindingClass, ref);
    setOpId((ManagedObject)stub);
    return stub;
  }
  
  private static void setOpId(ManagedObject mo) {
    assert mo != null;
    String opId = QueryMarker.getQueryId();
    if (opId == null)
      return; 
    getRequestContext(mo).put("operationID", opId);
  }
  
  private static RequestContextImpl getRequestContext(ManagedObject mo) {
    assert mo != null;
    Stub stub = (Stub)mo;
    RequestContextImpl rc = (RequestContextImpl)stub._getRequestContext();
    if (rc == null) {
      rc = new RequestContextImpl();
      stub._setRequestContext((RequestContext)rc);
    } 
    return rc;
  }
}

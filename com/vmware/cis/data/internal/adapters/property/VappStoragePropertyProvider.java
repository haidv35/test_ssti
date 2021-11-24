package com.vmware.cis.data.internal.adapters.property;

import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VappStoragePropertyProvider implements BackCompatPropertyProvider {
  private static final String VAPP = "VirtualApp";
  
  private static final String VAPP_VAPSTORAGE_PROPERTY = "VirtualApp/vappStorage";
  
  private static final String VAPP_DATASTORE_PROPERTY = "VirtualApp/datastore";
  
  private static final String STORAGE_POD = "StoragePod";
  
  private static final String DATASTORE = "Datastore";
  
  private static final String DATASTORE_SUMMARY_TYPE = "Datastore/summary/type";
  
  private static final String DATASTORE_PARENT = "Datastore/parent";
  
  private static final String PMEM = "PMEM";
  
  public Collection<String> getProperties() {
    return Collections.unmodifiableList(Arrays.asList(new String[] { "VirtualApp/vappStorage" }));
  }
  
  public List<Collection<?>> fetchPropertyValues(List<String> properties, Collection<Object> keys, DataProvider provider, Client vlsiClient) {
    assert properties != null;
    assert properties.size() == 1;
    assert properties.contains("VirtualApp/vappStorage");
    assert !keys.isEmpty();
    Map<Object, Object[]> datastoresByVapp = getDatastoresByVapp(provider, keys);
    Map<Object, Object> parentByVappDatastore = getParentByVappDatastore(provider, datastoresByVapp);
    List<Collection<?>> result = new ArrayList<>();
    List<Object> vappStorages = new ArrayList();
    for (Object key : keys) {
      Object[] datastores = datastoresByVapp.get(key);
      ManagedObjectReference[] vappStorage = getVappStorages(datastores, parentByVappDatastore);
      vappStorages.add(vappStorage);
    } 
    result.add(vappStorages);
    return result;
  }
  
  private static ManagedObjectReference[] getVappStorages(Object[] datastores, Map<Object, Object> parentByVappDatastore) {
    if (datastores == null)
      return new ManagedObjectReference[0]; 
    Collection<ManagedObjectReference> result = new ArrayList<>();
    for (Object datastore : datastores) {
      Object parent = parentByVappDatastore.get(datastore);
      if (isStoragePod(parent)) {
        result.add((ManagedObjectReference)parent);
      } else {
        result.add((ManagedObjectReference)datastore);
      } 
    } 
    return result.<ManagedObjectReference>toArray(new ManagedObjectReference[result.size()]);
  }
  
  private static boolean isStoragePod(Object object) {
    assert object instanceof ManagedObjectReference;
    ManagedObjectReference objectMor = (ManagedObjectReference)object;
    return "StoragePod".equals(objectMor.getType());
  }
  
  private static Collection<Object> extractDatastoreRefs(Map<Object, Object[]> datastoresByVapp) {
    Set<Object> datastoreRefs = new LinkedHashSet();
    for (Object vapp : datastoresByVapp.keySet()) {
      Object[] datastores = datastoresByVapp.get(vapp);
      if (datastores == null)
        continue; 
      for (Object datastoreRef : datastores)
        datastoreRefs.add(datastoreRef); 
    } 
    return datastoreRefs;
  }
  
  private static Map<Object, Object[]> getDatastoresByVapp(DataProvider provider, Collection<Object> vappKeys) {
    Query query = Query.Builder.select(new String[] { "VirtualApp/datastore" }).from(new String[] { "VirtualApp" }).where("@modelKey", PropertyPredicate.ComparisonOperator.IN, vappKeys).build();
    ResultSet resultSet = provider.executeQuery(query);
    List<ResourceItem> items = resultSet.getItems();
    Map<Object, Object[]> datastoresByVapp = (Map)new LinkedHashMap<>(items.size());
    for (ResourceItem item : items) {
      Object[] datastores = item.<Object[]>get("VirtualApp/datastore");
      datastoresByVapp.put(item.getKey(), datastores);
    } 
    return datastoresByVapp;
  }
  
  private static Map<Object, Object> getParentByVappDatastore(DataProvider provider, Map<Object, Object[]> datastoresByVapp) {
    Collection<Object> datastoreRefs = extractDatastoreRefs(datastoresByVapp);
    if (datastoreRefs.isEmpty())
      return Collections.emptyMap(); 
    Query query = Query.Builder.select(new String[] { "Datastore/parent" }).from(new String[] { "Datastore" }).where(LogicalOperator.AND, new PropertyPredicate[] { new PropertyPredicate("Datastore/summary/type", PropertyPredicate.ComparisonOperator.NOT_EQUAL, "PMEM"), new PropertyPredicate("@modelKey", PropertyPredicate.ComparisonOperator.IN, datastoreRefs) }).build();
    ResultSet resultSet = provider.executeQuery(query);
    List<ResourceItem> items = resultSet.getItems();
    Map<Object, Object> parentByDatastore = new LinkedHashMap<>();
    for (ResourceItem item : items) {
      Object parent = item.get("Datastore/parent");
      parentByDatastore.put(item.getKey(), parent);
    } 
    return parentByDatastore;
  }
}

package com.vmware.cis.data.internal.adapters.property;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PathToTheRootPropertyProvider implements BackCompatPropertyProvider {
  private static final Logger _logger = LoggerFactory.getLogger(PathToTheRootPropertyProvider.class);
  
  private static final String DATACENTER = "Datacenter";
  
  private static final String FOLDER_PATH_TO_THE_ROOT = "Folder/pathToTheRoot";
  
  private static final String VDVS_PATH_TO_THE_ROOT = "VmwareDistributedVirtualSwitch/pathToTheRoot";
  
  private static final Collection<String> _properties;
  
  static {
    List<String> properties = new ArrayList<>();
    properties.add("Folder/pathToTheRoot");
    properties.add("VmwareDistributedVirtualSwitch/pathToTheRoot");
    _properties = Collections.unmodifiableList(properties);
  }
  
  public Collection<String> getProperties() {
    return _properties;
  }
  
  public List<Collection<?>> fetchPropertyValues(List<String> properties, Collection<Object> keys, DataProvider provider, Client vlsiClient) {
    assert _properties.containsAll(properties);
    List<Collection<?>> propertyValues = new ArrayList<>(properties.size());
    for (String property : properties) {
      Collection<Object> values = getPaths(keys, provider, property);
      propertyValues.add(values);
    } 
    return propertyValues;
  }
  
  private static Collection<Object> getPaths(Collection<Object> keys, DataProvider provider, String property) {
    if (keys.size() > 1)
      _logger
        .warn("Performance problem: Backwards compatibility property provider for {} expects single resource, but it is invoked for {} resources.", property, 
          
          Integer.valueOf(keys.size())); 
    List<Object> paths = new ArrayList(keys.size());
    for (Object key : keys)
      paths.add(getPath(key, provider)); 
    return paths;
  }
  
  private static ManagedObjectReference[] getPath(Object key, DataProvider provider) {
    if (!(key instanceof ManagedObjectReference))
      throw new IllegalArgumentException("The provided key should be of type ManagedObjectReference. The current key '" + key + "' is of type " + key
          
          .getClass().getName()); 
    List<ManagedObjectReference> path = new ArrayList<>();
    ManagedObjectReference currentKey = (ManagedObjectReference)key;
    while (true) {
      String type = currentKey.getType();
      String qualifiedParentProperty = type + "/parent";
      Query query = Query.Builder.select(new String[] { qualifiedParentProperty }).from(new String[] { type }).where("@modelKey", PropertyPredicate.ComparisonOperator.EQUAL, currentKey).build();
      ResultSet resultSet = provider.executeQuery(query);
      List<ResourceItem> items = resultSet.getItems();
      if (items.isEmpty()) {
        _logger.warn("Could not find tree node with key '{}'. Most likely it has been deleted. No path is returned.", key);
        return new ManagedObjectReference[0];
      } 
      assert items.size() == 1;
      ManagedObjectReference parentKey = ((ResourceItem)items.get(0)).<ManagedObjectReference>get(qualifiedParentProperty);
      if (parentKey == null) {
        path.add(currentKey);
        break;
      } 
      if (!parentKey.getType().equals("Datacenter") || path.isEmpty())
        path.add(currentKey); 
      currentKey = parentKey;
    } 
    return path.<ManagedObjectReference>toArray(new ManagedObjectReference[path.size()]);
  }
}

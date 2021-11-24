package com.vmware.cis.data.internal.adapters.property;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.binding.vim.Datastore;
import com.vmware.vim.binding.vim.host.MountInfo;
import com.vmware.vim.vmomi.client.Client;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DatastoreIsReadOnlyPropertyProvider implements BackCompatPropertyProvider {
  private static final String READ_WRITE_VALUE = "readWrite";
  
  private static final String DATASTORE = "Datastore";
  
  private static final String PROP_IS_READ_ONLY = "Datastore/isReadOnly";
  
  private static final String PROP_HOSTS = "Datastore/host";
  
  private static final List<String> SUPPORTED_PROPERTIES = Arrays.asList(new String[] { "Datastore/isReadOnly" });
  
  public Collection<String> getProperties() {
    return SUPPORTED_PROPERTIES;
  }
  
  public List<Collection<?>> fetchPropertyValues(List<String> properties, Collection<Object> keys, DataProvider provider, Client vlsiClient) {
    assert properties != null;
    assert properties.size() == 1;
    assert properties.contains("Datastore/isReadOnly");
    Map<Object, Boolean> isReadOnlyByMor = getResults(provider, keys);
    Collection<Boolean> propertyValues = new ArrayList<>(keys.size());
    for (Object key : keys) {
      Boolean value = isReadOnlyByMor.get(key);
      propertyValues.add(value);
    } 
    List<Collection<?>> result = new ArrayList<>(1);
    result.add(propertyValues);
    return result;
  }
  
  private Map<Object, Boolean> getResults(DataProvider provider, Collection<Object> keys) {
    Query query = Query.Builder.select(new String[] { "Datastore/host" }).from(new String[] { "Datastore" }).where("@modelKey", PropertyPredicate.ComparisonOperator.IN, keys).build();
    ResultSet resultSet = provider.executeQuery(query);
    Map<Object, Boolean> isReadOnlyByMor = new LinkedHashMap<>(keys.size());
    for (ResourceItem item : resultSet.getItems()) {
      Datastore.HostMount[] hostMountArr = item.<Datastore.HostMount[]>get("Datastore/host");
      Boolean isReadOnlyValue = Boolean.valueOf(getIsReadOnly(hostMountArr));
      Object key = item.getKey();
      isReadOnlyByMor.put(key, isReadOnlyValue);
    } 
    return isReadOnlyByMor;
  }
  
  private boolean getIsReadOnly(Datastore.HostMount[] hostMountArr) {
    if (hostMountArr == null)
      return true; 
    for (Datastore.HostMount hostMount : hostMountArr) {
      MountInfo mountInfo = hostMount.mountInfo;
      if (mountInfo.getAccessible() != null)
        if (mountInfo.getAccessible().booleanValue())
          if (mountInfo.getMounted() != null)
            if (mountInfo.getMounted().booleanValue())
              if ("readWrite".equals(hostMount.mountInfo.accessMode))
                return false;     
    } 
    return true;
  }
}

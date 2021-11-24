package com.vmware.cis.data.internal.provider.ext.clientside.filter;

import com.google.common.collect.ImmutableMap;
import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.merge.DefaultItemComparator;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import com.vmware.cis.data.internal.provider.util.filter.FilterEvaluator;
import com.vmware.cis.data.internal.provider.util.property.PropertyByName;
import com.vmware.cis.data.internal.provider.util.property.PropertyByNameBackedByResourceItem;
import com.vmware.cis.data.internal.provider.util.property.ResourceItemPropertyValueByNameViaIndexMap;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClientSideFiltering implements DataProvider {
  public static final Map<String, QuerySchema.PropertyInfo> VMOMI_CLIENT_SIDE_PROPS = (Map<String, QuerySchema.PropertyInfo>)ImmutableMap.builder()
    .put("ClusterComputeResource/summary/numCpuCores", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.SHORT))
    .put("ComputeResource/summary/numCpuCores", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.SHORT))
    .put("Datastore/alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN))
    .put("Datastore/capability/perFileThinProvisioningSupported", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN))
    .put("Datastore/host/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))


    
    .put("Datastore/info/timestamp", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID))
    .put("Datastore/info/freeSpace", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .put("Datastore/overallStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM))
    .put("Datacenter/host/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("Datacenter/vm/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("Datacenter/alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM))
    .put("DistributedVirtualPortgroup/alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN))
    .put("DistributedVirtualPortgroup/config/numPorts", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("DistributedVirtualPortgroup/overallStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM))
    .put("DistributedVirtualPortgroup/summary/ipPoolName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("DistributedVirtualPortgroup/vm/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("Folder/allNormalVMOrPrimaryFTVM/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("Folder/host/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))


    
    .put("HostProfile/modifiedTime", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID))
    .put("HostSystem/alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN))
    .put("HostSystem/capability/tpmSupported", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN))
    .put("HostSystem/hardware/cpuInfo/numCpuPackages", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.SHORT))
    .put("HostSystem/hardware/memorySize", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .put("HostSystem/summary/hardware/numNics", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("HostSystem/summary/overallStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM))
    .put("HostSystem/summary/quickStats/uptime", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("HostSystem/vsanFaultDomainName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("Network/summary/ipPoolName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("Network/vm/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("OpaqueNetwork/summary/ipPoolName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("OpaqueNetwork/summary/opaqueNetworkId", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("OpaqueNetwork/summary/opaqueNetworkType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("OpaqueNetwork/vm/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("ResourcePool/config/cpuAllocation/reservation", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .put("ResourcePool/config/cpuAllocation/limit", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .put("ResourcePool/config/cpuAllocation/expandableReservation", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN))
    .put("ResourcePool/config/cpuAllocation/shares/level", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM))
    .put("ResourcePool/config/cpuAllocation/shares/shares", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("ResourcePool/config/memoryAllocation/shares/level", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM))
    .put("ResourcePool/config/memoryAllocation/shares/shares", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("ResourcePool/parentVApp", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID))
    .put("StoragePod/alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN))
    .put("StoragePod/datastore/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .put("StoragePod/overallStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM))
    .put("StoragePod/summary/capacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .put("StoragePod/summary/freeSpace", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .put("StoragePod/vmTemplateCount", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("VirtualApp/config/cpuAllocation/shares/level", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("VirtualApp/config/cpuAllocation/shares/shares", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("VirtualApp/config/memoryAllocation/shares/level", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("VirtualApp/config/memoryAllocation/shares/shares", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("VirtualMachine/alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN))
    .put("VirtualMachine/summary/config/annotation", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("VirtualMachine/summary/config/uuid", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("VirtualMachine/summary/overallStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM))
    .put("VirtualMachine/summary/quickStats/hostMemoryUsage", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("VirtualMachine/summary/quickStats/overallCpuUsage", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("VirtualMachine/summary/quickStats/uptimeSeconds", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("VirtualMachine/vsanFaultDomainName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    
    .put("Datastore/specificType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("Datastore/thinProvisioningSupported", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN))
    .put("HostSystem/memoryUsage", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("HostSystem/cpuUsage", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("HostSystem/pciPassthruSupported", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN))
    .put("HostSystem/certificateValidTo", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID))
    .put("StoragePod/capacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .put("StoragePod/free", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .put("VirtualMachine/guestMemoryPercentage", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .put("VirtualMachine/hostCpuUsage", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("VirtualMachine/hostMemoryUsage", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("ClusterComputeResource/freeCpuCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("ClusterComputeResource/totalCpuCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("ClusterComputeResource/usedCpuCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("ClusterComputeResource/freeMemoryCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("ClusterComputeResource/totalMemoryCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("ClusterComputeResource/usedMemoryCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT))
    .put("ClusterComputeResource/freeStorageCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .put("ClusterComputeResource/totalStorageCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .put("ClusterComputeResource/usedStorageCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG))
    .build();
  
  public static final Map<String, QuerySchema.PropertyInfo> VAPI_CLIENT_SIDE_PROPS = (Map<String, QuerySchema.PropertyInfo>)ImmutableMap.builder()
    .put("com.vmware.content.library.ItemModel/contentVersion", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("com.vmware.content.library.ItemModel/description", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .put("com.vmware.content.type.ovf.OvfTemplate/vmTemplate/osDescription", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING))
    .build();
  
  private final DataProvider _provider;
  
  private final Map<String, QuerySchema.PropertyInfo> _clientSideProps;
  
  private final QuerySchema _schema;
  
  public ClientSideFiltering(DataProvider provider, Map<String, QuerySchema.PropertyInfo> clientSideProps) {
    assert provider != null;
    assert clientSideProps != null;
    this._provider = provider;
    this._schema = provider.getSchema();
    this._clientSideProps = clientSideProps;
  }
  
  public QuerySchema getSchema() {
    if (this._schema.getModels().isEmpty())
      return this._schema; 
    QuerySchema clientSideSchema = QuerySchema.forProperties(this._clientSideProps);
    return SchemaUtil.merge(this._schema, clientSideSchema);
  }
  
  public ResultSet executeQuery(Query query) {
    if (!isClientSide(query))
      return this._provider.executeQuery(query); 
    List<PropertyPredicate> clientSidePredicates = new ArrayList<>();
    Filter newFilter = toExecutableFilter(query.getFilter(), clientSidePredicates);
    List<String> newSelect = toExecutableSelect(query.getProperties(), clientSidePredicates, query
        .getSortCriteria());
    Collection<String> resourceModels = query.getResourceModels();
    Query executableQuery = Query.Builder.select(newSelect).from(resourceModels).where(newFilter).build();
    ResultSet result = this._provider.executeQuery(executableQuery);
    ResultSet filteredResult = filterAtClientSide(result, query, clientSidePredicates);
    ResultSet pagedResult = ResultSetUtil.applyLimitAndOffset(filteredResult, query.getLimit(), query
        .getOffset());
    return ResultSetUtil.project(pagedResult, query.getProperties());
  }
  
  public String toString() {
    return this._provider.toString();
  }
  
  private boolean isClientSide(Query query) {
    if (query.getFilter() != null) {
      Filter filter = query.getFilter();
      for (PropertyPredicate predicate : filter.getCriteria()) {
        if (isClientSide(predicate.getProperty()))
          return true; 
      } 
    } 
    for (SortCriterion sortCriterion : query.getSortCriteria()) {
      if (isClientSide(sortCriterion.getProperty()))
        return true; 
    } 
    return false;
  }
  
  private boolean isClientSide(String property) {
    if (!this._clientSideProps.containsKey(property))
      return false; 
    QuerySchema.PropertyInfo propertyInfo = SchemaUtil.getPropertyInfoForQualifiedName(this._schema, property);
    if (propertyInfo == null) {
      String msg = String.format("The property '%s' is listed for client-side handling, but it is not available on the server.", new Object[] { property });
      throw new IllegalArgumentException(msg);
    } 
    return !propertyInfo.getFilterable();
  }
  
  private Filter toExecutableFilter(Filter filter, List<PropertyPredicate> clientSidePredicates) {
    if (filter == null)
      return filter; 
    List<PropertyPredicate> predicates = filter.getCriteria();
    List<PropertyPredicate> serverSidePredicates = new ArrayList<>(predicates.size());
    for (PropertyPredicate predicate : predicates) {
      if (isClientSide(predicate.getProperty())) {
        clientSidePredicates.add(predicate);
        continue;
      } 
      serverSidePredicates.add(predicate);
    } 
    if (clientSidePredicates.isEmpty())
      return filter; 
    if (serverSidePredicates.isEmpty())
      return null; 
    if (filter.getOperator().equals(LogicalOperator.OR)) {
      clientSidePredicates.addAll(serverSidePredicates);
      return null;
    } 
    return new Filter(serverSidePredicates, filter.getOperator());
  }
  
  private static List<String> toExecutableSelect(List<String> properties, List<PropertyPredicate> predicates, List<SortCriterion> sorts) {
    Set<String> newSelect = new LinkedHashSet<>(properties);
    for (PropertyPredicate predicate : predicates)
      newSelect.add(predicate.getProperty()); 
    for (SortCriterion sort : sorts)
      newSelect.add(sort.getProperty()); 
    return new ArrayList<>(newSelect);
  }
  
  private static ResultSet filterAtClientSide(ResultSet result, Query query, List<PropertyPredicate> clientSidePredicates) {
    Filter clientSideFilter = toFilter(query.getFilter(), clientSidePredicates);
    List<ResourceItem> filteredItems = filterAtClientSide(result.getProperties(), result
        .getItems(), clientSideFilter);
    List<ResourceItem> orderedItems = order(result.getProperties(), filteredItems, query
        .getSortCriteria());
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(result.getProperties());
    for (ResourceItem item : orderedItems)
      resultBuilder.item(item.getKey(), item.getPropertyValues()); 
    if (query.getWithTotalCount())
      resultBuilder.totalCount(Integer.valueOf(orderedItems.size())); 
    return resultBuilder.build();
  }
  
  private static List<ResourceItem> filterAtClientSide(List<String> properties, List<ResourceItem> items, Filter filter) {
    if (filter == null)
      return items; 
    List<ResourceItem> filteredItems = new ArrayList<>();
    for (ResourceItem item : items) {
      if (FilterEvaluator.eval(filter, toPropertyByName(item, properties)))
        filteredItems.add(item); 
    } 
    return filteredItems;
  }
  
  private static List<ResourceItem> order(List<String> properties, List<ResourceItem> items, List<SortCriterion> sortCriteria) {
    if (sortCriteria.isEmpty())
      return items; 
    Comparator<ResourceItem> comparator = new DefaultItemComparator(properties, sortCriteria);
    List<ResourceItem> orderedItems = new ArrayList<>(items);
    Collections.sort(orderedItems, comparator);
    return orderedItems;
  }
  
  private static Filter toFilter(Filter filter, List<PropertyPredicate> predicates) {
    if (filter == null)
      return null; 
    if (predicates.isEmpty())
      return null; 
    return new Filter(predicates, filter.getOperator());
  }
  
  private static PropertyByName toPropertyByName(ResourceItem item, List<String> properties) {
    return new PropertyByNameBackedByResourceItem(item, new ResourceItemPropertyValueByNameViaIndexMap(properties));
  }
}

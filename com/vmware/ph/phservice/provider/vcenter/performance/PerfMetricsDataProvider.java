package com.vmware.ph.phservice.provider.vcenter.performance;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vim.internal.vc.VcServiceInstanceContentBuilder;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.util.ManagedEntityReader;
import com.vmware.ph.phservice.provider.common.QueryContext;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlQueryContextUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.vim.binding.vim.Folder;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PerfMetricsDataProvider implements DataProvider {
  static final String PERF_COUNTERS_PROPERTY = "perfCounters";
  
  static final String PERF_ENTITY_METRICS_PROPERTY = "perfEntityMetrics";
  
  static final String PERF_INTERVAL_PROPERTY = "perfInterval";
  
  static final String PERF_MAX_SAMPLE_PROPERTY = "perfMaxSample";
  
  static final String PERF_INSTANCES_PROPERTY = "perfInstances";
  
  private final Builder<ServiceInstanceContent> _serviceInstanceContentBuilder;
  
  private final VmodlTypeMap _vmodlTypeMap;
  
  private final ManagedEntityReader _entityMoRefReader;
  
  private final PerfMetricsReader _perfMetricsReader;
  
  public PerfMetricsDataProvider(VcClient vcClient) {
    this._serviceInstanceContentBuilder = new VcServiceInstanceContentBuilder(vcClient);
    this._vmodlTypeMap = vcClient.getVmodlContext().getVmodlTypeMap();
    this._entityMoRefReader = new ManagedEntityReader(vcClient);
    this._perfMetricsReader = new PerfMetricsReader(vcClient);
  }
  
  PerfMetricsDataProvider(Builder<ServiceInstanceContent> serviceInstanceContentBuilder, VmodlTypeMap vmodlTypeMap, ManagedEntityReader entityMoRefReader, PerfMetricsReader perfMetricsReader) {
    this._serviceInstanceContentBuilder = serviceInstanceContentBuilder;
    this._vmodlTypeMap = vmodlTypeMap;
    this._entityMoRefReader = entityMoRefReader;
    this._perfMetricsReader = perfMetricsReader;
  }
  
  public QuerySchema getSchema() {
    List<VmodlType> inventoryVmodlTypes = ManagedEntityReader.getInventoryManagedEntityVmodlTypes(this._vmodlTypeMap);
    List<String> propertyNames = Arrays.asList(new String[] { "perfEntityMetrics", "perfCounters", "perfInterval", "perfMaxSample", "perfInstances" });
    Map<String, QuerySchema.ModelInfo> schemaModels = new HashMap<>();
    for (VmodlType vmodlType : inventoryVmodlTypes) {
      QuerySchema.ModelInfo modelInfo = VmodlTypeToQuerySchemaModelInfoConverter.convertPropertyNamesToModelInfo(propertyNames);
      schemaModels.put(vmodlType.getWsdlName(), modelInfo);
    } 
    QuerySchema querySchema = QuerySchema.forModels(schemaModels);
    return querySchema;
  }
  
  public ResultSet executeQuery(Query query) {
    List<ManagedObjectReference> managedEntityMoRefs = getManagedEntityMoRefs(query);
    List<String> queryProperties = query.getProperties();
    Map<ManagedObjectReference, List<Object>> managedEntityMoRefToPropertyValues = getManagedEntitiesPropertyValues(managedEntityMoRefs, queryProperties, query

        
        .getFilter());
    ResultSet resultSet = buildQueryResultSet(queryProperties, managedEntityMoRefs, managedEntityMoRefToPropertyValues);
    return resultSet;
  }
  
  private List<ManagedObjectReference> getManagedEntityMoRefs(Query query) {
    String resourceModel = query.getResourceModels().iterator().next();
    QueryContext queryContext = QueryContextUtil.getQueryContextFromQueryFilter(query);
    List<ManagedObjectReference> moRefs = VmodlQueryContextUtil.getMoRefsFromContext(queryContext, resourceModel);
    if (!moRefs.isEmpty())
      return moRefs; 
    VmodlType vmodlType = this._vmodlTypeMap.getVmodlType(resourceModel);
    List<ManagedObjectReference> managedEntityMoRefs = null;
    if (vmodlType.getTypeClass().equals(Folder.class)) {
      ManagedObjectReference rootFolderMoRef = ((ServiceInstanceContent)this._serviceInstanceContentBuilder.build()).getRootFolder();
      managedEntityMoRefs = Arrays.asList(new ManagedObjectReference[] { rootFolderMoRef });
    } else {
      managedEntityMoRefs = this._entityMoRefReader.getManagedEntityMoRefs(vmodlType, query
          
          .getOffset(), query
          .getLimit());
    } 
    return managedEntityMoRefs;
  }
  
  private Map<ManagedObjectReference, List<Object>> getManagedEntitiesPropertyValues(List<ManagedObjectReference> managedEntityMoRefs, List<String> queryProperties, Filter queryFilter) {
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(queryProperties);
    Map<String, Object> filterProperties = getFilterProperties(queryFilter.getCriteria());
    Map<ManagedObjectReference, List<Object>> moRefToPropertyValues = new LinkedHashMap<>(managedEntityMoRefs.size());
    for (String queryProperty : nonQualifiedQueryProperties) {
      if (QuerySchemaUtil.isQueryPropertyModelKey(queryProperty))
        continue; 
      Map<ManagedObjectReference, Object> moRefToPropertyValue = getManagedEntitiesPropertyValue(managedEntityMoRefs, queryProperty, filterProperties);
      mergePropertyValues(moRefToPropertyValues, moRefToPropertyValue);
    } 
    return moRefToPropertyValues;
  }
  
  private static ResultSet buildQueryResultSet(List<String> queryProperties, List<ManagedObjectReference> managedEntityMoRefs, Map<ManagedObjectReference, List<Object>> managedEntityMoRefToPropertyValues) {
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(queryProperties);
    for (ManagedObjectReference managedEntityMoRef : managedEntityMoRefs) {
      List<Object> propertyValues = managedEntityMoRefToPropertyValues.get(managedEntityMoRef);
      propertyValues.add(0, managedEntityMoRef);
      resultBuilder.item(managedEntityMoRef, propertyValues);
    } 
    return resultBuilder.build();
  }
  
  private Map<ManagedObjectReference, Object> getManagedEntitiesPropertyValue(List<ManagedObjectReference> managedEntityMoRefs, String queryProperty, Map<String, Object> filterProperties) {
    List<PerfEntityMetrics> entityMetrics;
    switch (queryProperty) {
      case "perfEntityMetrics":
        entityMetrics = getManagedEntityMetrics(managedEntityMoRefs, filterProperties);
        return convertToMetricsValues(entityMetrics);
      case "perfInterval":
        return convertToIntervalValues(managedEntityMoRefs, filterProperties);
      case "perfCounters":
        return convertToCounterValues(managedEntityMoRefs, filterProperties);
      case "perfMaxSample":
        return convertToMaxSampleValues(managedEntityMoRefs, filterProperties);
      case "perfInstances":
        return convertToInstanceValues(managedEntityMoRefs, filterProperties);
    } 
    return Collections.emptyMap();
  }
  
  private List<PerfEntityMetrics> getManagedEntityMetrics(List<ManagedObjectReference> managedEntityMoRefs, Map<String, Object> filterProperties) {
    if (managedEntityMoRefs.isEmpty())
      return Collections.emptyList(); 
    List<String> countersNames = getPerfCountersNames(filterProperties, true);
    PerfInterval perfInterval = getPerfInterval(filterProperties, true);
    Integer perfMaxSample = getPerfMaxSample(filterProperties);
    List<String> perfInstances = getPerfInstances(filterProperties);
    return this._perfMetricsReader.collectPerformanceData(managedEntityMoRefs, countersNames, perfInterval, perfMaxSample, perfInstances);
  }
  
  private static Map<String, Object> getFilterProperties(List<PropertyPredicate> criteria) {
    Map<String, Object> filterProperties = new TreeMap<>();
    for (PropertyPredicate propertyPredicate : criteria) {
      String propertyName = QuerySchemaUtil.getActualPropertyName(propertyPredicate.getProperty());
      filterProperties.put(propertyName, propertyPredicate.getComparableValue());
    } 
    return filterProperties;
  }
  
  private static void mergePropertyValues(Map<ManagedObjectReference, List<Object>> moRefToCurrentPropertyValues, Map<ManagedObjectReference, Object> moRefToPropertyValue) {
    for (Map.Entry<ManagedObjectReference, Object> propertyValueEntry : moRefToPropertyValue.entrySet()) {
      ManagedObjectReference moRef = propertyValueEntry.getKey();
      List<Object> propertyValues = moRefToCurrentPropertyValues.get(moRef);
      if (propertyValues == null)
        propertyValues = new LinkedList(); 
      propertyValues.add(propertyValueEntry.getValue());
      moRefToCurrentPropertyValues.put(moRef, propertyValues);
    } 
  }
  
  private static Map<ManagedObjectReference, Object> convertToMetricsValues(List<PerfEntityMetrics> entityMetrics) {
    Map<ManagedObjectReference, Object> moRefToEntityMetrics = new LinkedHashMap<>(entityMetrics.size());
    for (PerfEntityMetrics entityMetric : entityMetrics) {
      ManagedObjectReference entityMoRef = entityMetric.getEntityMoRef();
      List<Object[]> metricsForMoRef = entityMetric.getEntityMetrics();
      moRefToEntityMetrics.put(entityMoRef, metricsForMoRef);
    } 
    return moRefToEntityMetrics;
  }
  
  private static Map<ManagedObjectReference, Object> convertToIntervalValues(List<ManagedObjectReference> managedEntityMoRefs, Map<String, Object> filterProperties) {
    PerfInterval perfInterval = getPerfInterval(filterProperties, false);
    return convertToSameValueMap(managedEntityMoRefs, perfInterval);
  }
  
  private static Map<ManagedObjectReference, Object> convertToCounterValues(List<ManagedObjectReference> managedEntityMoRefs, Map<String, Object> filterProperties) {
    List<String> countersNames = getPerfCountersNames(filterProperties, false);
    return convertToSameValueMap(managedEntityMoRefs, countersNames);
  }
  
  private static Map<ManagedObjectReference, Object> convertToMaxSampleValues(List<ManagedObjectReference> managedEntityMoRefs, Map<String, Object> filterProperties) {
    Integer maxSamples = getPerfMaxSample(filterProperties);
    return convertToSameValueMap(managedEntityMoRefs, maxSamples);
  }
  
  private Map<ManagedObjectReference, Object> convertToInstanceValues(List<ManagedObjectReference> managedEntityMoRefs, Map<String, Object> filterProperties) {
    List<String> perfInstances = getPerfInstances(filterProperties);
    return convertToSameValueMap(managedEntityMoRefs, perfInstances);
  }
  
  private static List<String> getPerfCountersNames(Map<String, Object> filterProperties, boolean isPerfCountersRequired) {
    Object perfCounters = filterProperties.get("perfCounters");
    if (perfCounters != null && perfCounters instanceof List)
      return (List<String>)perfCounters; 
    if (isPerfCountersRequired)
      throw new IllegalArgumentException(
          String.format("Rrequired [%s] property should contain list of counter names.", new Object[] { "perfCounters" })); 
    return null;
  }
  
  private static PerfInterval getPerfInterval(Map<String, Object> filterProperties, boolean isPerfIntervalRequired) {
    Object perfInterval = filterProperties.get("perfInterval");
    if (perfInterval != null)
      return PerfInterval.parse((String)perfInterval); 
    if (isPerfIntervalRequired)
      throw new IllegalArgumentException(
          String.format("Missing value for required [%s] property. Supported values are %s.", new Object[] { "perfInterval", Arrays.toString(PerfInterval.values()) })); 
    return null;
  }
  
  private static Integer getPerfMaxSample(Map<String, Object> filterProperties) {
    Object perfMaxSample = filterProperties.get("perfMaxSample");
    if (perfMaxSample != null)
      return Integer.valueOf(Integer.parseInt(perfMaxSample.toString())); 
    return null;
  }
  
  private List<String> getPerfInstances(Map<String, Object> filterProperties) {
    Object perfInstances = filterProperties.get("perfInstances");
    if (perfInstances instanceof List)
      return (List<String>)perfInstances; 
    return null;
  }
  
  private static Map<ManagedObjectReference, Object> convertToSameValueMap(List<ManagedObjectReference> managedEntityMoRefs, Object value) {
    Map<ManagedObjectReference, Object> moRefToSameValues = new LinkedHashMap<>(managedEntityMoRefs.size());
    for (ManagedObjectReference managedEntityRef : managedEntityMoRefs)
      moRefToSameValues.put(managedEntityRef, value); 
    return moRefToSameValues;
  }
}

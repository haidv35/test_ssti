package com.vmware.ph.phservice.provider.vsan;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.vim.VimVmodlUtil;
import com.vmware.ph.phservice.common.vim.internal.VimServiceInstanceContentBuilder;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.util.ClusterReader;
import com.vmware.ph.phservice.common.vim.vc.util.DatastoreReader;
import com.vmware.ph.phservice.common.vim.vc.util.HostReader;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.common.vsan.VsanClusterReader;
import com.vmware.ph.phservice.common.vsan.VsanMassCollectorReader;
import com.vmware.ph.phservice.common.vsan.filtering.FilterRule;
import com.vmware.ph.phservice.provider.common.QueryContext;
import com.vmware.ph.phservice.provider.common.QueryContextParser;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlJsonLdQueryContextParser;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlQueryContextUtil;
import com.vmware.ph.phservice.provider.vsan.internal.QueryFilterToVsanMassCollectorSpecConverter;
import com.vmware.ph.phservice.provider.vsan.internal.VsanMassCollectorSpecTimeModifier;
import com.vmware.vim.binding.vim.ClusterComputeResource;
import com.vmware.vim.binding.vim.Datastore;
import com.vmware.vim.binding.vim.HostSystem;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.fault.ManagedObjectNotFound;
import com.vmware.vim.vsan.binding.vim.VsanJsonFilterRule;
import com.vmware.vim.vsan.binding.vim.VsanMassCollectorSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

public class VsanMassCollectorDataProvider implements DataProvider {
  public static final String RESOURCE_MODEL_TYPE = "VsanMassCollector";
  
  public static final String OBJECT_ID_PROP_NAME = "objectId";
  
  public static final String VSAN_RETRIEVE_PROPERTIES_PROP_NAME = "vsanRetrieveProperties";
  
  public static final String VSAN_RETRIEVE_PROPERTIES_JSON_PROP_NAME = "vsanRetrievePropertiesJson";
  
  public static final String OBJECTS_PROPERTY = "objects";
  
  public static final String VSAN_QUERY_SPECS_START_TIME_PROPERTY = "vsanQueryStartTime";
  
  public static final String VSAN_QUERY_SPECS_END_TIME_PROPERTY = "vsanQueryEndTime";
  
  public static final String VSAN_QUERY_SPECS_PROPERTY = "vsanMassCollectorSpecs";
  
  public static final String FILTER_SPECS_PROPERTY = "filterSpecs";
  
  public static final String VSAN_MASS_COLLECTOR_SPEC_OBJECT_COLLECTION_PROPERTY = "vsanMassCollectorSpecs/objectCollection";
  
  public static final String VSAN_MASS_COLLECTOR_SPEC_OBJECTS_PROPERTY = "vsanMassCollectorSpecs/objects";
  
  public static final String VSAN_MASS_COLLECTOR_SPEC_COLLECTION_PROPERTIES = "vsanMassCollectorSpecs/properties";
  
  public static final String VSAN_MASS_COLLECTOR_SPEC_PROPERTIES_PARAMS_PROPERTY = "vsanMassCollectorSpecs/propertiesParams";
  
  private static final String VCENTER_OBJECT_COLLECTION_TYPE = "VCENTER";
  
  private static final String SERVICE_INSTANCE_OBJECT_COLLECTION_TYPE = "SERVICE_INSTANCE";
  
  private static final String VSAN_ENABLED_OBJECT_COLLECTION_TYPE = "VSAN_ENABLED";
  
  private static final String EXCEPT_WITNESS_OBJECT_COLLECTION_TYPE = "EXCEPT_WITNESS";
  
  private static final String HOSTS_OBJECT_COLLECTION_TYPE = "HOSTS";
  
  private static final String CLUSTERS_OBJECT_COLLECTION_TYPE = "CLUSTERS";
  
  private static final String DATASTORES_OBJECT_COLLECTION_TYPE = "DATASTORES";
  
  private static final String ALL_DATASTORES_OBJECT_COLLECTION_TYPE = "ALL_DATASTORES";
  
  private static final String DATASTORE_CLASS_SIMPLE_NAME = Datastore.class.getSimpleName();
  
  private static final String HOST_SYSTEM_CLASS_SIMPLE_NAME = HostSystem.class.getSimpleName();
  
  private static final Log _log = LogFactory.getLog(VsanMassCollectorDataProvider.class);
  
  private final VsanMassCollectorReader _vsanMassCollectorReader;
  
  private final Builder<ServiceInstanceContent> _serviceInstanceContentBuilder;
  
  private final ClusterReader _clusterReader;
  
  private final VsanClusterReader _vsanClusterReader;
  
  private final HostReader _hostReader;
  
  private final DatastoreReader _datastoreReader;
  
  private final QueryFilterToVsanMassCollectorSpecConverter _queryConverter;
  
  private final QueryContextParser _queryContextParser;
  
  private final VmodlToJsonLdSerializer _serializer;
  
  public VsanMassCollectorDataProvider(VmomiClient vsanHealthVmomiClient, VcClient vcClient, VmodlToJsonLdSerializer serializer) {
    this(new VsanMassCollectorReader(vsanHealthVmomiClient), (Builder<ServiceInstanceContent>)new VimServiceInstanceContentBuilder(vcClient
          .getVlsiClient()), new ClusterReader(vcClient), new VsanClusterReader(vsanHealthVmomiClient, vcClient), new HostReader(vcClient), new DatastoreReader(vcClient), serializer);
  }
  
  VsanMassCollectorDataProvider(VsanMassCollectorReader vsanMassCollectorReader, Builder<ServiceInstanceContent> serviceInstanceContentBuilder, ClusterReader clusterReader, VsanClusterReader vsanClusterReader, HostReader hostReader, DatastoreReader datastoreReader, VmodlToJsonLdSerializer serializer) {
    this._vsanMassCollectorReader = vsanMassCollectorReader;
    this._serviceInstanceContentBuilder = serviceInstanceContentBuilder;
    this._clusterReader = clusterReader;
    this._vsanClusterReader = vsanClusterReader;
    this._hostReader = hostReader;
    this._datastoreReader = datastoreReader;
    this._serializer = serializer;
    this._queryConverter = new QueryFilterToVsanMassCollectorSpecConverter(serializer);
    this._queryContextParser = (QueryContextParser)new VmodlJsonLdQueryContextParser(serializer);
  }
  
  public QuerySchema getSchema() {
    return createQuerySchema();
  }
  
  public ResultSet executeQuery(Query query) {
    List<String> queryProperties = query.getProperties();
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(queryProperties);
    List<Object> propertyValues = getVsanMassCollectorPropertyValues(query);
    resultSetBuilder.item(propertyValues.get(0), propertyValues);
    ResultSet resultSet = resultSetBuilder.build();
    return resultSet;
  }
  
  private List<Object> getVsanMassCollectorPropertyValues(Query query) {
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(query.getProperties());
    Long vsanQueryStartTime = getVsanQueryTimePropertyValueFromQueryFilter(query, "vsanQueryStartTime");
    Long vsanQueryEndTime = getVsanQueryTimePropertyValueFromQueryFilter(query, "vsanQueryEndTime");
    Map<ManagedObjectReference, String> moRefToObjectId = getMoRefToObjectIdFromQueryContext(query, this._queryContextParser);
    List<Object> propertyValues = new ArrayList();
    for (String nonQualifiedQueryProperty : nonQualifiedQueryProperties) {
      Object propertyValue = getVsanMassCollectorPropertyValue(query, nonQualifiedQueryProperty, moRefToObjectId, vsanQueryStartTime, vsanQueryEndTime);
      propertyValues.add(propertyValue);
    } 
    return propertyValues;
  }
  
  private Object getVsanMassCollectorPropertyValue(Query query, String nonQualifiedQueryProperty, Map<ManagedObjectReference, String> moRefToObjectId, Long vsanQueryStartTime, Long vsanQueryEndTime) {
    Object result = null;
    try {
      if (QuerySchemaUtil.isQueryPropertyModelKey(nonQualifiedQueryProperty)) {
        result = this._vsanMassCollectorReader.getMoRef();
      } else if ("vsanRetrieveProperties".equals(nonQualifiedQueryProperty)) {
        VsanMassCollectorSpec[] specs = getVsanMassCollectorSpecs(query, moRefToObjectId, vsanQueryStartTime, vsanQueryEndTime);
        if (specs.length > 0)
          result = this._vsanMassCollectorReader.retrieveProperties(specs); 
      } else if ("vsanRetrievePropertiesJson".equals(nonQualifiedQueryProperty)) {
        VsanMassCollectorSpec[] specs = getVsanMassCollectorSpecs(query, moRefToObjectId, vsanQueryStartTime, vsanQueryEndTime);
        if (specs.length > 0) {
          result = this._vsanMassCollectorReader.retrievePropertiesJson(specs);
          result = filterQueryJsonStringResult(result, query);
        } 
      } else if ("vsanMassCollectorSpecs/objectCollection".equals(nonQualifiedQueryProperty)) {
        result = QueryUtil.getFilterPropertyComparableValues(query, "vsanMassCollectorSpecs/objectCollection");
      } else if ("vsanQueryStartTime".equals(nonQualifiedQueryProperty)) {
        result = vsanQueryStartTime;
      } else if ("vsanQueryEndTime".equals(nonQualifiedQueryProperty)) {
        result = vsanQueryEndTime;
      } else if ("objectId".equals(nonQualifiedQueryProperty)) {
        if (!moRefToObjectId.isEmpty())
          result = moRefToObjectId.values().iterator().next(); 
      } else {
        result = QueryUtil.getFilterPropertyComparableValues(query, "vsanMassCollectorSpecs");
      } 
    } catch (Exception e) {
      ExceptionsContextManager.store(e);
      if (_log.isInfoEnabled())
        _log.info("Failed to obtain value for property " + nonQualifiedQueryProperty, e); 
    } 
    return result;
  }
  
  private VsanMassCollectorSpec[] getVsanMassCollectorSpecs(Query query, Map<ManagedObjectReference, String> moRefToObjectId, Long vsanQueryStartTime, Long vsanQueryEndTime) {
    VsanMassCollectorSpec[] vsanMassCollectorSpecs = this._queryConverter.getVsanMassCollectorSpecs(query);
    vsanMassCollectorSpecs = modifyVsanMassCollectorSpecsObjects(vsanMassCollectorSpecs, moRefToObjectId, this._serviceInstanceContentBuilder, this._clusterReader, this._vsanClusterReader, this._hostReader, this._datastoreReader);
    modifyVsanMassCollectorSpecsTime(vsanMassCollectorSpecs, vsanQueryStartTime, vsanQueryEndTime);
    return vsanMassCollectorSpecs;
  }
  
  private static Long getVsanQueryTimePropertyValueFromQueryFilter(Query query, String vsanQueryTimeProperty) {
    List<String> vsanQueryTimeValueInFilter = QueryUtil.getFilterPropertyComparableValues(query, vsanQueryTimeProperty);
    Long vsanQueryTimeValue = null;
    if (!vsanQueryTimeValueInFilter.isEmpty())
      try {
        vsanQueryTimeValue = Long.valueOf(vsanQueryTimeValueInFilter.get(0));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid filter values for property: " + vsanQueryTimeProperty);
      }  
    return vsanQueryTimeValue;
  }
  
  private static void modifyVsanMassCollectorSpecsTime(VsanMassCollectorSpec[] vsanMassCollectorSpecs, Long vsanQueryStartTime, Long vsanQueryEndTime) {
    if (vsanQueryStartTime == null || vsanQueryStartTime == null)
      return; 
    Long nowMillis = Long.valueOf(System.currentTimeMillis());
    VsanMassCollectorSpecTimeModifier vsanMassCollectorSpecTimeModifier = new VsanMassCollectorSpecTimeModifier(nowMillis.longValue() + vsanQueryStartTime.longValue(), nowMillis.longValue() + vsanQueryEndTime.longValue());
    for (VsanMassCollectorSpec vsanMassCollectorSpec : vsanMassCollectorSpecs)
      vsanMassCollectorSpecTimeModifier.synchronizeSpec(vsanMassCollectorSpec); 
  }
  
  private static VsanMassCollectorSpec[] modifyVsanMassCollectorSpecsObjects(VsanMassCollectorSpec[] specs, Map<ManagedObjectReference, String> moRefToObjectId, Builder<ServiceInstanceContent> serviceInstanceContentBuilder, ClusterReader clusterReader, VsanClusterReader vsanClusterReader, HostReader hostReader, DatastoreReader datastoreReader) {
    if (moRefToObjectId.isEmpty())
      return specs; 
    Set<ManagedObjectReference> clusterMoRefs = filterObjectIdToMoRefsByWsdlType(moRefToObjectId, ClusterComputeResource.class.getSimpleName()).keySet();
    if (!clusterMoRefs.isEmpty())
      return modifyVsanMassCollectorSpecsObjectsForCluster(specs, clusterMoRefs, serviceInstanceContentBuilder, clusterReader, vsanClusterReader, hostReader); 
    Set<ManagedObjectReference> isolatedObjectsMoRefs = moRefToObjectId.keySet();
    if (!isolatedObjectsMoRefs.isEmpty())
      return modifyVsanMassCollectorSpecsObjectsForIsolatedObjects(specs, isolatedObjectsMoRefs, serviceInstanceContentBuilder, hostReader, datastoreReader); 
    return new VsanMassCollectorSpec[0];
  }
  
  private static Map<ManagedObjectReference, String> getMoRefToObjectIdFromQueryContext(Query query, QueryContextParser queryContextParser) {
    QueryContext queryContext = QueryContextUtil.getQueryContextFromQueryFilter(query, queryContextParser);
    Map<ManagedObjectReference, String> moRefToObjectId = VmodlQueryContextUtil.getMoRefToObjectIdFromContext(queryContext, "objects");
    return moRefToObjectId;
  }
  
  private static Map<ManagedObjectReference, String> filterObjectIdToMoRefsByWsdlType(Map<ManagedObjectReference, String> moRefToObjectIds, String wsdlType) {
    Map<ManagedObjectReference, String> resultMoRefToObjectIds = new HashMap<>(moRefToObjectIds.size());
    for (Map.Entry<ManagedObjectReference, String> moRefToObjectId : moRefToObjectIds.entrySet()) {
      ManagedObjectReference moRef = moRefToObjectId.getKey();
      if (wsdlType.equals(moRef.getType()))
        resultMoRefToObjectIds.put(moRef, moRefToObjectId.getValue()); 
    } 
    return resultMoRefToObjectIds;
  }
  
  private static VsanMassCollectorSpec[] modifyVsanMassCollectorSpecsObjectsForCluster(VsanMassCollectorSpec[] specs, Set<ManagedObjectReference> clusterMoRefs, Builder<ServiceInstanceContent> serviceInstanceContentBuilder, ClusterReader clusterReader, VsanClusterReader vsanClusterReader, HostReader hostReader) {
    List<VsanMassCollectorSpec> resultSpecs = new ArrayList<>();
    for (VsanMassCollectorSpec spec : specs) {
      if (spec.objectCollection == null) {
        if (spec.objects != null)
          resultSpecs.add(spec); 
      } else {
        List<ManagedObjectReference> objects = null;
        if (spec.objectCollection.equals("VCENTER")) {
          ServiceInstanceContent serviceInstanceContent = (ServiceInstanceContent)serviceInstanceContentBuilder.build();
          objects = Arrays.asList(new ManagedObjectReference[] { serviceInstanceContent.getRootFolder() });
        } else if (spec.objectCollection.equals("SERVICE_INSTANCE")) {
          objects = Arrays.asList(new ManagedObjectReference[] { VimVmodlUtil.SERVICE_INSTANCE_MOREF });
        } else if (spec.objectCollection.contains("CLUSTERS")) {
          boolean isVsanEnabledFilter = spec.objectCollection.contains("VSAN_ENABLED");
          List<ManagedObjectReference> selectedClusters = getSelectedClusters(vsanClusterReader, clusterMoRefs, isVsanEnabledFilter);
          if (!selectedClusters.isEmpty())
            objects = selectedClusters; 
        } else if (spec.objectCollection.contains("HOSTS")) {
          boolean isVsanEnabledFilter = spec.objectCollection.contains("VSAN_ENABLED");
          boolean withoutWitnessFilter = spec.objectCollection.contains("EXCEPT_WITNESS");
          List<ManagedObjectReference> selectedHosts = getSelectedClustersHosts(clusterReader, vsanClusterReader, hostReader, clusterMoRefs, isVsanEnabledFilter, withoutWitnessFilter);
          if (!selectedHosts.isEmpty())
            objects = selectedHosts; 
        } else if (spec.objectCollection.contains("DATASTORES")) {
          List<ManagedObjectReference> selectedDatastores = getSelectedClustersDatastores(clusterReader, clusterMoRefs, spec.objectCollection);
          if (!selectedDatastores.isEmpty())
            objects = selectedDatastores; 
        } 
        if (objects != null) {
          spec.objects = objects.<ManagedObjectReference>toArray(new ManagedObjectReference[objects.size()]);
          spec.objectCollection = null;
          resultSpecs.add(spec);
        } 
      } 
    } 
    return resultSpecs.<VsanMassCollectorSpec>toArray(new VsanMassCollectorSpec[resultSpecs.size()]);
  }
  
  private static VsanMassCollectorSpec[] modifyVsanMassCollectorSpecsObjectsForIsolatedObjects(VsanMassCollectorSpec[] specs, Set<ManagedObjectReference> isolatedObjectsMoRefs, Builder<ServiceInstanceContent> serviceInstanceContentBuilder, HostReader hostReader, DatastoreReader datastoreReader) {
    List<VsanMassCollectorSpec> resultSpecs = new ArrayList<>();
    for (VsanMassCollectorSpec spec : specs) {
      if (spec.objectCollection == null) {
        if (spec.objects != null)
          resultSpecs.add(spec); 
      } else {
        ManagedObjectReference[] objects = null;
        if (spec.objectCollection.equals("VCENTER")) {
          ServiceInstanceContent serviceInstanceContent = (ServiceInstanceContent)serviceInstanceContentBuilder.build();
          objects = new ManagedObjectReference[] { serviceInstanceContent.getRootFolder() };
        } else if (spec.objectCollection.contains("HOSTS")) {
          boolean isVsanEnabled = spec.objectCollection.contains("VSAN_ENABLED");
          boolean withoutWitness = spec.objectCollection.contains("EXCEPT_WITNESS");
          List<ManagedObjectReference> selectedHosts = new ArrayList<>();
          for (ManagedObjectReference objectMoRef : isolatedObjectsMoRefs) {
            if (HOST_SYSTEM_CLASS_SIMPLE_NAME.equals(objectMoRef.getType()) && 
              isConnectedHostMatchingFilters(objectMoRef, isVsanEnabled, withoutWitness, hostReader))
              selectedHosts.add(objectMoRef); 
          } 
          if (!selectedHosts.isEmpty())
            objects = selectedHosts.<ManagedObjectReference>toArray(
                new ManagedObjectReference[selectedHosts.size()]); 
        } else if (spec.objectCollection.contains("DATASTORES")) {
          Map<ManagedObjectReference, String> datastoreMoRefsToTypes = new HashMap<>();
          try {
            datastoreMoRefsToTypes = datastoreReader.getDatastoreMoRefsToTypes();
          } catch (ManagedObjectNotFound e) {
            _log.debug("Could not fetch datastore types info.", (Throwable)e);
          } 
          List<ManagedObjectReference> selectedDatastores = new ArrayList<>();
          for (ManagedObjectReference objectMoRef : isolatedObjectsMoRefs) {
            if (DATASTORE_CLASS_SIMPLE_NAME.equals(objectMoRef.getType()) && 
              isDatastoreMatchingFilters(spec.objectCollection, datastoreMoRefsToTypes
                
                .get(objectMoRef)))
              selectedDatastores.add(objectMoRef); 
          } 
          if (!selectedDatastores.isEmpty())
            objects = selectedDatastores.<ManagedObjectReference>toArray(
                new ManagedObjectReference[selectedDatastores.size()]); 
        } 
        if (objects != null) {
          spec.objects = objects;
          spec.objectCollection = null;
          resultSpecs.add(spec);
        } 
      } 
    } 
    return resultSpecs.<VsanMassCollectorSpec>toArray(new VsanMassCollectorSpec[resultSpecs.size()]);
  }
  
  private static List<ManagedObjectReference> getSelectedClusters(VsanClusterReader vsanClusterReader, Set<ManagedObjectReference> clusterMoRefs, boolean isVsanEnabledFilter) {
    List<ManagedObjectReference> selectedClusters = new ArrayList<>();
    for (ManagedObjectReference clusterMoRef : clusterMoRefs) {
      if (isVsanEnabledFilter) {
        try {
          if (vsanClusterReader.isVsanEnabled(clusterMoRef))
            selectedClusters.add(clusterMoRef); 
        } catch (ManagedObjectNotFound e) {
          _log.debug("Could not check if cluster is vSAN enabled", (Throwable)e);
        } 
        continue;
      } 
      selectedClusters.add(clusterMoRef);
    } 
    return selectedClusters;
  }
  
  private static List<ManagedObjectReference> getSelectedClustersHosts(ClusterReader clusterReader, VsanClusterReader vsanClusterReader, HostReader hostReader, Set<ManagedObjectReference> clusterMoRefs, boolean isVsanEnabledFilter, boolean withoutWitnessFilter) {
    List<ManagedObjectReference> selectedHosts = new ArrayList<>();
    for (ManagedObjectReference clusterMoRef : clusterMoRefs) {
      List<ManagedObjectReference> clusterAssociatedHostMoRefs = new ArrayList<>();
      ManagedObjectReference[] clusterHostMoRefs = getHostsInCluster(clusterReader, clusterMoRef);
      if (clusterHostMoRefs != null) {
        clusterAssociatedHostMoRefs.addAll(Arrays.asList(clusterHostMoRefs));
        _log.debug("Hosts in cluster: " + Arrays.toString((Object[])clusterHostMoRefs));
      } 
      List<ManagedObjectReference> witnessHostsMoRefs = getClusterWitnessHosts(vsanClusterReader, clusterMoRef);
      if (witnessHostsMoRefs != null) {
        clusterAssociatedHostMoRefs.addAll(witnessHostsMoRefs);
        _log.debug("Witness hosts associated with cluster: " + 
            
            Arrays.toString(witnessHostsMoRefs.toArray((Object[])new ManagedObjectReference[0])));
      } 
      for (ManagedObjectReference hostMoRef : clusterAssociatedHostMoRefs) {
        if (isConnectedHostMatchingFilters(hostMoRef, isVsanEnabledFilter, withoutWitnessFilter, hostReader))
          selectedHosts.add(hostMoRef); 
      } 
    } 
    _log.debug("Selected hosts associated with cluster: " + 
        
        Arrays.toString(selectedHosts.toArray((Object[])new ManagedObjectReference[0])));
    return selectedHosts;
  }
  
  private static ManagedObjectReference[] getHostsInCluster(ClusterReader clusterReader, ManagedObjectReference clusterMoRef) {
    ManagedObjectReference[] clusterHostMoRefs = null;
    try {
      clusterHostMoRefs = clusterReader.getClusterHosts(clusterMoRef);
    } catch (ManagedObjectNotFound e) {
      _log.debug("Could not fetch hosts associated with cluster", (Throwable)e);
    } 
    return clusterHostMoRefs;
  }
  
  private static List<ManagedObjectReference> getClusterWitnessHosts(VsanClusterReader vsanClusterReader, ManagedObjectReference clusterMoRef) {
    List<ManagedObjectReference> witnessHostsMoRefs = null;
    try {
      witnessHostsMoRefs = vsanClusterReader.getWitnessHosts(Arrays.asList(new ManagedObjectReference[] { clusterMoRef }));
    } catch (Exception e) {
      _log.debug("Could not fetch witness hosts associated with cluster", e);
    } 
    return witnessHostsMoRefs;
  }
  
  private static List<ManagedObjectReference> getSelectedClustersDatastores(ClusterReader clusterReader, Set<ManagedObjectReference> clusterMoRefs, String objectCollection) {
    Map<String, List<ManagedObjectReference>> allClustersDatastoreTypesToMoRefs = new HashMap<>();
    try {
      allClustersDatastoreTypesToMoRefs = clusterReader.getClustersDatastoresTypeToMoRefs(clusterMoRefs);
    } catch (ManagedObjectNotFound e) {
      _log.debug("Could not fetch datastore associated with cluster", (Throwable)e);
    } 
    List<ManagedObjectReference> selectedDatastores = new ArrayList<>();
    for (Map.Entry<String, List<ManagedObjectReference>> entry : allClustersDatastoreTypesToMoRefs.entrySet()) {
      if (isDatastoreMatchingFilters(objectCollection, entry.getKey()))
        selectedDatastores.addAll(entry.getValue()); 
    } 
    return selectedDatastores;
  }
  
  private static boolean isConnectedHostMatchingFilters(ManagedObjectReference hostMoRef, boolean isVsanEnabledFilter, boolean withoutWitnessFilter, HostReader hostReader) {
    try {
      if (!hostReader.isConnected(hostMoRef))
        return false; 
      if (isVsanEnabledFilter && !hostReader.isVsanEnabled(hostMoRef))
        return false; 
      if (withoutWitnessFilter && hostReader.isVsanWitness(hostMoRef))
        return false; 
    } catch (ManagedObjectNotFound e) {
      _log.debug("Could not fetch data about managed object", (Throwable)e);
      return false;
    } 
    return true;
  }
  
  private static boolean isDatastoreMatchingFilters(String objectCollection, String datastoreType) {
    if (StringUtils.isBlank(objectCollection) || StringUtils.isBlank(datastoreType))
      return false; 
    String objectCollectionToUpperCase = objectCollection.toUpperCase();
    String datastoreTypeToUpperCase = datastoreType.toUpperCase();
    return (objectCollectionToUpperCase.contains("ALL_DATASTORES") || objectCollectionToUpperCase
      
      .contains(datastoreTypeToUpperCase));
  }
  
  private static QuerySchema createQuerySchema() {
    Map<String, QuerySchema.PropertyInfo> propertiesInfo = new TreeMap<>();
    propertiesInfo.put("vsanRetrieveProperties", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanRetrievePropertiesJson", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanMassCollectorSpecs/objectCollection", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanMassCollectorSpecs/objects", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanMassCollectorSpecs/properties", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanMassCollectorSpecs", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanQueryStartTime", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanQueryEndTime", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("filterSpecs", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanMassCollectorSpecs/propertiesParams", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("objectId", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    QuerySchema.ModelInfo modelInfo = new QuerySchema.ModelInfo(propertiesInfo);
    Map<String, QuerySchema.ModelInfo> models = new TreeMap<>();
    models.put("VsanMassCollector", modelInfo);
    return QuerySchema.forModels(models);
  }
  
  private Object filterQueryJsonStringResult(Object result, Query query) {
    if (!(result instanceof String)) {
      if (_log.isWarnEnabled())
        _log.warn("Result cannot be filtered since it is not of the expected type: String, but is: " + result
            
            .getClass()); 
      return result;
    } 
    FilterRule filterRule = createFilterRuleForQuery(query);
    JsonLd resultJsonLd = new JsonLd((String)result);
    filterRule.apply(resultJsonLd);
    return resultJsonLd.toString();
  }
  
  private FilterRule createFilterRuleForQuery(Query query) {
    List<VsanJsonFilterRule> vsanJsonFilterRules = new ArrayList<>();
    List<String> queryFilterRulesStrings = QueryUtil.getFilterPropertyComparableValues(query, "filterSpecs");
    for (String filterRuleString : queryFilterRulesStrings) {
      VsanJsonFilterRule vsanJsonFilterRule = deserializeVsanJsonFilterRule(filterRuleString);
      vsanJsonFilterRules.add(vsanJsonFilterRule);
    } 
    return new FilterRule(vsanJsonFilterRules);
  }
  
  private VsanJsonFilterRule deserializeVsanJsonFilterRule(String ruleJsonString) {
    VsanJsonFilterRule vsanJsonFilterRule = null;
    try {
      vsanJsonFilterRule = (VsanJsonFilterRule)this._serializer.deserialize(new JSONObject(ruleJsonString));
    } catch (Exception e) {
      if (_log.isWarnEnabled())
        _log.warn("Cannot deserialize VsanJsonFilterRule from string: " + ruleJsonString); 
    } 
    return vsanJsonFilterRule;
  }
}

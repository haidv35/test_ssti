package com.vmware.ph.phservice.provider.vsan;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.common.vsan.VsanPerformanceManagerReader;
import com.vmware.ph.phservice.common.vsan.VsanVcClusterConfigReader;
import com.vmware.ph.phservice.common.vsan.VsanVcDiskManagementSystemReader;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.vsan.internal.QueryFilterToVsanPerformanceDataConverter;
import com.vmware.vim.binding.impl.vmodl.KeyAnyValueImpl;
import com.vmware.vim.binding.vim.fault.InvalidLogin;
import com.vmware.vim.binding.vmodl.KeyAnyValue;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vsan.binding.vim.cluster.VsanPerfDiagnoseQuerySpec;
import com.vmware.vim.vsan.binding.vim.cluster.VsanPerfEntityMetricCSV;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VsanPerfDiagnosticsDataProvider implements DataProvider {
  public static final String RESOURCE_MODEL_TYPE = "VsanPerformanceManager";
  
  public static final String PERF_DIAGNOSE_DATA_PROPERTY = "perfDiagnoseData";
  
  public static final String PERF_DIAGNOSE_DATA_JSON_PROPERTY = "perfDiagnoseDataJson";
  
  public static final String PERF_STATS_QUERY_DATA_FILTER_PROPERTY = "perfStatsQueryData";
  
  public static final String CLUSTER_CONTEXT_PROPERTY = "cluster";
  
  public static final String PERF_DIAGNOSE_QUERY_CONTEXT_PROPERTY = "perfDiagnoseQuery";
  
  public static final String TRANSACTION_ID_CONTEXT_PROPERTY = "transactionId";
  
  public static final String LOCALE_CONTEXT_PROPERTY = "locale";
  
  private static final Log _log = LogFactory.getLog(VsanPerfDiagnosticsDataProvider.class);
  
  private final VcClient _vcClient;
  
  private final VsanPerformanceManagerReader _vsanPerformanceManagerReader;
  
  private final VsanVcDiskManagementSystemReader _vsanVcDiskManagementSystemReader;
  
  private final VsanVcClusterConfigReader _vsanVcClusterConfigReader;
  
  private final QueryFilterToVsanPerformanceDataConverter _queryFilterConverter;
  
  private final VmodlToJsonLdSerializer _serializer;
  
  public VsanPerfDiagnosticsDataProvider(VmomiClient vsanHealthVmomiClient, VcClient vcClient, VmodlToJsonLdSerializer serializer, ExecutorService executorService) {
    this(vcClient, new VsanPerformanceManagerReader(vsanHealthVmomiClient, executorService), new VsanVcDiskManagementSystemReader(vsanHealthVmomiClient, vcClient), new VsanVcClusterConfigReader(vsanHealthVmomiClient), new QueryFilterToVsanPerformanceDataConverter(serializer), serializer);
  }
  
  VsanPerfDiagnosticsDataProvider(VcClient vcClient, VsanPerformanceManagerReader vsanPerformanceManagerReader, VsanVcDiskManagementSystemReader vsanVcDiskManagementSystemReader, VsanVcClusterConfigReader vsanVcClusterConfigReader, QueryFilterToVsanPerformanceDataConverter queryFilterConverter, VmodlToJsonLdSerializer serializer) {
    this._vcClient = vcClient;
    this._vsanPerformanceManagerReader = vsanPerformanceManagerReader;
    this._vsanVcDiskManagementSystemReader = vsanVcDiskManagementSystemReader;
    this._vsanVcClusterConfigReader = vsanVcClusterConfigReader;
    this._queryFilterConverter = queryFilterConverter;
    this._serializer = serializer;
  }
  
  public QuerySchema getSchema() {
    QuerySchema querySchema = createQuerySchema();
    return querySchema;
  }
  
  public ResultSet executeQuery(Query query) {
    List<String> queryProperties = query.getProperties();
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(queryProperties);
    List<Object> propertyValues = getPropertyValues(query);
    resultSetBuilder.item(propertyValues.get(0), propertyValues);
    ResultSet resultSet = resultSetBuilder.build();
    return resultSet;
  }
  
  private List<Object> getPropertyValues(Query query) {
    String locale;
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(query.getProperties());
    Map<String, String[]> entityTypeToFields = this._queryFilterConverter.gePerfStatsQueryDataFilterProperty(query, "perfStatsQueryData");
    ManagedObjectReference clusterMoRef = this._queryFilterConverter.<ManagedObjectReference>getContextProperty(query, ManagedObjectReference.class, "cluster");
    VsanPerfDiagnoseQuerySpec perfDiagnoseQuerySpec = this._queryFilterConverter.<VsanPerfDiagnoseQuerySpec>getContextProperty(query, VsanPerfDiagnoseQuerySpec.class, "perfDiagnoseQuery");
    String transactionId = this._queryFilterConverter.<String>getContextProperty(query, String.class, "transactionId");
    try {
      locale = this._queryFilterConverter.<String>getContextProperty(query, String.class, "locale");
    } catch (IllegalArgumentException e) {
      locale = getDefaultLocale();
    } 
    List<Object> propertyValues = new ArrayList();
    for (String nonQualifiedQueryProperty : nonQualifiedQueryProperties) {
      Object propertyValue = getPropertyValue(nonQualifiedQueryProperty, entityTypeToFields, clusterMoRef, perfDiagnoseQuerySpec, transactionId, locale);
      propertyValues.add(propertyValue);
    } 
    return propertyValues;
  }
  
  private Object getPropertyValue(String nonQualifiedQueryProperty, Map<String, String[]> entityTypeToFields, ManagedObjectReference clusterMoRef, VsanPerfDiagnoseQuerySpec perfDiagnoseQuerySpec, String transactionId, String locale) {
    Object<String, String[]> result = null;
    try {
      if (QuerySchemaUtil.isQueryPropertyModelKey(nonQualifiedQueryProperty)) {
        result = (Object<String, String[]>)this._vsanPerformanceManagerReader.getMoRef();
      } else if ("perfDiagnoseData".equals(nonQualifiedQueryProperty)) {
        result = (Object<String, String[]>)getPerfDiagnosisData(entityTypeToFields, clusterMoRef, perfDiagnoseQuerySpec, transactionId, locale);
      } else if ("perfDiagnoseDataJson".equals(nonQualifiedQueryProperty)) {
        KeyAnyValue[] perfDiagnosisData = getPerfDiagnosisData(entityTypeToFields, clusterMoRef, perfDiagnoseQuerySpec, transactionId, locale);
        result = (Object<String, String[]>)this._serializer.serialize(perfDiagnosisData);
      } else if ("perfStatsQueryData".equals(nonQualifiedQueryProperty)) {
        result = (Object<String, String[]>)entityTypeToFields;
      } else if ("cluster".equals(nonQualifiedQueryProperty)) {
        ManagedObjectReference managedObjectReference = clusterMoRef;
      } else if ("perfDiagnoseQuery".equals(nonQualifiedQueryProperty)) {
        VsanPerfDiagnoseQuerySpec vsanPerfDiagnoseQuerySpec = perfDiagnoseQuerySpec;
      } else if ("transactionId".equals(nonQualifiedQueryProperty)) {
        result = (Object<String, String[]>)transactionId;
      } else if ("locale".equals(nonQualifiedQueryProperty)) {
        result = (Object<String, String[]>)locale;
      } 
    } catch (Exception e) {
      ExceptionsContextManager.store(e);
      if (_log.isInfoEnabled())
        _log.info("Failed to obtain value for property " + nonQualifiedQueryProperty, e); 
    } 
    return result;
  }
  
  private KeyAnyValue[] getPerfDiagnosisData(Map<String, String[]> entityTypeToFields, ManagedObjectReference clusterMoRef, VsanPerfDiagnoseQuerySpec perfDiagnoseQuerySpec, String transactionId, String locale) throws Exception {
    List<KeyAnyValue> data = new ArrayList<>();
    KeyAnyValueImpl keyAnyValueImpl1 = new KeyAnyValueImpl();
    keyAnyValueImpl1.setKey("querySpecs");
    keyAnyValueImpl1.setValue(perfDiagnoseQuerySpec);
    data.add(keyAnyValueImpl1);
    VsanPerfEntityMetricCSV[] perfStatsResults = this._vsanPerformanceManagerReader.queryVsanPerf(entityTypeToFields, perfDiagnoseQuerySpec
        
        .getStartTime(), perfDiagnoseQuerySpec
        .getEndTime(), clusterMoRef);
    VsanPerfEntityMetricCSV[] filteredPerfStatsResults = filterEmptyPerfStatsResults(perfStatsResults);
    KeyAnyValueImpl keyAnyValueImpl2 = new KeyAnyValueImpl();
    keyAnyValueImpl2.setKey("perfsvcData");
    keyAnyValueImpl2.setValue(filteredPerfStatsResults);
    data.add(keyAnyValueImpl2);
    KeyAnyValueImpl keyAnyValueImpl3 = new KeyAnyValueImpl();
    keyAnyValueImpl3.setKey("transactionId");
    keyAnyValueImpl3.setValue(transactionId);
    data.add(keyAnyValueImpl3);
    boolean isAllFlash = this._vsanVcDiskManagementSystemReader.isAllFlashCluster(clusterMoRef);
    KeyAnyValueImpl keyAnyValueImpl4 = new KeyAnyValueImpl();
    keyAnyValueImpl4.setKey("clusterType");
    keyAnyValueImpl4.setValue(String.valueOf(isAllFlash));
    data.add(keyAnyValueImpl4);
    KeyAnyValueImpl keyAnyValueImpl5 = new KeyAnyValueImpl();
    keyAnyValueImpl5.setKey("locale");
    keyAnyValueImpl5.setValue(locale);
    data.add(keyAnyValueImpl5);
    String vcenterBuildNumber = this._vcClient.getServiceInstanceContent().getAbout().getBuild();
    KeyAnyValueImpl keyAnyValueImpl6 = new KeyAnyValueImpl();
    keyAnyValueImpl6.setKey("vcenterBuildNumber");
    keyAnyValueImpl6.setValue(vcenterBuildNumber);
    data.add(keyAnyValueImpl6);
    String vcenterVersion = this._vcClient.getServiceInstanceContent().getAbout().getVersion();
    KeyAnyValueImpl keyAnyValueImpl7 = new KeyAnyValueImpl();
    keyAnyValueImpl7.setKey("vcenterVersion");
    keyAnyValueImpl7.setValue(vcenterVersion);
    data.add(keyAnyValueImpl7);
    boolean isDedupEnabled = this._vsanVcClusterConfigReader.isDedupEnabled(clusterMoRef);
    if (isDedupEnabled) {
      KeyAnyValueImpl keyAnyValueImpl = new KeyAnyValueImpl();
      keyAnyValueImpl.setKey("isDedupEnabled");
      keyAnyValueImpl.setValue(Boolean.valueOf(isDedupEnabled));
      data.add(keyAnyValueImpl);
    } 
    String witnessNodeUuid = this._vsanVcClusterConfigReader.getWitnessNodeUuid(clusterMoRef);
    if (witnessNodeUuid != null) {
      KeyAnyValueImpl keyAnyValueImpl = new KeyAnyValueImpl();
      keyAnyValueImpl.setKey("witnessNodeUuid");
      keyAnyValueImpl.setValue(witnessNodeUuid);
      data.add(keyAnyValueImpl);
    } 
    return data.<KeyAnyValue>toArray(new KeyAnyValue[data.size()]);
  }
  
  private static QuerySchema createQuerySchema() {
    Map<String, QuerySchema.PropertyInfo> propertiesInfo = new TreeMap<>();
    propertiesInfo.put("perfDiagnoseData", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("perfDiagnoseDataJson", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("perfStatsQueryData", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("cluster", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("perfDiagnoseQuery", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("transactionId", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    QuerySchema.ModelInfo modelInfo = new QuerySchema.ModelInfo(propertiesInfo);
    Map<String, QuerySchema.ModelInfo> models = new TreeMap<>();
    models.put("VsanPerformanceManager", modelInfo);
    return QuerySchema.forModels(models);
  }
  
  private VsanPerfEntityMetricCSV[] filterEmptyPerfStatsResults(VsanPerfEntityMetricCSV[] perfStatsResults) {
    List<VsanPerfEntityMetricCSV> filteredPerfStatsResults = new ArrayList<>();
    for (VsanPerfEntityMetricCSV perfStatsResult : perfStatsResults) {
      if (perfStatsResult != null && perfStatsResult
        .getValue() != null && (perfStatsResult
        .getValue()).length != 0)
        filteredPerfStatsResults.add(perfStatsResult); 
    } 
    return filteredPerfStatsResults.<VsanPerfEntityMetricCSV>toArray(
        new VsanPerfEntityMetricCSV[filteredPerfStatsResults.size()]);
  }
  
  private String getDefaultLocale() {
    try {
      this._vcClient.login();
    } catch (InvalidLogin|com.vmware.vim.binding.vim.fault.InvalidLocale|com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderException e) {
      String systemDefaultLocale = Locale.getDefault().toString();
      String logMessage = "Could not login to VC to identify locale! Will use the system default locale: " + systemDefaultLocale;
      if (_log.isDebugEnabled()) {
        _log.warn(logMessage, e);
      } else {
        _log.warn(logMessage);
      } 
      return systemDefaultLocale;
    } 
    return this._vcClient.getSessionManager().getCurrentSession().getLocale();
  }
}

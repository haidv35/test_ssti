package com.vmware.ph.phservice.cloud.dataapp.vsan.internal.collector;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.PluginManifestParser;
import com.vmware.ph.phservice.collector.internal.NamedQueryResultSetMapping;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.IndependentResultsMapping;
import com.vmware.ph.phservice.collector.internal.data.NamedQuery;
import com.vmware.ph.phservice.collector.internal.manifest.Manifest;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.provider.vsan.internal.QueryFilterToVsanPerformanceDataConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class VsanPerformanceJsonManifestParser implements PluginManifestParser {
  static final String QUERY_NAME_PREFIX = "vir:VsanPerformanceManager";
  
  static final String CLOUD_PERF_ANALYSIS_PLUGIN_TYPE = "cloud_perf_analysis";
  
  static final String CLOUD_PERF_ANALYSIS_DATA_TYPE = "cloud_perf_analysis";
  
  public boolean isApplicable(String manifestStr) {
    try {
      (new QueryFilterToVsanPerformanceDataConverter.PerfStatsQueryDataParser()).parse(manifestStr);
      return true;
    } catch (Exception e) {
      return false;
    } 
  }
  
  public Set<CollectionSchedule> getManifestSchedules(String manifestStr) {
    return Collections.emptySet();
  }
  
  public Manifest getManifest(String manifestStr, CollectionSchedule schedule) {
    if (!isApplicable(manifestStr))
      return null; 
    String perfStatsQueryData = manifestStr;
    Query query = parseQuery(perfStatsQueryData);
    String queryName = PluginTypeContext.createQueryName("vir:VsanPerformanceManager", new PluginTypeContext("cloud_perf_analysis", "cloud_perf_analysis", true));
    NamedQuery namedQuery = new NamedQuery(query, queryName);
    IndependentResultsMapping independentResultsMapping = VsanHealthJsonManifestParser.buildMapping(Arrays.asList(new NamedQuery[] { namedQuery }));
    Manifest manifest = Manifest.Builder.forQueries(new NamedQuery[] { namedQuery }).withMapping((NamedQueryResultSetMapping)independentResultsMapping).withRecommendedPageSize(5000).build();
    return manifest;
  }
  
  public Map<PluginTypeContext, String> getPluginManifests(String manifestStr) {
    PluginTypeContext pluginTypeContext = new PluginTypeContext("cloud_perf_analysis", "cloud_perf_analysis", true);
    return Collections.singletonMap(pluginTypeContext, manifestStr);
  }
  
  private Query parseQuery(String perfStatsQueryData) {
    Query query = Query.Builder.select(new String[] { "@modelKey", "perfDiagnoseDataJson", "transactionId" }).from(new String[] { "VsanPerformanceManager" }).where(new PropertyPredicate[] { new PropertyPredicate("perfStatsQueryData", PropertyPredicate.ComparisonOperator.EQUAL, perfStatsQueryData) }).orderBy("@modelKey").build();
    return query;
  }
}

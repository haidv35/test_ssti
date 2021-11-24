package com.vmware.ph.phservice.cloud.dataapp.vsan.internal.collector;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.PluginManifestParser;
import com.vmware.ph.phservice.collector.internal.NamedQueryResultSetMapping;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.IndependentResultsMapping;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.ResultSetToPayloadMapping;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.ph.phservice.collector.internal.data.NamedQuery;
import com.vmware.ph.phservice.collector.internal.manifest.Manifest;
import com.vmware.ph.phservice.collector.internal.manifest.scheduling.ScheduleIntervalParser;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VsanHealthJsonManifestParser implements PluginManifestParser {
  private static final Log _log = LogFactory.getLog(VsanHealthJsonManifestParser.class);
  
  public static final CollectionSchedule TEN_MINUTES_SCHEDULE = new CollectionSchedule(600000L);
  
  public static final CollectionSchedule ONE_HOUR_SCHEDULE = new CollectionSchedule(3600000L);
  
  static final int PAGE_SIZE = 5000;
  
  private static final String MANIFEST_SPEC_PROPERTY = "manifest";
  
  private static final String COLLECTION_INTERVAL_PROPERTY = "collectionInterval";
  
  private static final String NETWORK_DIAGNOSTICS_DATA_TYPE = "network_diagnostics_data";
  
  private final VmodlToJsonLdSerializer _serializer;
  
  private final VsanJsonQuerySpecsParser _querySpecsParser;
  
  private final VsanJsonObfuscationRulesParser _obfuscationRulesParser;
  
  public VsanHealthJsonManifestParser(VmodlToJsonLdSerializer serializer) {
    this(serializer, new VsanJsonQuerySpecsParser(), new VsanJsonObfuscationRulesParser());
  }
  
  public VsanHealthJsonManifestParser(VmodlToJsonLdSerializer serializer, VsanJsonQuerySpecsParser querySpecsParser, VsanJsonObfuscationRulesParser obfuscationRulesParser) {
    this._serializer = serializer;
    this._querySpecsParser = querySpecsParser;
    this._obfuscationRulesParser = obfuscationRulesParser;
  }
  
  public boolean isApplicable(String manifestStr) {
    try {
      JSONObject manifestJson = new JSONObject(manifestStr);
      manifestJson.getJSONArray("manifest");
      return true;
    } catch (JSONException e) {
      return false;
    } 
  }
  
  public Manifest getManifest(String manifestStr, CollectionSchedule schedule) {
    try {
      Map<CollectionSchedule, String> scheduleToManifestStr = getScheduleToManifestStr(manifestStr);
      if (schedule == null && !scheduleToManifestStr.isEmpty())
        schedule = scheduleToManifestStr.keySet().iterator().next(); 
      manifestStr = scheduleToManifestStr.get(schedule);
      if (manifestStr == null) {
        _log.info(String.format("Manifest for %s schedule is empty.", new Object[] { schedule }));
        return null;
      } 
      JSONObject manifestJson = new JSONObject(manifestStr);
      Manifest manifest = parse(manifestJson, schedule);
      return manifest;
    } catch (JSONException e) {
      ExceptionsContextManager.store((Throwable)e);
      return null;
    } 
  }
  
  public Set<CollectionSchedule> getManifestSchedules(String manifestStr) {
    Set<CollectionSchedule> manifestSchedules;
    if (isApplicable(manifestStr)) {
      manifestSchedules = getScheduleToManifestStr(manifestStr).keySet();
    } else {
      manifestSchedules = Collections.emptySet();
    } 
    _log.info(
        String.format("The manifest contains the following schedules: %s.", new Object[] { manifestSchedules }));
    return manifestSchedules;
  }
  
  public Map<PluginTypeContext, String> getPluginManifests(String manifestStr) {
    try {
      JSONObject manifestJson = new JSONObject(manifestStr);
      JSONArray pluginManifestJsonArray = manifestJson.getJSONArray("manifest");
      Map<PluginTypeContext, String> pluginContextToManifest = new LinkedHashMap<>(pluginManifestJsonArray.length());
      for (Object pluginManifestObject : pluginManifestJsonArray) {
        JSONObject pluginManifestSpec = (JSONObject)pluginManifestObject;
        PluginTypeContext pluginContext = this._querySpecsParser.createPluginTypeContext(pluginManifestSpec);
        manifestJson.put("manifest", new JSONArray(
              
              Collections.singletonList(pluginManifestSpec)));
        pluginContextToManifest.put(pluginContext, manifestJson.toString());
      } 
      return pluginContextToManifest;
    } catch (JSONException e) {
      ExceptionsContextManager.store((Throwable)e);
      return Collections.emptyMap();
    } 
  }
  
  Manifest parse(JSONObject manifestJson, CollectionSchedule schedule) {
    try {
      List<NamedQuery> queries = this._querySpecsParser.parseQuerySpecs(manifestJson, schedule);
      IndependentResultsMapping independentResultsMapping = buildMapping(queries);
      List<ObfuscationRule> obfuscationRules = this._obfuscationRulesParser.parse(manifestJson, this._serializer);
      Manifest manifest = Manifest.Builder.forQueries(queries.<NamedQuery>toArray(new NamedQuery[queries.size()])).withMapping((NamedQueryResultSetMapping)independentResultsMapping).withObfuscationRules(obfuscationRules).withRecommendedPageSize(5000).build();
      return manifest;
    } catch (JSONException e) {
      ExceptionsContextManager.store((Throwable)e);
      return null;
    } 
  }
  
  static IndependentResultsMapping buildMapping(List<NamedQuery> queries) {
    VsanMassCollectorToJsonLdAndPluginDataMapping vsanItemMapping = new VsanMassCollectorToJsonLdAndPluginDataMapping();
    List<Mapping<NamedPropertiesResourceItem, Collection<JsonLd>>> itemMappings = (List)Collections.singletonList(vsanItemMapping);
    ResultSetToPayloadMapping resultSetToCdfPayloadMapping = new ResultSetToPayloadMapping(itemMappings, null);
    Map<String, Mapping<ResultSet, Payload>> cdfResultSetMappings = new HashMap<>();
    for (NamedQuery query : queries)
      cdfResultSetMappings.put(query.getName(), resultSetToCdfPayloadMapping); 
    IndependentResultsMapping independentResultsMapping = new IndependentResultsMapping(cdfResultSetMappings);
    return independentResultsMapping;
  }
  
  private static Map<CollectionSchedule, String> getScheduleToManifestStr(String manifestStr) {
    JSONObject manifestsJson = new JSONObject(manifestStr);
    Map<CollectionSchedule, List<JSONObject>> scheduleToManifestSpecs = getScheduleToManifestSpecs(manifestsJson);
    Map<CollectionSchedule, String> scheduleToManifestStr = new LinkedHashMap<>(scheduleToManifestSpecs.size());
    for (Map.Entry<CollectionSchedule, List<JSONObject>> scheduleToManifestSpec : scheduleToManifestSpecs.entrySet()) {
      manifestsJson.put("manifest", scheduleToManifestSpec.getValue());
      scheduleToManifestStr.put(scheduleToManifestSpec
          .getKey(), manifestsJson
          .toString());
    } 
    return scheduleToManifestStr;
  }
  
  private static Map<CollectionSchedule, List<JSONObject>> getScheduleToManifestSpecs(JSONObject manifestsJson) {
    Map<CollectionSchedule, List<JSONObject>> scheduleToManifestSpecs = new LinkedHashMap<>();
    for (Object manifestObject : manifestsJson.getJSONArray("manifest")) {
      JSONObject manifestSpec = (JSONObject)manifestObject;
      CollectionSchedule schedule = getSchedule(manifestSpec);
      List<JSONObject> manifestSpecs = scheduleToManifestSpecs.get(schedule);
      if (manifestSpecs == null) {
        manifestSpecs = new ArrayList<>();
        scheduleToManifestSpecs.put(schedule, manifestSpecs);
      } 
      manifestSpecs.add(manifestSpec);
    } 
    return scheduleToManifestSpecs;
  }
  
  private static CollectionSchedule getSchedule(JSONObject manifestJson) {
    CollectionSchedule schedule = ONE_HOUR_SCHEDULE;
    String collectionIntervalStr = manifestJson.optString("collectionInterval", null);
    if (collectionIntervalStr != null) {
      long scheduleIntervalMillis = ScheduleIntervalParser.parseIntervalMillis(collectionIntervalStr);
      schedule = new CollectionSchedule(scheduleIntervalMillis);
    } else {
      String manifestDataType = manifestJson.getString("dataType");
      if ("network_diagnostics_data".equals(manifestDataType))
        schedule = TEN_MINUTES_SCHEDULE; 
    } 
    return schedule;
  }
}

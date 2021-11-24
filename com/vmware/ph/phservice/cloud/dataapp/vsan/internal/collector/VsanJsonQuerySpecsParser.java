package com.vmware.ph.phservice.cloud.dataapp.vsan.internal.collector;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;
import com.vmware.ph.phservice.collector.internal.data.NamedQuery;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;

public class VsanJsonQuerySpecsParser {
  private static final String QUERY_NAME_PREFIX = "vir:VsanMassCollector";
  
  public List<NamedQuery> parseQuerySpecs(JSONObject manifestJson, CollectionSchedule schedule) {
    List<NamedQuery> namedQueries = new ArrayList<>();
    JSONArray pluginTypeManifestJsonArray = manifestJson.getJSONArray("manifest");
    for (Object pluginTypeManifestObject : pluginTypeManifestJsonArray) {
      JSONObject pluginTypeManifestJson = (JSONObject)pluginTypeManifestObject;
      PluginTypeContext pluginTypeContext = createPluginTypeContext(pluginTypeManifestJson);
      JSONArray querySpecs = pluginTypeManifestJson.getJSONArray("querySpecs");
      List<String> querySpecsStrings = new ArrayList<>();
      for (Object querySpec : querySpecs) {
        String querySpecStr = querySpec.toString();
        querySpecsStrings.add(querySpecStr);
      } 
      List<String> filterSpecsStrings = getFilterSpecsStrings(manifestJson);
      Query query = Query.Builder.select(new String[] { "@modelKey", "objectId", "vsanRetrievePropertiesJson" }).from(new String[] { "VsanMassCollector" }).where(new PropertyPredicate[] { new PropertyPredicate("vsanMassCollectorSpecs", PropertyPredicate.ComparisonOperator.EQUAL, querySpecsStrings), new PropertyPredicate("vsanQueryStartTime", PropertyPredicate.ComparisonOperator.EQUAL, getQuerySpecStartTimePropertyFromSchedule(schedule)), new PropertyPredicate("vsanQueryEndTime", PropertyPredicate.ComparisonOperator.EQUAL, "0"), new PropertyPredicate("filterSpecs", PropertyPredicate.ComparisonOperator.EQUAL, filterSpecsStrings) }).orderBy("@modelKey").build();
      String queryName = PluginTypeContext.createQueryName("vir:VsanMassCollector", pluginTypeContext);
      NamedQuery namedQuery = new NamedQuery(query, queryName);
      namedQueries.add(namedQuery);
    } 
    return namedQueries;
  }
  
  public PluginTypeContext createPluginTypeContext(JSONObject pluginTypeManifestJson) {
    String pluginType = pluginTypeManifestJson.getString("pluginType");
    String dataType = pluginTypeManifestJson.getString("dataType");
    boolean supportUserTriggered = pluginTypeManifestJson.optBoolean("supportUserTriggered", true);
    PluginTypeContext pluginTypeContext = new PluginTypeContext(pluginType, dataType, supportUserTriggered);
    return pluginTypeContext;
  }
  
  private String getQuerySpecStartTimePropertyFromSchedule(CollectionSchedule schedule) {
    long querySpecStartTime = schedule.getInterval() + getQuerySpecStartTimeBufferFromSchedule(schedule);
    return "-" + querySpecStartTime;
  }
  
  private static long getQuerySpecStartTimeBufferFromSchedule(CollectionSchedule schedule) {
    long scheduleInterval = schedule.getInterval();
    if (scheduleInterval < TimeUnit.MINUTES.toMillis(15L))
      return TimeUnit.MINUTES.toMillis(5L); 
    if (scheduleInterval < TimeUnit.HOURS.toMillis(12L))
      return TimeUnit.MINUTES.toMillis(15L); 
    return TimeUnit.HOURS.toMillis(1L);
  }
  
  private static List<String> getFilterSpecsStrings(JSONObject manifestJson) {
    List<String> result = new ArrayList<>();
    JSONArray filterSpecs = manifestJson.optJSONArray("filterSpecs");
    if (filterSpecs != null)
      for (Object filterSpec : filterSpecs)
        result.add(filterSpec.toString());  
    return result;
  }
}

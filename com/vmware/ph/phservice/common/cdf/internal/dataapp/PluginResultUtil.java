package com.vmware.ph.phservice.common.cdf.internal.dataapp;

import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PluginResultUtil {
  private static final Log _log = LogFactory.getLog(PluginResultUtil.class);
  
  public static String convertPluginResultsToJson(List<PluginResult> pluginResults) {
    JSONObject resultJson = new JSONObject();
    if (pluginResults == null) {
      resultJson.put("results", Collections.emptyList());
      return resultJson.toString();
    } 
    for (PluginResult pluginResult : pluginResults) {
      JSONObject responseJson = new JSONObject();
      responseJson.put("objectId", pluginResult.getObjectId());
      Object content = pluginResult.getContent();
      if (content instanceof String) {
        responseJson.put("content", content);
      } else {
        try {
          responseJson.put("content", new JSONObject(content));
        } catch (JSONException e) {
          responseJson.put("content", content.toString());
        } 
      } 
      responseJson.put("processedDate", pluginResult.getTimestamp().getTime());
      resultJson.append("results", responseJson);
    } 
    return resultJson.toString();
  }
  
  public static List<PluginResult> parsePluginResults(String collectorId, String collectorInstanceId, String responseBody) {
    if (responseBody == null)
      return null; 
    JSONObject responseJson = new JSONObject(responseBody);
    JSONArray jsonArray = responseJson.optJSONArray("results");
    if (jsonArray == null || jsonArray.length() == 0) {
      if (_log.isDebugEnabled())
        _log.debug("Empty response '" + responseBody + "' retrieved when querying result DAP plugin [" + collectorId + ", " + collectorInstanceId + "]."); 
      return null;
    } 
    List<PluginResult> results = new ArrayList<>(jsonArray.length());
    for (Object object : jsonArray) {
      JSONObject responseData = (JSONObject)object;
      String objectId = responseData.getString("objectId");
      Object content = responseData.get("content");
      Long timestamp = Long.valueOf(responseData.getLong("processedDate"));
      PluginResult result = new PluginResult(objectId, content, new Date(timestamp.longValue()));
      results.add(result);
    } 
    return results;
  }
}

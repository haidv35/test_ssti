package com.vmware.ph.phservice.cloud.health.dataapp;

import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class DataAppResultToVmodlObjectConverter {
  private static final Log _log = LogFactory.getLog(DataAppResultToVmodlObjectConverter.class);
  
  private final VmodlToJsonLdSerializer _serializer;
  
  public DataAppResultToVmodlObjectConverter(VmodlToJsonLdSerializer serializer) {
    this._serializer = serializer;
  }
  
  public <T> T convertToVmodlObject(PluginResult pluginResult) {
    if (pluginResult == null)
      return null; 
    JSONObject resultContentJson = null;
    if (pluginResult.getContent() != null)
      try {
        resultContentJson = new JSONObject((String)pluginResult.getContent());
      } catch (JSONException e) {
        if (_log.isWarnEnabled())
          _log.warn(
              String.format("Could not convert content of PluginResult:  \"%s\" to JSON: %s", new Object[] { pluginResult.getContent(), e })); 
      }  
    T vmodlObject = null;
    try {
      vmodlObject = (T)this._serializer.deserialize(resultContentJson);
    } catch (Exception e) {
      if (_log.isWarnEnabled())
        _log.warn(
            String.format("Could not deserialize PluginResult content JSON: %s to type: %s", new Object[] { resultContentJson, e })); 
    } 
    return vmodlObject;
  }
}

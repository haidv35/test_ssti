package com.vmware.ph.phservice.provider.common.vmomi;

import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import com.vmware.ph.phservice.provider.common.QueryContext;
import com.vmware.ph.phservice.provider.common.QueryContextParser;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

public class VmodlJsonLdQueryContextParser extends QueryContextParser {
  private static final Log _log = LogFactory.getLog(VmodlJsonLdQueryContextParser.class);
  
  private final VmodlToJsonLdSerializer _serializer;
  
  public VmodlJsonLdQueryContextParser(VmodlToJsonLdSerializer serializer) {
    this._serializer = serializer;
  }
  
  public QueryContext parse(Object contextData) {
    QueryContext queryContext = super.parse(contextData);
    if (queryContext == null && contextData instanceof String) {
      Map<String, List<Object>> keyToVmodlObjects = new LinkedHashMap<>();
      JSONObject jsonContextObject = new JSONObject((String)contextData);
      Iterator<String> keys = jsonContextObject.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        Object jsonLdValue = jsonContextObject.get(key);
        try {
          List<Object> result;
          Object vmodlValue = this._serializer.deserialize(jsonLdValue);
          if (vmodlValue instanceof Object[]) {
            result = Arrays.asList((Object[])vmodlValue);
          } else {
            result = Arrays.asList(new Object[] { vmodlValue });
          } 
          keyToVmodlObjects.put(key, result);
        } catch (Exception e) {
          _log.warn("Unable to deserialize key: " + key + "with value: " + jsonLdValue, e);
        } 
      } 
      queryContext = new QueryContext(keyToVmodlObjects);
    } 
    return queryContext;
  }
}

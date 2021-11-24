package com.vmware.ph.phservice.common.cdf.internal.jsonld20;

import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONObjectUtil {
  public static JSONObject getOrCreateHolder(JSONObject root, String path, char separator) {
    return getOrCreateHolderInternal(root, path, 0, separator);
  }
  
  static JSONObject getOrCreateHolderInternal(JSONObject currentRoot, String fullPath, int propertyIndex, char separator) {
    boolean isLastProperty = (getPropertyCount(fullPath, separator) - 1 == propertyIndex);
    if (!isLastProperty) {
      JSONObject deepestHolder;
      String currentProperty = getProperty(propertyIndex, fullPath, separator);
      Object o = currentRoot.opt(currentProperty);
      if (o instanceof JSONObject) {
        deepestHolder = getOrCreateHolderInternal((JSONObject)o, fullPath, propertyIndex + 1, separator);
      } else if (null == o || JSONObject.NULL.equals(o)) {
        JSONObject j = new JSONObject();
        currentRoot.put(currentProperty, j);
        deepestHolder = getOrCreateHolderInternal(j, fullPath, propertyIndex + 1, separator);
      } else {
        throw new IllegalArgumentException("I was traversing path " + fullPath + " and I was expecting either JSONObject or nothing(null) at '" + currentProperty + "' in JSON " + currentRoot

            
            .toString(2) + "\n but I've found " + o
            .getClass().getName() + " with value " + o);
      } 
      return deepestHolder;
    } 
    return currentRoot;
  }
  
  static int getPropertyCount(String propPath, char separator) {
    StringBuilder charSequence = new StringBuilder();
    charSequence.append(separator);
    int res = 1 + propPath.length() - propPath.replace(charSequence, "").length();
    return res;
  }
  
  static String getProperty(int idxProperty, String propPath, char separator) {
    int posAfterSeparator = 0;
    for (int i = 0; i < idxProperty; i++) {
      posAfterSeparator = 1 + propPath.indexOf(separator, posAfterSeparator);
      if (0 == posAfterSeparator)
        return null; 
    } 
    int posNextSeparator = propPath.indexOf(separator, posAfterSeparator);
    if (-1 == posNextSeparator)
      posNextSeparator = propPath.length(); 
    String res = propPath.substring(posAfterSeparator, posNextSeparator);
    return res;
  }
  
  public static JSONArray convertJsonStringsToJsonArray(Iterable<String> jsonsForConversion) {
    JSONArray jsonArray = new JSONArray();
    for (String singleJsonForConversion : jsonsForConversion)
      jsonArray.put(new JSONObject(singleJsonForConversion)); 
    return jsonArray;
  }
  
  public static String convertJsonStringsToJsonArrayString(Iterable<String> inputJsonStrings) {
    int jsonArrayStringLength = getJsonArrayStringLength(inputJsonStrings);
    StringBuilder stringBuilder = new StringBuilder(jsonArrayStringLength);
    stringBuilder.append("[");
    Iterator<String> jsonStringsIterator = inputJsonStrings.iterator();
    while (jsonStringsIterator.hasNext()) {
      stringBuilder.append(jsonStringsIterator.next());
      if (jsonStringsIterator.hasNext())
        stringBuilder.append(","); 
    } 
    stringBuilder.append("]");
    return stringBuilder.toString();
  }
  
  public static int getJsonArrayStringLength(Iterable<String> inputJsonStrings) {
    int resultStringLength = 1;
    Iterator<String> inputJsonStringsIterator = inputJsonStrings.iterator();
    while (inputJsonStringsIterator.hasNext()) {
      resultStringLength += ((String)inputJsonStringsIterator.next()).length();
      if (inputJsonStringsIterator.hasNext())
        resultStringLength++; 
    } 
    resultStringLength++;
    return resultStringLength;
  }
  
  public static String getFirstJsonObjectFromJsonString(Iterable<String> inputJsonStrings) {
    if (inputJsonStrings == null || !inputJsonStrings.iterator().hasNext())
      return null; 
    String jsonString = inputJsonStrings.iterator().next();
    if (StringUtils.isBlank(jsonString))
      return null; 
    Object json = (new JSONTokener(jsonString)).nextValue();
    if (json instanceof JSONObject)
      return json.toString(); 
    if (json instanceof JSONArray) {
      JSONArray jsonArray = (JSONArray)json;
      if (jsonArray.length() == 0)
        return null; 
      if (jsonArray.length() == 1)
        return jsonArray.getJSONObject(0).toString(); 
      return jsonArray.toString();
    } 
    return null;
  }
  
  public static String buildJsonArrayStringFromJsonLds(Collection<JsonLd> jsonLds) {
    Collection<String> jsonLdStrings = new ArrayList<>();
    for (JsonLd jsonLd : jsonLds)
      jsonLdStrings.add(jsonLd.toString()); 
    String jsonArrayString = convertJsonStringsToJsonArrayString(jsonLdStrings);
    return jsonArrayString;
  }
}

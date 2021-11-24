package com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.ph.phservice.common.internal.JsonUtil;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.util.introspection.SecureUberspector;

public final class VelocityHelper {
  public static final Log log = LogFactory.getLog(VelocityHelper.class);
  
  private static final String GLOBAL_PREDEFINED_PREFIX = "GLOBAL-";
  
  private static final String GLOBAL_EMPTY = "GLOBAL-EMPTY";
  
  private static final String GLOBAL_Integer = "GLOBAL-" + Integer.class.getSimpleName();
  
  private static final String GLOBAL_logger = "GLOBAL-logger";
  
  private static final String GLOBAL_System = "GLOBAL-System";
  
  private static final String GLOBAL_Arrays = "GLOBAL-" + Arrays.class.getSimpleName();
  
  private static final String GLOBAL_Collections = "GLOBAL-" + Collections.class.getSimpleName();
  
  private static final String GLOBAL_DigestUtils = "GLOBAL-" + DigestUtils.class.getSimpleName();
  
  private static final String GLOBAL_TelemetryCryptographyUtil = "GLOBAL-" + TelemetryCryptographyUtil.class
    .getSimpleName();
  
  private static final String GLOBAL_CalendarUtil = "GLOBAL-" + CalendarUtil.class
    .getSimpleName();
  
  private static final String GLOBAL_JsonUtil = "GLOBAL-" + JsonUtil.class
    .getSimpleName();
  
  private static final String LOCAL_PREDEFINED_PREFIX = "LOCAL-";
  
  private static final String LOCAL_OUTPUT = "LOCAL-output";
  
  private static final String LOCAL_RESOURCE_ITEM = "LOCAL-resourceItem";
  
  private static final String MOREF_OBJECT = "moref-object";
  
  private static final String MOREF_OBJECT_SERVER_GUID = "moref-serverGuid";
  
  private static final String MOREF_OBJECT_VALUE = "moref-value";
  
  private static final String MOREF_OBJECT_TYPE = "moref-type";
  
  public static final String LOCAL_CDF20RESULT = "LOCAL-cdf20Result";
  
  private static final Map<String, Object> predefinedVelocityObjects;
  
  private static final String EMPTY_STRING_RESULT = "";
  
  private static byte[] telemetrySaltBytesArray;
  
  static {
    Map<String, Object> m = new TreeMap<>();
    m.put("GLOBAL-EMPTY", null);
    m.put(GLOBAL_DigestUtils, DigestUtils.class);
    m.put(GLOBAL_TelemetryCryptographyUtil, TelemetryCryptographyUtil.class);
    m.put(GLOBAL_Collections, Collections.class);
    m.put(GLOBAL_Arrays, Arrays.class);
    m.put("GLOBAL-System", SystemWrapper.class);
    m.put("GLOBAL-logger", log);
    m.put(GLOBAL_CalendarUtil, CalendarUtil.class);
    m.put(GLOBAL_Integer, Integer.class);
    m.put(GLOBAL_JsonUtil, JsonUtil.class);
    predefinedVelocityObjects = Collections.unmodifiableMap(m);
  }
  
  public static VelocityContext createVelocityContextWithPredefinedGlobalObjects() {
    VelocityContext context = new VelocityContext();
    updateContextWith(context, predefinedVelocityObjects);
    return context;
  }
  
  public static VelocityEngine createVelocityEngine() {
    Properties properties = new Properties();
    properties.setProperty("runtime.references.strict", "true");
    properties.setProperty("runtime.references.strict.escape", "true");
    properties.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
    properties.setProperty("runtime.introspector.uberspect", SecureUberspector.class.getName());
    return new VelocityEngine(properties);
  }
  
  public static void updateContextWith(VelocityContext context, Map<String, Object> objectsToAdd) {
    for (Map.Entry<String, Object> ent : objectsToAdd.entrySet())
      updateContextWith(context, ent.getKey(), ent.getValue()); 
  }
  
  public static void updateContextWith(VelocityContext context, String key, Object value) {
    StringBuilder sb = new StringBuilder(key.length());
    for (char c : key.toCharArray()) {
      if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9') && c != '_' && c != '-')
        c = '-'; 
      sb.append(c);
    } 
    context.put(sb.toString(), value);
  }
  
  public static String executeVelocityExpression(String expression, VelocityEngine velocityEngine, VelocityContext substitutor, String logTag) {
    try {
      StringWriter dummySw = new StringWriter();
      velocityEngine.evaluate((Context)substitutor, dummySw, logTag, expression);
      return dummySw.toString();
    } catch (VelocityException e) {
      if (log.isDebugEnabled())
        log.debug(
            String.format("Returning null for input expression %s, because of VelocityException. Exception is: %s", new Object[] { expression, ExceptionUtils.getMessage((Throwable)e) })); 
      return null;
    } 
  }
  
  public static Map<String, Object> prepareVelocityPropertiesForResourceItemsThatMayContainMoref(ResourceItem input) {
    Map<String, Object> attributes = new TreeMap<>();
    Object resourceObject = input.getPropertyValues().get(0);
    if (resourceObject instanceof ManagedObjectReference) {
      ManagedObjectReference moref = (ManagedObjectReference)resourceObject;
      attributes.put("moref-object", moref);
      attributes.put("moref-type", (moref == null) ? null : moref.getType());
      attributes.put("moref-value", (moref == null) ? null : moref.getValue());
      attributes.put("moref-serverGuid", (moref == null) ? null : moref.getServerGuid());
    } 
    return attributes;
  }
  
  public static Map<String, Object> prepareVelocityPropertiesFromResourceItem(NamedPropertiesResourceItem input) {
    Map<String, Object> attributes = new TreeMap<>();
    List<String> propertyNames = input.getActualPropertyNames();
    List<Object> propertyValues = input.getActualPropertyValues();
    for (int i = 0; i < propertyNames.size(); i++)
      attributes.put(propertyNames.get(i), propertyValues.get(i)); 
    attributes.put("LOCAL-resourceItem", input);
    return attributes;
  }
  
  public static Map<String, Object> evaluateAttributes(Map<String, String> attributePatterns, VelocityEngine velocityEngine, VelocityContext context) {
    Map<String, Object> res = new LinkedHashMap<>();
    Set<Map.Entry<String, String>> attribs = attributePatterns.entrySet();
    for (Map.Entry<String, String> attr : attribs) {
      String attrName = attr.getKey();
      String attrMapping = attr.getValue();
      Object rawValue = context.get(attrMapping);
      if (rawValue != null) {
        res.put(attrName, rawValue);
        continue;
      } 
      if (attrMapping.contains("$")) {
        String value = executeVelocityExpression(attrMapping, velocityEngine, context, attrName);
        if (value != null) {
          res.put(attrName, value);
          continue;
        } 
        if (log.isDebugEnabled())
          log.debug("Cannot evaluate the value of attribute '" + attrName + "' - it's mapping (from the manifest) is: " + attrMapping); 
      } 
    } 
    return res;
  }
  
  public static void updateContextWith(VelocityContext context, NamedPropertiesResourceItem resourceItem) {
    updateContextWith(context, "LOCAL-resourceItem", resourceItem);
  }
  
  public static void updateContextWith(VelocityContext context, Payload.Builder payloadBuilder) {
    updateContextWith(context, "LOCAL-output", payloadBuilder);
  }
  
  public static void setTelemetrySalt(String telemetrySalt) {
    if (!StringUtils.isBlank(telemetrySalt)) {
      try {
        telemetrySaltBytesArray = Hex.decodeHex(telemetrySalt.toCharArray());
      } catch (DecoderException e) {
        log.warn("Failed to decode the telemetry salt.", (Throwable)e);
      } 
    } else {
      log.warn("No telemetry salt is available. Hashing data with a salt will not be possible");
    } 
  }
  
  public static class TelemetryCryptographyUtil {
    public static String sha256HexWithSalt(String data) {
      if (VelocityHelper.telemetrySaltBytesArray == null) {
        VelocityHelper.log.debug("No salt is available. Cannot hash data.");
        return "";
      } 
      try {
        byte[] dataBytesArray = data.getBytes(StandardCharsets.UTF_8.name());
        byte[] dataWithSaltByteArray = new byte[dataBytesArray.length + VelocityHelper.telemetrySaltBytesArray.length];
        System.arraycopy(dataBytesArray, 0, dataWithSaltByteArray, 0, dataBytesArray.length);
        System.arraycopy(VelocityHelper
            .telemetrySaltBytesArray, 0, dataWithSaltByteArray, dataBytesArray.length, VelocityHelper


            
            .telemetrySaltBytesArray.length);
        return DigestUtils.sha256Hex(dataWithSaltByteArray);
      } catch (UnsupportedEncodingException e) {
        VelocityHelper.log.warn("Failed to hash data.", e);
        return "";
      } 
    }
  }
}

package com.vmware.ph.phservice.common.vapi.util;

import java.net.URI;
import org.apache.commons.lang.StringUtils;

public final class VapiUriSchemeUtil {
  public static final char PACKAGE_SEPARATOR = '.';
  
  private static final String URI_PREFIX = "urn:vapi:";
  
  private static final char TYPE_SEPARATOR = ':';
  
  public static URI createUri(String type, String id) {
    if (StringUtils.isBlank(type))
      throw new IllegalArgumentException("Resource type must not be null or empty"); 
    if (type.indexOf(':') >= 0)
      throw new IllegalArgumentException(String.format("Resource type must not contain colons: '%s'", new Object[] { type })); 
    if (StringUtils.isBlank(id))
      throw new IllegalArgumentException("vAPI ID must not be null or empty"); 
    if (type.indexOf('.') <= 0)
      throw new IllegalArgumentException(String.format("Cannot create an URI resource reference for a VMODL1 resource type (i.e. resource type is not qualified with package name): '%s'", new Object[] { type })); 
    return URI.create("urn:vapi:" + type + ':' + id);
  }
  
  public static String getId(URI uri) {
    validateVapiUri(uri);
    String uriString = uri.toString();
    int typeSeparatorIndex = getTypeSeparatorIndex(uriString);
    return uriString.substring(typeSeparatorIndex + 1);
  }
  
  public static String getType(URI uri) {
    validateVapiUri(uri);
    String uriString = uri.toString();
    int typeSeparatorIndex = getTypeSeparatorIndex(uriString);
    return uriString.substring("urn:vapi:".length(), typeSeparatorIndex);
  }
  
  public static boolean isVapiUri(URI uri) {
    if (uri == null)
      throw new IllegalArgumentException("URI must not be null"); 
    return uri.toString().startsWith("urn:vapi:");
  }
  
  private static void validateVapiUri(URI uri) {
    if (!isVapiUri(uri))
      throw new IllegalArgumentException("Not a vAPI URI: " + uri.toString()); 
  }
  
  private static int getTypeSeparatorIndex(String vapiUriString) {
    int typeSeparatorIndex = vapiUriString.indexOf(':', "urn:vapi:"
        .length());
    if (typeSeparatorIndex < 0 || typeSeparatorIndex >= vapiUriString
      .length() - 1)
      throw new IllegalArgumentException("Invalid vAPI URI: " + vapiUriString); 
    return typeSeparatorIndex;
  }
}

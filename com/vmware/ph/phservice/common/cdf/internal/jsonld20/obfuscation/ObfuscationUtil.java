package com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation;

import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationCache;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;

public class ObfuscationUtil {
  public static final String DEFAULT_SUBSTRING_SPLIT_PATTERN = "[:._\\-\\s]";
  
  public static final String DEFAULT_OBFUSCATED_SUBSTRING_DELIMITER = "-";
  
  public static String obfuscateString(String inputString, ObfuscationCache obfuscationCache) {
    String obfuscatedString = DigestUtils.shaHex(inputString.getBytes(Charset.forName("UTF-8")));
    if (obfuscationCache != null)
      obfuscationCache.storeDeobfuscated(obfuscatedString, inputString); 
    return obfuscatedString;
  }
  
  public static String obfuscateSubstring(String inputString, String substringSplitPattern, String obfuscatedSubstringDelimiter, ObfuscationCache obfuscationCache) {
    String[] substrings = inputString.split(substringSplitPattern);
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(obfuscateString(substrings[0], null));
    for (int i = 1; i < substrings.length; i++) {
      stringBuilder.append(obfuscatedSubstringDelimiter);
      stringBuilder.append(obfuscateString(substrings[i], null));
    } 
    String obfuscatedString = stringBuilder.toString();
    if (obfuscationCache != null)
      obfuscationCache.storeDeobfuscated(obfuscatedString, inputString); 
    return obfuscatedString;
  }
  
  public static String obfuscateMatchingStrings(String inputString, Pattern regexPattern, boolean shouldObfuscateSubstrings, String substringSplitPattern, String obfuscatedSubstringDelimiter, ObfuscationCache obfuscationCache) {
    Matcher matcher = regexPattern.matcher(inputString);
    StringBuffer obfuscatedStringBuffer = new StringBuffer(inputString.length());
    while (matcher.find()) {
      String replacement, matchingString = matcher.group();
      if (shouldObfuscateSubstrings) {
        replacement = obfuscateSubstring(matchingString, substringSplitPattern, obfuscatedSubstringDelimiter, obfuscationCache);
      } else {
        replacement = obfuscateString(matchingString, obfuscationCache);
      } 
      matcher.appendReplacement(obfuscatedStringBuffer, replacement);
    } 
    matcher.appendTail(obfuscatedStringBuffer);
    return obfuscatedStringBuffer.toString();
  }
}

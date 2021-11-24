package com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation;

import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationCache;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationException;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationRule;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class RegexBasedRule implements ObfuscationRule {
  private static final Pattern JSON_OBJECT_END_REGEX = Pattern.compile("(?<=})");
  
  private Pattern _pattern = null;
  
  private final boolean _shouldObfuscateSubstrings;
  
  private final String _substringSplitPattern;
  
  private final String _obfuscatedSubstringDelimiter;
  
  public RegexBasedRule(String patternString) throws ObfuscationException {
    this(patternString, false);
  }
  
  public RegexBasedRule(String patternString, boolean shouldObfuscateSubstrings) throws ObfuscationException {
    this(patternString, shouldObfuscateSubstrings, "[:._\\-\\s]", "-");
  }
  
  public RegexBasedRule(String patternString, boolean shouldObfuscateSubstrings, String substringSplitPattern, String obfuscatedSubstringDelimiter) throws ObfuscationException {
    try {
      if (patternString != null)
        this._pattern = Pattern.compile(patternString); 
    } catch (PatternSyntaxException e) {
      throw new ObfuscationException("Could not create obfuscation rule, because regex pattern could not be compiled: " + patternString, e);
    } 
    this._shouldObfuscateSubstrings = shouldObfuscateSubstrings;
    this._substringSplitPattern = substringSplitPattern;
    this._obfuscatedSubstringDelimiter = obfuscatedSubstringDelimiter;
  }
  
  public Object apply(Object input) {
    return apply(input, null);
  }
  
  public Object apply(Object input, ObfuscationCache obfuscationCache) {
    if (input == null)
      return null; 
    if (!(input instanceof JsonLd) || this._pattern == null)
      return input; 
    String obfuscatedJsonLdString = obfuscateJsonLdStringInParts(input.toString(), obfuscationCache);
    ((JsonLd)input).setJsonString(obfuscatedJsonLdString);
    return input;
  }
  
  public boolean getShouldObfuscateSubstrings() {
    return this._shouldObfuscateSubstrings;
  }
  
  private String obfuscateJsonLdStringInParts(String input, ObfuscationCache obfuscationCache) {
    String[] unobfuscatedSubstrings = JSON_OBJECT_END_REGEX.split(input);
    StringBuilder obfuscatedSubstrings = new StringBuilder();
    for (int i = 0; i < unobfuscatedSubstrings.length; i++) {
      String unobfuscatedSubstring = unobfuscatedSubstrings[i];
      String obfuscatedSubstring = ObfuscationUtil.obfuscateMatchingStrings(unobfuscatedSubstring, this._pattern, this._shouldObfuscateSubstrings, this._substringSplitPattern, this._obfuscatedSubstringDelimiter, obfuscationCache);
      obfuscatedSubstrings.append(obfuscatedSubstring);
      unobfuscatedSubstrings[i] = null;
    } 
    return obfuscatedSubstrings.toString();
  }
}

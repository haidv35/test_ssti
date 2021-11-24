package com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation;

public class SubstringObfuscationSpec {
  private final boolean _obfuscateBySubstring;
  
  private final String _substringSplitPattern;
  
  private final String _obfuscatedSubstringDelimiter;
  
  public SubstringObfuscationSpec(boolean obfuscateBySubstring) {
    this(obfuscateBySubstring, "[:._\\-\\s]", "-");
  }
  
  public SubstringObfuscationSpec(boolean obfuscateBySubstring, String substringSplitPattern, String obfuscatedSubstringDelimiter) {
    this._obfuscateBySubstring = obfuscateBySubstring;
    this._substringSplitPattern = substringSplitPattern;
    this._obfuscatedSubstringDelimiter = obfuscatedSubstringDelimiter;
  }
  
  public boolean shouldObfuscateBySubstrings() {
    return this._obfuscateBySubstring;
  }
  
  public String getSubstringSplitPattern() {
    return this._substringSplitPattern;
  }
  
  public String getObfuscatedSubstringDelimiter() {
    return this._obfuscatedSubstringDelimiter;
  }
}

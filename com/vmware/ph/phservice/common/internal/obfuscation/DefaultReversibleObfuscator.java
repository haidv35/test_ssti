package com.vmware.ph.phservice.common.internal.obfuscation;

import java.util.Map;

public class DefaultReversibleObfuscator extends DefaultObfuscator implements ReversibleObfuscator {
  private static final int DEFAULT_CACHE_EXPIRATION_MILLIS = 3600000;
  
  private final ObfuscationCache _obfuscationCache;
  
  public DefaultReversibleObfuscator() {
    this._obfuscationCache = new ObfuscationCache(3600000L);
  }
  
  public DefaultReversibleObfuscator(long cacheExpirationMillis) {
    this._obfuscationCache = new ObfuscationCache(cacheExpirationMillis);
  }
  
  public Object deobfuscate(Object input) {
    if (input == null)
      return null; 
    if (input instanceof String)
      return this._obfuscationCache.retrieveDeobfuscated((String)input); 
    return input;
  }
  
  protected Object applyObfuscationRule(Object input, ObfuscationRule rule) throws ObfuscationException {
    return rule.apply(input, this._obfuscationCache);
  }
  
  public void invalidate() {
    this._obfuscationCache.invalidate();
  }
  
  public Map<String, String> exportObfuscationMap() {
    Map<String, String> obfuscationMap = this._obfuscationCache.exportObfuscationCache();
    return obfuscationMap;
  }
}

package com.vmware.ph.phservice.common.internal.obfuscation;

import com.vmware.ph.phservice.common.internal.cache.Cache;
import com.vmware.ph.phservice.common.internal.cache.SimpleTimeBasedCacheImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ObfuscationCache {
  private static final int DEFAULT_INITIAL_CACHE_CAPACITY = 1024;
  
  private static final Pattern OBFUSCATED_KEY_PATTERN = Pattern.compile("[0-9|a-f]{40}");
  
  private final Cache<String, String> _lookupTable;
  
  public ObfuscationCache(long cacheExpirationMillis) {
    this._lookupTable = new SimpleTimeBasedCacheImpl<>(cacheExpirationMillis, 1024);
  }
  
  public void storeDeobfuscated(String obfuscatedString, String actualString) {
    if (actualString != null && obfuscatedString != null)
      this._lookupTable.put(obfuscatedString, actualString); 
  }
  
  public String retrieveDeobfuscated(String obfuscatedString) {
    if (obfuscatedString == null)
      return null; 
    String deobfuscatedString = String.valueOf(obfuscatedString);
    List<String> allLookupKeys = new ArrayList<>(this._lookupTable.getAllKeys());
    for (String key : allLookupKeys) {
      if (isObfuscated(key))
        deobfuscatedString = deobfuscatedString.replaceAll(key, this._lookupTable.get(key)); 
    } 
    return deobfuscatedString;
  }
  
  public Map<String, String> exportObfuscationCache() {
    return this._lookupTable.export();
  }
  
  public void invalidate() {
    this._lookupTable.invalidate();
  }
  
  private boolean isObfuscated(String key) {
    return OBFUSCATED_KEY_PATTERN.matcher(key).find();
  }
}

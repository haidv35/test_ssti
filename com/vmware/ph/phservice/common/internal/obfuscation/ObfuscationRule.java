package com.vmware.ph.phservice.common.internal.obfuscation;

public interface ObfuscationRule {
  Object apply(Object paramObject) throws ObfuscationException;
  
  Object apply(Object paramObject, ObfuscationCache paramObfuscationCache) throws ObfuscationException;
}

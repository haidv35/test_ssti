package com.vmware.ph.phservice.common.internal.obfuscation;

import java.util.List;

public interface ReversibleObfuscator extends Obfuscator {
  Object obfuscate(Object paramObject, List<ObfuscationRule> paramList);
  
  Object deobfuscate(Object paramObject);
}

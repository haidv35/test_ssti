package com.vmware.ph.phservice.common.internal.obfuscation;

import java.util.List;

public interface Obfuscator {
  Object obfuscate(Object paramObject, List<ObfuscationRule> paramList);
}

package com.vmware.ph.phservice.common.internal.obfuscation;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultObfuscator implements Obfuscator {
  private static final Log _log = LogFactory.getLog(DefaultObfuscator.class);
  
  public Object obfuscate(Object input, List<ObfuscationRule> rules) {
    if (input == null || rules == null || rules.isEmpty())
      return input; 
    Object result = input;
    for (ObfuscationRule rule : rules) {
      long obfuscationRuleStartTimestamp = System.currentTimeMillis();
      try {
        result = applyObfuscationRule(result, rule);
      } catch (ObfuscationException e) {
        if (_log.isErrorEnabled())
          _log.error("Could not obfuscate input object, returning null.", e); 
        return null;
      } finally {
        if (_log.isDebugEnabled())
          _log.debug(
              String.format("Obfuscator took %d milliseconds to apply %s.", new Object[] { Long.valueOf(System.currentTimeMillis() - obfuscationRuleStartTimestamp), rule
                  .getClass().getSimpleName() })); 
      } 
    } 
    return result;
  }
  
  protected Object applyObfuscationRule(Object input, ObfuscationRule rule) throws ObfuscationException {
    return rule.apply(input);
  }
}

package com.vmware.ph.phservice.collector.internal.cdf.mapping;

import com.vmware.ph.phservice.provider.common.internal.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SafeMappingWrapper<IN, OUT> implements Mapping<IN, OUT> {
  private static final Log _log = LogFactory.getLog(SafeMappingWrapper.class);
  
  private final Mapping<IN, OUT> _wrappedMapping;
  
  public SafeMappingWrapper(Mapping<IN, OUT> wrappedMapping) {
    this._wrappedMapping = wrappedMapping;
  }
  
  public OUT map(IN input, Context context) {
    try {
      return this._wrappedMapping.map(input, context);
    } catch (Throwable t) {
      _log.error("Error while executing object mapping. Returning null.", t);
      return null;
    } 
  }
}

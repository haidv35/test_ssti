package com.vmware.ph.phservice.collector.internal.cdf.mapping;

import java.util.Map;

public interface QueryResultToDictionaryConverter<INPUT> {
  Map<String, Object> createDictionary(INPUT paramINPUT);
}

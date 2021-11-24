package com.vmware.ph.phservice.provider.common;

import java.util.List;

public interface DataRetriever<T> {
  List<T> retrieveData();
  
  String getKey(T paramT);
}

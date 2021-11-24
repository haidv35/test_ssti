package com.vmware.ph.phservice.common;

import java.io.Closeable;
import java.util.List;

public interface ItemsStream<T> extends Closeable {
  List<T> read(int paramInt) throws IllegalArgumentException;
  
  int getLimit();
}

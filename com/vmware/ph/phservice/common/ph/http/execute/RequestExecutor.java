package com.vmware.ph.phservice.common.ph.http.execute;

import java.io.Closeable;
import org.apache.http.client.methods.HttpUriRequest;

public interface RequestExecutor extends Closeable {
  int executeRequest(HttpUriRequest paramHttpUriRequest) throws Exception;
}

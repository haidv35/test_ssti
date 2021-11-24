package com.vmware.ph.phservice.cloud.dataapp.internal;

public interface ProgressReporter {
  void reportProgress(int paramInt);
  
  void reportSuccess() throws Exception;
  
  void reportFailure(Exception paramException) throws Exception;
}

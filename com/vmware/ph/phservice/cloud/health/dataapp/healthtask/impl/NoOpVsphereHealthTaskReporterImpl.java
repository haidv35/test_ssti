package com.vmware.ph.phservice.cloud.health.dataapp.healthtask.impl;

import com.vmware.ph.phservice.cloud.health.dataapp.healthtask.VsphereHealthTaskReporter;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.Locale;

public class NoOpVsphereHealthTaskReporterImpl implements VsphereHealthTaskReporter {
  public void triggerTask(ManagedObjectReference moRef, Locale locale, String sessionUser) {}
  
  public void reportProgress(int percentDone) {}
  
  public void reportSuccess() {}
  
  public void reportFailure(Exception error) {}
  
  public void close() {}
}

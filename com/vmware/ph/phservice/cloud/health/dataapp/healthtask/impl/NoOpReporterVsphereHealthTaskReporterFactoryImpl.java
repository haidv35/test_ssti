package com.vmware.ph.phservice.cloud.health.dataapp.healthtask.impl;

import com.vmware.ph.phservice.cloud.health.dataapp.healthtask.VsphereHealthTaskReporter;
import com.vmware.ph.phservice.cloud.health.dataapp.healthtask.VsphereHealthTaskReporterFactory;
import com.vmware.ph.phservice.common.internal.i18n.LocalizedMessageProvider;

public class NoOpReporterVsphereHealthTaskReporterFactoryImpl implements VsphereHealthTaskReporterFactory {
  public VsphereHealthTaskReporter createVsphereHealthTaskReporter(LocalizedMessageProvider localizedMessageProvider) {
    return new NoOpVsphereHealthTaskReporterImpl();
  }
}

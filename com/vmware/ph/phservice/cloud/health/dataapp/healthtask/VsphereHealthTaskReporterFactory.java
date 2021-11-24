package com.vmware.ph.phservice.cloud.health.dataapp.healthtask;

import com.vmware.ph.phservice.common.internal.i18n.LocalizedMessageProvider;

public interface VsphereHealthTaskReporterFactory {
  VsphereHealthTaskReporter createVsphereHealthTaskReporter(LocalizedMessageProvider paramLocalizedMessageProvider);
}

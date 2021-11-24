package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.upload.service.UploadServiceBuilder;

public interface PhEnvironmentProvider {
  UploadServiceBuilder.Environment getEnvironment();
}

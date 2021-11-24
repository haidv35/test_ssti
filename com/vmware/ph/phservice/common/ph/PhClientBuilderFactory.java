package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.client.api.commondataformat.dimensions.Collector;
import com.vmware.ph.client.api.impl.PhClientBuilder;
import com.vmware.ph.upload.service.UploadServiceBuilder;

public class PhClientBuilderFactory {
  public PhClientBuilder create(UploadServiceBuilder.Environment environment, Collector collector) {
    return PhClientBuilder.create(environment, collector);
  }
}

package com.vmware.ph.phservice.collector.core;

import com.vmware.ph.phservice.collector.internal.core.BaseCollectorBuilder;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cdf.PayloadUploader;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;

public class CollectorBuilder extends BaseCollectorBuilder<CollectorBuilder> {
  public CollectorBuilder(CollectorAgentProvider collectorAgentProvider, Builder<DataProvidersConnection> dataProvidersConnectionBuilder, ManifestContentProvider manifestContentProvider, PayloadUploader payloadUploader) {
    super(collectorAgentProvider, dataProvidersConnectionBuilder, true, manifestContentProvider, payloadUploader);
  }
  
  public CollectorBuilder(CollectorAgentProvider collectorAgentProvider, ManifestContentProvider manifestContentProvider) {
    super(collectorAgentProvider, null, false, manifestContentProvider, null);
  }
  
  protected CollectorBuilder getThis() {
    return this;
  }
}

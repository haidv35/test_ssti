package com.vmware.ph.phservice.collector.internal.core;

import com.vmware.ph.phservice.collector.core.CollectorAgentProvider;
import com.vmware.ph.phservice.collector.internal.cdf.QueryServicePayloadCollector;
import com.vmware.ph.phservice.common.internal.obfuscation.Obfuscator;
import com.vmware.ph.phservice.provider.common.internal.ContextFactory;

public class UsageDataCollectorBuilder {
  private final ContextFactory _contextFactory;
  
  private final CollectorAgentProvider _collectorAgentProvider;
  
  private final Obfuscator _obfuscator;
  
  private boolean _shouldUploadCollectionCompletedPayload;
  
  public UsageDataCollectorBuilder(ContextFactory contextFactory, CollectorAgentProvider collectorAgentProvider, Obfuscator obfuscator) {
    this._contextFactory = contextFactory;
    this._collectorAgentProvider = collectorAgentProvider;
    this._obfuscator = obfuscator;
  }
  
  public UsageDataCollectorBuilder shouldUploadCollectionCompletedPayload(boolean shouldUploadCollectionCompletedPayload) {
    this._shouldUploadCollectionCompletedPayload = shouldUploadCollectionCompletedPayload;
    return this;
  }
  
  public UsageDataCollector build() {
    QueryServicePayloadCollector queryServiceCollector = new QueryServicePayloadCollector(this._obfuscator);
    UsageDataCollector phUsageDataCollector = new UsageDataCollector(queryServiceCollector, this._contextFactory, this._collectorAgentProvider, this._shouldUploadCollectionCompletedPayload);
    return phUsageDataCollector;
  }
}

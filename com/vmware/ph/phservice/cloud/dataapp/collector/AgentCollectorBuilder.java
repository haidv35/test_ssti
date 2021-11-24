package com.vmware.ph.phservice.cloud.dataapp.collector;

import com.vmware.ph.phservice.cloud.dataapp.internal.collector.SpecsCollector;
import com.vmware.ph.phservice.collector.core.Collector;
import com.vmware.ph.phservice.collector.core.CollectorAgentProvider;
import com.vmware.ph.phservice.collector.core.QueryServiceConnection;
import com.vmware.ph.phservice.collector.internal.core.BaseCollectorBuilder;
import com.vmware.ph.phservice.collector.internal.manifest.ManifestParser;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import java.util.List;

public class AgentCollectorBuilder extends BaseCollectorBuilder<AgentCollectorBuilder> {
  private final List<CollectionSpec> _collectionSpecs;
  
  public AgentCollectorBuilder(CollectorAgentProvider collectorAgentProvider, Builder<DataProvidersConnection> dataProvidersConnectionBuilder, List<CollectionSpec> collectionSpecs) {
    super(collectorAgentProvider, dataProvidersConnectionBuilder, true, null, null);
    this._collectionSpecs = collectionSpecs;
  }
  
  public AgentCollectorBuilder(CollectorAgentProvider collectorAgentProvider, ManifestContentProvider manifestContentProvider) {
    super(collectorAgentProvider, null, false, manifestContentProvider, null);
    this._collectionSpecs = null;
  }
  
  protected Collector createCollector(CollectorAgentProvider collectorAgentProvider, QueryServiceConnection queryServiceConnection, CollectionSchedule collectionSchedule, ManifestParser manifestParser) {
    Collector collector = (this._collectionSpecs != null) ? new SpecsCollector(queryServiceConnection, collectorAgentProvider, this._collectionSpecs, collectionSchedule, manifestParser) : super.createCollector(collectorAgentProvider, queryServiceConnection, collectionSchedule, manifestParser);
    return collector;
  }
  
  protected AgentCollectorBuilder getThis() {
    return this;
  }
}

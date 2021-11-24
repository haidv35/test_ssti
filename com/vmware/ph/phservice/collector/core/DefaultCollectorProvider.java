package com.vmware.ph.phservice.collector.core;

import com.vmware.ph.config.ceip.CeipConfigProvider;
import com.vmware.ph.phservice.collector.internal.core.PermitControlledCollectorWrapper;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cdf.PayloadUploader;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultCollectorProvider implements CollectorProvider {
  private static final Log _log = LogFactory.getLog(DefaultCollectorProvider.class);
  
  private final CeipConfigProvider _ceipConfigProvider;
  
  private final CollectorAgentProvider _collectorAgentProvider;
  
  private final ManifestContentProvider _manifestContentProvider;
  
  private final Builder<DataProvidersConnection> _dataProvidersConnectionBuilder;
  
  private final PayloadUploader _payloadUploader;
  
  private final boolean _isLegacySchedulingSupported;
  
  private QueryServiceConnectionFactory _queryServiceConnectionFactory;
  
  private Semaphore _collectorSemaphore;
  
  private Long _permitTimeoutInMillis;
  
  public DefaultCollectorProvider(CeipConfigProvider ceipConfigProvider, CollectorAgentProvider collectorAgentProvider, ManifestContentProvider manifestContentProvider, Builder<DataProvidersConnection> dataProvidersConnectionBuilder, PayloadUploader payloadUploader, boolean isLegacySchedulingSupported) {
    this._ceipConfigProvider = ceipConfigProvider;
    this._collectorAgentProvider = collectorAgentProvider;
    this._manifestContentProvider = manifestContentProvider;
    this._dataProvidersConnectionBuilder = dataProvidersConnectionBuilder;
    this._payloadUploader = payloadUploader;
    this._isLegacySchedulingSupported = isLegacySchedulingSupported;
  }
  
  public void setQueryServiceConnectionFactory(QueryServiceConnectionFactory queryServiceConnectionFactory) {
    this._queryServiceConnectionFactory = queryServiceConnectionFactory;
  }
  
  public void setCollectorSemaphore(Semaphore collectorSemaphore) {
    this._collectorSemaphore = collectorSemaphore;
  }
  
  public void setPermitTimeoutInMillis(Long permitTimeoutInMillis) {
    this._permitTimeoutInMillis = permitTimeoutInMillis;
  }
  
  public boolean isActive() {
    DataProvidersConnection connection = (DataProvidersConnection)this._dataProvidersConnectionBuilder.build();
    if (connection == null)
      return false; 
    connection.close();
    return true;
  }
  
  public Collector getCollector(CollectionSchedule collectionSchedule) {
    if (_log.isDebugEnabled())
      _log.debug("Retrieve the CEIP status."); 
    boolean isCeipEnabled = this._ceipConfigProvider.isCeipEnabled();
    if (!isCeipEnabled) {
      if (_log.isInfoEnabled())
        _log.info("Skipping collection because usage data collector is not enabled."); 
      return null;
    } 
    CollectorBuilder collectorBuilder = new CollectorBuilder(this._collectorAgentProvider, this._dataProvidersConnectionBuilder, this._manifestContentProvider, this._payloadUploader);
    collectorBuilder
      .withCollectionSchedule(collectionSchedule)
      .withLegacySchedulingSupport(this._isLegacySchedulingSupported);
    if (this._queryServiceConnectionFactory != null)
      collectorBuilder.withQueryServiceConnectionFactory(this._queryServiceConnectionFactory); 
    Collector collector = collectorBuilder.build();
    if (this._collectorSemaphore != null)
      collector = new PermitControlledCollectorWrapper(collector, this._collectorSemaphore, this._permitTimeoutInMillis.longValue()); 
    return collector;
  }
  
  public Set<CollectionSchedule> getCollectorSchedules() {
    CollectorBuilder collectorBuilder = new CollectorBuilder(this._collectorAgentProvider, this._manifestContentProvider);
    collectorBuilder
      .withLegacySchedulingSupport(this._isLegacySchedulingSupported);
    Set<CollectionSchedule> manifestSchedules = collectorBuilder.getCollectorSchedules();
    return manifestSchedules;
  }
}

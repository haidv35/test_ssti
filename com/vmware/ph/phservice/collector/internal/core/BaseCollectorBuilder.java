package com.vmware.ph.phservice.collector.internal.core;

import com.vmware.ph.phservice.collector.core.Collector;
import com.vmware.ph.phservice.collector.core.CollectorAgentProvider;
import com.vmware.ph.phservice.collector.core.DefaultQueryServiceConnectionFactory;
import com.vmware.ph.phservice.collector.core.QueryServiceConnection;
import com.vmware.ph.phservice.collector.core.QueryServiceConnectionFactory;
import com.vmware.ph.phservice.collector.core.impl.CollectorImpl;
import com.vmware.ph.phservice.collector.internal.manifest.ManifestParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.XmlManifestParser;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cdf.PayloadUploader;
import com.vmware.ph.phservice.common.internal.obfuscation.DefaultObfuscator;
import com.vmware.ph.phservice.common.internal.obfuscation.Obfuscator;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseCollectorBuilder<B extends BaseCollectorBuilder<B>> implements Builder<Collector> {
  private static final Log _log = LogFactory.getLog(BaseCollectorBuilder.class);
  
  private final CollectorAgentProvider _collectorAgentProvider;
  
  private final Builder<DataProvidersConnection> _dataProvidersConnectionBuilder;
  
  private final boolean _shouldCloseConnection;
  
  private final ManifestContentProvider _manifestContentProvider;
  
  private final PayloadUploader _payloadUploader;
  
  private Object _contextData;
  
  private QueryServiceConnectionFactory _queryServiceConnectionFactory = new DefaultQueryServiceConnectionFactory();
  
  private ManifestParser _manifestParser;
  
  private Obfuscator _obfuscator = (Obfuscator)new DefaultObfuscator();
  
  private CollectionSchedule _collectionSchedule = CollectionSchedule.WEEKLY;
  
  private boolean _isLegacySchedulingSupported = false;
  
  private boolean _shouldUploadCollectionCompletedPayload = false;
  
  public BaseCollectorBuilder(CollectorAgentProvider collectorAgentProvider, Builder<DataProvidersConnection> dataProvidersConnectionBuilder, boolean shouldCloseConnection, ManifestContentProvider manifestContentProvider, PayloadUploader payloadUploader) {
    this._collectorAgentProvider = collectorAgentProvider;
    this._dataProvidersConnectionBuilder = dataProvidersConnectionBuilder;
    this._shouldCloseConnection = shouldCloseConnection;
    this._manifestContentProvider = manifestContentProvider;
    this._payloadUploader = payloadUploader;
  }
  
  public B withQueryServiceConnectionFactory(QueryServiceConnectionFactory queryServiceConnectionFactory) {
    this
      ._queryServiceConnectionFactory = Objects.<QueryServiceConnectionFactory>requireNonNull(queryServiceConnectionFactory);
    return getThis();
  }
  
  public B withManifestParser(ManifestParser manifestParser) {
    this._manifestParser = manifestParser;
    return getThis();
  }
  
  public B withContextData(Object contextData) {
    this._contextData = contextData;
    return getThis();
  }
  
  public B withObfuscator(Obfuscator obfuscator) {
    this._obfuscator = obfuscator;
    return getThis();
  }
  
  public B withCollectionSchedule(CollectionSchedule schedule) {
    this._collectionSchedule = schedule;
    return getThis();
  }
  
  public B withLegacySchedulingSupport(boolean isSupported) {
    this._isLegacySchedulingSupported = isSupported;
    return getThis();
  }
  
  public B withUploadCollectionCompletedPayload(boolean shouldUploadCollectionCompletedPayload) {
    this._shouldUploadCollectionCompletedPayload = shouldUploadCollectionCompletedPayload;
    return getThis();
  }
  
  public Collector build() {
    QueryServiceConnection queryServiceConnection = this._queryServiceConnectionFactory.createQueryServiceConnection(this._dataProvidersConnectionBuilder);
    if (queryServiceConnection == null)
      return null; 
    ManifestParser manifestParser = getManifestParser();
    Collector collector = createCollector(this._collectorAgentProvider, queryServiceConnection, this._collectionSchedule, manifestParser);
    if (this._shouldCloseConnection)
      collector = new ConnectionClosingCollectorWrapper(collector, queryServiceConnection); 
    return collector;
  }
  
  public Set<CollectionSchedule> getCollectorSchedules() {
    String manifestContent = getManifestContentForCollectionSchedules();
    if (manifestContent == null) {
      _log.info("Could not obtain the collector manifest. Will return an empty set of schedules");
      return Collections.emptySet();
    } 
    ManifestParser manifestParser = getManifestParser();
    Set<CollectionSchedule> manifestSchedules = manifestParser.getManifestSchedules(manifestContent);
    if (_log.isDebugEnabled())
      _log.debug(
          String.format("Collection will run with the following schedules: %s", new Object[] { manifestSchedules })); 
    return manifestSchedules;
  }
  
  protected ManifestParser getManifestParser() {
    ManifestParser manifestParser = this._manifestParser;
    if (manifestParser == null)
      manifestParser = new XmlManifestParser(this._isLegacySchedulingSupported); 
    return manifestParser;
  }
  
  protected String getManifestContentForCollectionSchedules() {
    try {
      CollectorAgentProvider.CollectorAgentId agentId = this._collectorAgentProvider.getCollectorAgentId();
      String manifestContent = this._manifestContentProvider.getManifestContent(agentId
          .getCollectorId(), agentId
          .getCollectorInstanceId());
      return manifestContent;
    } catch (com.vmware.ph.phservice.common.manifest.ManifestContentProvider.ManifestException e) {
      _log.error("Cannot obtain the content of the collector manifest");
      return null;
    } 
  }
  
  protected Collector createCollector(CollectorAgentProvider collectorAgentProvider, QueryServiceConnection queryServiceConnection, CollectionSchedule collectionSchedule, ManifestParser manifestParser) {
    Collector collector = new CollectorImpl(queryServiceConnection, collectorAgentProvider, this._manifestContentProvider, this._payloadUploader, this._obfuscator, collectionSchedule, manifestParser, this._shouldUploadCollectionCompletedPayload);
    if (this._contextData != null)
      ((CollectorImpl)collector).setContextData(this._contextData); 
    return collector;
  }
  
  protected abstract B getThis();
}

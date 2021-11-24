package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;
import com.vmware.ph.phservice.collector.internal.data.NamedQuery;
import com.vmware.ph.phservice.collector.internal.manifest.Manifest;
import com.vmware.ph.phservice.collector.internal.manifest.ManifestParser;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CollectionTriggerTypeManifestParser implements ManifestParser {
  private final ManifestParser _wrappedManifestParser;
  
  private final CollectionTriggerType _collectionTriggerType;
  
  public CollectionTriggerTypeManifestParser(ManifestParser wrappedManifestParser, CollectionTriggerType collectionTriggerType) {
    this._wrappedManifestParser = wrappedManifestParser;
    this._collectionTriggerType = collectionTriggerType;
  }
  
  public boolean isApplicable(String manifestStr) {
    return this._wrappedManifestParser.isApplicable(manifestStr);
  }
  
  public Manifest getManifest(String manifestStr, CollectionSchedule schedule) {
    Manifest manifest = this._wrappedManifestParser.getManifest(manifestStr, schedule);
    if (manifest == null)
      return null; 
    if (this._collectionTriggerType != CollectionTriggerType.ON_DEMAND)
      return manifest; 
    NamedQuery[] queries = manifest.getQueries();
    NamedQuery[] onDemandQueries = filterOnDemandQueries(queries);
    manifest = Manifest.Builder.forManifestWithNewQueries(manifest, onDemandQueries).build();
    return manifest;
  }
  
  public Set<CollectionSchedule> getManifestSchedules(String manifestStr) {
    return this._wrappedManifestParser.getManifestSchedules(manifestStr);
  }
  
  private static NamedQuery[] filterOnDemandQueries(NamedQuery[] queries) {
    List<NamedQuery> onDemandQueries = new ArrayList<>();
    if (queries != null)
      for (NamedQuery namedQuery : queries) {
        boolean isOnDemandSupported = true;
        String queryName = namedQuery.getName();
        PluginTypeContext pluginTypeContext = PluginTypeContext.getContextFromQueryName(queryName);
        if (pluginTypeContext != null)
          isOnDemandSupported = pluginTypeContext.isUserTriggerSupported(); 
        if (isOnDemandSupported)
          onDemandQueries.add(namedQuery); 
      }  
    return onDemandQueries.<NamedQuery>toArray(new NamedQuery[onDemandQueries.size()]);
  }
}

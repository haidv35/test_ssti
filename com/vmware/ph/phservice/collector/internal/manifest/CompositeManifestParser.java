package com.vmware.ph.phservice.collector.internal.manifest;

import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import java.util.List;
import java.util.Set;

public class CompositeManifestParser implements ManifestParser {
  private final List<ManifestParser> _manifestParsers;
  
  public CompositeManifestParser(List<ManifestParser> manifestParsers) {
    this._manifestParsers = manifestParsers;
  }
  
  public boolean isApplicable(String manifestStr) {
    ManifestParser manifestParser = getApplicableManifestParser(manifestStr);
    return (manifestParser != null);
  }
  
  public Manifest getManifest(String manifestStr, CollectionSchedule schedule) {
    ManifestParser manifestParser = getApplicableManifestParser(manifestStr);
    if (manifestParser != null)
      return manifestParser.getManifest(manifestStr, schedule); 
    return null;
  }
  
  public Set<CollectionSchedule> getManifestSchedules(String manifestStr) {
    ManifestParser manifestParser = getApplicableManifestParser(manifestStr);
    if (manifestParser != null)
      return manifestParser.getManifestSchedules(manifestStr); 
    return null;
  }
  
  protected ManifestParser getApplicableManifestParser(String manifestStr) {
    ManifestParser resultManifestParser = null;
    for (ManifestParser manifestParser : this._manifestParsers) {
      if (manifestParser.isApplicable(manifestStr)) {
        resultManifestParser = manifestParser;
        break;
      } 
    } 
    return resultManifestParser;
  }
}

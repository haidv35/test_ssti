package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;
import com.vmware.ph.phservice.collector.internal.manifest.CompositeManifestParser;
import com.vmware.ph.phservice.collector.internal.manifest.ManifestParser;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PluginCompositeManifestParser extends CompositeManifestParser implements PluginManifestParser {
  public PluginCompositeManifestParser(List<ManifestParser> manifestParsers) {
    super(manifestParsers);
  }
  
  public Map<PluginTypeContext, String> getPluginManifests(String manifestStr) {
    ManifestParser manifestParser = getApplicableManifestParser(manifestStr);
    if (manifestParser instanceof PluginManifestParser)
      return ((PluginManifestParser)manifestParser).getPluginManifests(manifestStr); 
    return Collections.emptyMap();
  }
}

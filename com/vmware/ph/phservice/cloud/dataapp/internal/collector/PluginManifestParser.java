package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;
import com.vmware.ph.phservice.collector.internal.manifest.ManifestParser;
import java.util.Map;

public interface PluginManifestParser extends ManifestParser {
  Map<PluginTypeContext, String> getPluginManifests(String paramString);
}

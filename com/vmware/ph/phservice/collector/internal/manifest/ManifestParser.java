package com.vmware.ph.phservice.collector.internal.manifest;

import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import java.util.Set;

public interface ManifestParser {
  boolean isApplicable(String paramString);
  
  Manifest getManifest(String paramString, CollectionSchedule paramCollectionSchedule);
  
  Set<CollectionSchedule> getManifestSchedules(String paramString);
}

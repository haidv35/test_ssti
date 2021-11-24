package com.vmware.ph.phservice.provider.spbm.collector.dataretriever;

import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import com.vmware.vim.binding.pbm.profile.CapabilityBasedProfile;
import com.vmware.vim.binding.pbm.profile.Profile;
import com.vmware.vim.binding.pbm.profile.ProfileId;
import com.vmware.vim.binding.pbm.profile.ProfileManager;
import com.vmware.vim.binding.pbm.profile.ResourceType;
import com.vmware.vim.binding.pbm.profile.ResourceTypeEnum;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.core.impl.BlockingFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CapabilityBasedProfileRetriever implements DataRetriever<CapabilityBasedProfile> {
  private static final Log log = LogFactory.getLog(CapabilityBasedProfileRetriever.class);
  
  private final PbmServiceClient _pbmClient;
  
  public CapabilityBasedProfileRetriever(SpbmCollectorContext collectorContext) {
    this._pbmClient = collectorContext.getPbmServiceClient();
  }
  
  public List<CapabilityBasedProfile> retrieveData() {
    List<CapabilityBasedProfile> capabilityBasedProfile = Collections.emptyList();
    try {
      if (log.isDebugEnabled())
        log.debug("Creating ProfileManager stub to retrieve CapabilityBasedProfile"); 
      ProfileManager profileManager = this._pbmClient.getProfileManager();
      BlockingFuture blockingFuture = new BlockingFuture();
      ResourceType resourceType = new ResourceType();
      resourceType.setResourceType(ResourceTypeEnum.STORAGE.toString());
      profileManager.queryProfile(resourceType, null, (Future)blockingFuture);
      ProfileId[] profileIds = (ProfileId[])blockingFuture.get();
      if (profileIds != null) {
        BlockingFuture blockingFuture1 = new BlockingFuture();
        profileManager.retrieveContent(profileIds, (Future)blockingFuture1);
        Profile[] profiles = (Profile[])blockingFuture1.get();
        if (profiles != null) {
          capabilityBasedProfile = new ArrayList<>(profiles.length);
          for (Profile profile : profiles)
            capabilityBasedProfile.add((CapabilityBasedProfile)profile); 
        } 
      } else if (log.isDebugEnabled()) {
        log.debug("No capability profiles in the environment.");
      } 
    } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
      log.warn("Error occured while retrieving CapabilityBasedProfile using ProfileManager.retrieveContent() API", e);
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return capabilityBasedProfile;
  }
  
  public String getKey(CapabilityBasedProfile t) {
    return t.getProfileId().getUniqueId();
  }
}

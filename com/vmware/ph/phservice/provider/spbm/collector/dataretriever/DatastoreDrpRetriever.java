package com.vmware.ph.phservice.provider.spbm.collector.dataretriever;

import com.vmware.ph.phservice.common.vim.vc.util.DatastoreReader;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import com.vmware.vim.binding.pbm.placement.PlacementHub;
import com.vmware.vim.binding.pbm.profile.DefaultProfileInfo;
import com.vmware.vim.binding.pbm.profile.ProfileManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.core.impl.BlockingFuture;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DatastoreDrpRetriever implements DataRetriever<DefaultProfileInfo> {
  private static final Log log = LogFactory.getLog(DatastoreDrpRetriever.class);
  
  private final PbmServiceClient _pbmClient;
  
  private final List<String> _objectDatastoreTypes = Arrays.asList(new String[] { "VSAN", "VVOL" });
  
  public DatastoreDrpRetriever(SpbmCollectorContext collectorContext) {
    this._pbmClient = collectorContext.getPbmServiceClient();
  }
  
  public List<DefaultProfileInfo> retrieveData() {
    List<DefaultProfileInfo> defaultProfileInfos = Collections.emptyList();
    List<ManagedObjectReference> datastoreMoRefs = DatastoreReader.getDatastoreMoRefs(this._pbmClient
        .getVcClient(), this._objectDatastoreTypes);
    if (datastoreMoRefs.isEmpty())
      return defaultProfileInfos; 
    PlacementHub[] placementHubs = new PlacementHub[datastoreMoRefs.size()];
    int count = 0;
    for (ManagedObjectReference datastoreMoRef : datastoreMoRefs) {
      PlacementHub hub = new PlacementHub();
      hub.setHubType("Datastore");
      hub.setHubId(datastoreMoRef.getValue());
      placementHubs[count] = hub;
      count++;
    } 
    try {
      ProfileManager profileManager = this._pbmClient.getProfileManager();
      BlockingFuture blockingFuture = new BlockingFuture();
      profileManager.queryDefaultRequirementProfiles(placementHubs, (Future)blockingFuture);
      DefaultProfileInfo[] defaultProfileInfoArray = (DefaultProfileInfo[])blockingFuture.get();
      if (defaultProfileInfoArray != null)
        defaultProfileInfos = Arrays.asList(defaultProfileInfoArray); 
    } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
      log.warn("Error occurred while retrieving DefaultRequirementProfiles", e);
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return defaultProfileInfos;
  }
  
  public String getKey(DefaultProfileInfo t) {
    return t.getDefaultProfile().getProfileId().getUniqueId();
  }
}

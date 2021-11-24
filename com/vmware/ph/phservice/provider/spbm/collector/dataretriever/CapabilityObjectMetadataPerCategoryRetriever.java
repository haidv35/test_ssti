package com.vmware.ph.phservice.provider.spbm.collector.dataretriever;

import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import com.vmware.vim.binding.pbm.capability.provider.CapabilityObjectMetadataPerCategory;
import com.vmware.vim.binding.pbm.profile.ProfileManager;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.core.impl.BlockingFuture;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CapabilityObjectMetadataPerCategoryRetriever implements DataRetriever<CapabilityObjectMetadataPerCategory> {
  private static final Log log = LogFactory.getLog(CapabilityObjectMetadataPerCategoryRetriever.class);
  
  private final PbmServiceClient _pbmClient;
  
  public CapabilityObjectMetadataPerCategoryRetriever(SpbmCollectorContext collectorContext) {
    this._pbmClient = collectorContext.getPbmServiceClient();
  }
  
  public List<CapabilityObjectMetadataPerCategory> retrieveData() {
    List<CapabilityObjectMetadataPerCategory> capabilityObjectMetadataPerCategory = Collections.emptyList();
    try {
      ProfileManager profileManager = this._pbmClient.getProfileManager();
      if (log.isDebugEnabled())
        log.debug("Created ProfileManager stub to retrieve CapabilityObjectMetadataPerCategory"); 
      BlockingFuture blockingFuture = new BlockingFuture();
      profileManager.fetchCapabilityMetadata(null, null, (Future)blockingFuture);
      CapabilityObjectMetadataPerCategory[] capabilityObjectMetadataPerCategoryArray = (CapabilityObjectMetadataPerCategory[])blockingFuture.get();
      if (capabilityObjectMetadataPerCategoryArray != null) {
        if (log.isDebugEnabled())
          log.debug("Retrieved CapabilityObjectMetadataPerCategory."); 
        capabilityObjectMetadataPerCategory = Arrays.asList(capabilityObjectMetadataPerCategoryArray);
      } 
    } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
      log.warn("Error occured while retrieving CapabilityObjectMetadataPerCategory", e);
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return capabilityObjectMetadataPerCategory;
  }
  
  public String getKey(CapabilityObjectMetadataPerCategory t) {
    return t.getSubCategory();
  }
}

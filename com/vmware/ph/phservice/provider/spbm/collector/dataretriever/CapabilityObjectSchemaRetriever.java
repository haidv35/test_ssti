package com.vmware.ph.phservice.provider.spbm.collector.dataretriever;

import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import com.vmware.vim.binding.pbm.capability.provider.CapabilityObjectSchema;
import com.vmware.vim.binding.pbm.profile.ProfileManager;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.core.impl.BlockingFuture;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CapabilityObjectSchemaRetriever implements DataRetriever<CapabilityObjectSchema> {
  private static final Log log = LogFactory.getLog(CapabilityObjectSchemaRetriever.class);
  
  private final PbmServiceClient _pbmClient;
  
  public CapabilityObjectSchemaRetriever(SpbmCollectorContext collectorContext) {
    this._pbmClient = collectorContext.getPbmServiceClient();
  }
  
  public List<CapabilityObjectSchema> retrieveData() {
    List<CapabilityObjectSchema> capabilityObjectSchema = Collections.emptyList();
    try {
      if (log.isDebugEnabled())
        log.debug("Creating ProfileManager stub to retrieve CapabilityObjectSchema"); 
      ProfileManager profileManager = this._pbmClient.getProfileManager();
      BlockingFuture blockingFuture = new BlockingFuture();
      profileManager.fetchCapabilitySchema(null, null, (Future)blockingFuture);
      CapabilityObjectSchema[] capabilityObjectSchemaArray = (CapabilityObjectSchema[])blockingFuture.get();
      if (capabilityObjectSchemaArray != null) {
        capabilityObjectSchema = Arrays.asList(capabilityObjectSchemaArray);
      } else if (log.isDebugEnabled()) {
        log.debug("No capability schema in the environment.");
      } 
    } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
      log.warn("Error occured while retrieving CapabilitySchemaObject using ProfileManager.fetchCapabilitySchema() API", e);
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return capabilityObjectSchema;
  }
  
  public String getKey(CapabilityObjectSchema t) {
    return t.getNamespaceInfo().getNamespace();
  }
}

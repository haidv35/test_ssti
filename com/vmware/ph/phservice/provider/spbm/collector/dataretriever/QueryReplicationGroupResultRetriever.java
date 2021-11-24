package com.vmware.ph.phservice.provider.spbm.collector.dataretriever;

import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import com.vmware.vim.binding.pbm.ServerObjectRef;
import com.vmware.vim.binding.pbm.capability.CapabilityInstance;
import com.vmware.vim.binding.pbm.capability.provider.CapabilityObjectSchema;
import com.vmware.vim.binding.pbm.capability.provider.LineOfServiceInfo;
import com.vmware.vim.binding.pbm.profile.CapabilityBasedProfile;
import com.vmware.vim.binding.pbm.profile.Profile;
import com.vmware.vim.binding.pbm.profile.ProfileId;
import com.vmware.vim.binding.pbm.profile.ProfileManager;
import com.vmware.vim.binding.pbm.profile.QueryProfileResult;
import com.vmware.vim.binding.pbm.profile.ResourceType;
import com.vmware.vim.binding.pbm.profile.ResourceTypeEnum;
import com.vmware.vim.binding.pbm.profile.SubProfileCapabilityConstraints;
import com.vmware.vim.binding.pbm.replication.QueryReplicationGroupResult;
import com.vmware.vim.binding.pbm.replication.ReplicationManager;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.core.impl.BlockingFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueryReplicationGroupResultRetriever implements DataRetriever<QueryReplicationGroupResult> {
  private static final Log log = LogFactory.getLog(QueryReplicationGroupResultRetriever.class);
  
  private static final String DATA_SERVICE_NS = "com.vmware.storageprofile.dataservice";
  
  private final PbmServiceClient _pbmClient;
  
  public QueryReplicationGroupResultRetriever(SpbmCollectorContext collectorContext) {
    this._pbmClient = collectorContext.getPbmServiceClient();
  }
  
  public List<QueryReplicationGroupResult> retrieveData() {
    List<QueryReplicationGroupResult> replicationGroupResultList = Collections.emptyList();
    try {
      if (log.isDebugEnabled())
        log.debug("Creating ProfileManager stub."); 
      ProfileManager profileManager = this._pbmClient.getProfileManager();
      ProfileId[] replicationProfileIds = getRequirementProfilesWithReplicationCapabilities(profileManager);
      if (replicationProfileIds.length != 0) {
        BlockingFuture blockingFuture = new BlockingFuture();
        profileManager.queryAssociatedEntities(replicationProfileIds, (Future)blockingFuture);
        QueryProfileResult[] queryProfileResult = (QueryProfileResult[])blockingFuture.get();
        if (queryProfileResult != null && queryProfileResult.length > 0) {
          ServerObjectRef[] entities = new ServerObjectRef[queryProfileResult.length];
          for (int i = 0; i < queryProfileResult.length; i++)
            entities[i] = queryProfileResult[i].getObject(); 
          if (log.isDebugEnabled())
            log.debug("Creating ReplicationManager stub to retrieve QueryReplicationGroupResult"); 
          ReplicationManager replicationManager = this._pbmClient.<ReplicationManager>createStub(this._pbmClient.getServiceInstanceContent().getReplicationManager());
          BlockingFuture blockingFuture1 = new BlockingFuture();
          replicationManager.queryReplicationGroups(entities, (Future)blockingFuture1);
          QueryReplicationGroupResult[] replicationGroupResult = (QueryReplicationGroupResult[])blockingFuture1.get();
          if (replicationGroupResult != null) {
            if (log.isDebugEnabled())
              log.debug("Retrieved queryReplicationGroupResult"); 
            replicationGroupResultList = new LinkedList<>();
            for (QueryReplicationGroupResult result : replicationGroupResult) {
              if (result.getReplicationGroupId() != null)
                replicationGroupResultList.add(result); 
            } 
          } else if (log.isDebugEnabled()) {
            log.debug("Failed to return queryReplicationGroupResult..");
          } 
        } else if (log.isDebugEnabled()) {
          log.debug("No VM/disk entities associated with profile.");
        } 
      } else if (log.isDebugEnabled()) {
        log.debug("No requirement profiles with replication capabilities.");
      } 
    } catch (InterruptedException|ExecutionException e) {
      log.warn("Error occurred while retrieving QueryReplicationGroupResult", e);
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return replicationGroupResultList;
  }
  
  public String getKey(QueryReplicationGroupResult t) {
    return t.getObject().getKey();
  }
  
  private static ProfileId[] getRequirementProfilesWithReplicationCapabilities(ProfileManager profileManager) throws InterruptedException, ExecutionException {
    List<String> replicationNamespaces = getReplicationNamespaces(profileManager);
    List<ProfileId> replicationProfileIds = Collections.emptyList();
    if (!replicationNamespaces.isEmpty()) {
      Profile[] profiles = getAllProfilesOfGivenCategory(profileManager, CapabilityBasedProfile.ProfileCategoryEnum.REQUIREMENT.toString().toString());
      if (profiles != null) {
        replicationProfileIds = new ArrayList<>(profiles.length);
        List<String> dspIdsWithReplication = getDspIdsWithReplication(profileManager, replicationNamespaces);
        for (Profile profile : profiles) {
          if (profile instanceof CapabilityBasedProfile) {
            CapabilityBasedProfile capBasedProfile = (CapabilityBasedProfile)profile;
            if (hasReplicationCapabilities(capBasedProfile, replicationNamespaces, dspIdsWithReplication))
              replicationProfileIds.add(profile.getProfileId()); 
          } 
        } 
      } 
    } else if (log.isDebugEnabled()) {
      log.debug("No replication namespaces advertised.");
    } 
    return replicationProfileIds.<ProfileId>toArray(new ProfileId[replicationProfileIds.size()]);
  }
  
  private static List<String> getReplicationNamespaces(ProfileManager profileManager) throws InterruptedException, ExecutionException {
    String[] los = { LineOfServiceInfo.LineOfServiceEnum.REPLICATION.toString() };
    BlockingFuture blockingFuture = new BlockingFuture();
    profileManager.fetchCapabilitySchema(null, los, (Future)blockingFuture);
    CapabilityObjectSchema[] capabilityObjectSchemas = (CapabilityObjectSchema[])blockingFuture.get();
    if (capabilityObjectSchemas != null) {
      List<String> replicationNamespaces = new ArrayList<>(capabilityObjectSchemas.length);
      for (CapabilityObjectSchema capabilityObjectSchema : capabilityObjectSchemas) {
        String namespace = capabilityObjectSchema.getNamespaceInfo().getNamespace();
        replicationNamespaces.add(namespace);
        if (log.isDebugEnabled())
          log.debug("REPLICATION NAMESPACE : " + namespace); 
      } 
      return replicationNamespaces;
    } 
    return Collections.emptyList();
  }
  
  private static Profile[] getAllProfilesOfGivenCategory(ProfileManager profileManager, String profileCategory) throws InterruptedException, ExecutionException {
    BlockingFuture blockingFuture = new BlockingFuture();
    ResourceType resourceType = new ResourceType();
    resourceType.setResourceType(ResourceTypeEnum.STORAGE.toString());
    profileManager.queryProfile(resourceType, profileCategory, (Future)blockingFuture);
    ProfileId[] profileIds = (ProfileId[])blockingFuture.get();
    Profile[] profiles = null;
    if (profileIds != null) {
      BlockingFuture blockingFuture1 = new BlockingFuture();
      profileManager.retrieveContent(profileIds, (Future)blockingFuture1);
      profiles = (Profile[])blockingFuture1.get();
    } 
    return profiles;
  }
  
  private static List<String> getDspIdsWithReplication(ProfileManager profileManager, List<String> replicationNamespaces) throws InterruptedException, ExecutionException {
    Profile[] allDsps = getAllProfilesOfGivenCategory(profileManager, CapabilityBasedProfile.ProfileCategoryEnum.DATA_SERVICE_POLICY.toString());
    List<String> replicationDspIds = Collections.emptyList();
    if (allDsps != null) {
      replicationDspIds = new ArrayList<>(allDsps.length);
      for (Profile profile : allDsps) {
        if (profile instanceof CapabilityBasedProfile) {
          CapabilityBasedProfile capBasedProfile = (CapabilityBasedProfile)profile;
          if (hasReplicationCapabilities(capBasedProfile, replicationNamespaces, null))
            replicationDspIds.add(profile.getProfileId().getUniqueId()); 
        } 
      } 
    } 
    return replicationDspIds;
  }
  
  private static boolean hasReplicationCapabilities(CapabilityBasedProfile capBasedProfile, List<String> replicationNamespaces, List<String> dspsWithReplication) throws InterruptedException, ExecutionException {
    boolean result = false;
    if (capBasedProfile.getConstraints() instanceof SubProfileCapabilityConstraints) {
      SubProfileCapabilityConstraints capabilityConstraints = (SubProfileCapabilityConstraints)capBasedProfile.getConstraints();
      for (SubProfileCapabilityConstraints.SubProfile subProfile : capabilityConstraints.getSubProfiles()) {
        CapabilityInstance[] capInstances = subProfile.getCapability();
        for (CapabilityInstance capInstance : capInstances) {
          boolean isReplicationCapability = (replicationNamespaces != null && replicationNamespaces.contains(capInstance.getId().getNamespace()));
          boolean isDataServiceNamespace = capInstance.getId().getNamespace().equals("com.vmware.storageprofile.dataservice");
          boolean isDataServiceReplicationCapability = (isDataServiceNamespace && dspsWithReplication != null && dspsWithReplication.contains(capInstance.getId().getId()));
          if (isReplicationCapability || isDataServiceReplicationCapability)
            result = true; 
        } 
      } 
    } 
    return result;
  }
}

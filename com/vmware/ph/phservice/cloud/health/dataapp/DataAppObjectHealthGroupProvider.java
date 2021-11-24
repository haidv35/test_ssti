package com.vmware.ph.phservice.cloud.health.dataapp;

import com.vmware.ph.phservice.cloud.dataapp.AgentStatus;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgent;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager;
import com.vmware.ph.phservice.cloud.dataapp.internal.ProgressReporter;
import com.vmware.ph.phservice.cloud.health.ObjectHealthGroupProvider;
import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import com.vmware.ph.phservice.provider.common.QueryContext;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataAppObjectHealthGroupProvider implements ObjectHealthGroupProvider {
  public static final String CONTEXT_OBJECT_KEY = "objects";
  
  private static final Log _log = LogFactory.getLog(DataAppObjectHealthGroupProvider.class);
  
  private final DataAppAgentManager _dataAppAgentManager;
  
  private final DataAppAgentIdProvider _dataAppAgentIdProvider;
  
  private final DataAppMoRefToObjectIdConverter _moRefToObjectIdConverter;
  
  private final DataAppResultToVmodlObjectConverter _resultConverter;
  
  public DataAppObjectHealthGroupProvider(DataAppAgentManager dataAppAgentManager, DataAppAgentIdProvider dataAppAgentIdProvider, DataAppMoRefToObjectIdConverter moRefToObjectIdConverter, DataAppResultToVmodlObjectConverter resultConverter) {
    this._dataAppAgentManager = dataAppAgentManager;
    this._dataAppAgentIdProvider = dataAppAgentIdProvider;
    this._moRefToObjectIdConverter = moRefToObjectIdConverter;
    this._resultConverter = resultConverter;
  }
  
  public Pair<VsanClusterHealthGroup, Date> getObjectHealthGroupAndTimestamp(ManagedObjectReference moRef, boolean useCache, String perspective, ProgressReporter progressReporter) {
    PluginResult pluginResult = null;
    String objectId = this._moRefToObjectIdConverter.getObjectId(moRef);
    QueryContext queryContext = createQueryContextFromMoRef(moRef);
    DataAppAgent dataAppAgent = getDataAppAgent();
    if (dataAppAgent != null)
      if (progressReporter != null) {
        pluginResult = dataAppAgent.execute(objectId, queryContext, progressReporter);
      } else {
        pluginResult = dataAppAgent.execute(objectId, queryContext, useCache);
      }  
    VsanClusterHealthGroup healthGroup = this._resultConverter.<VsanClusterHealthGroup>convertToVmodlObject(pluginResult);
    Pair<VsanClusterHealthGroup, Date> result = null;
    if (healthGroup != null) {
      Date timestamp = null;
      if (pluginResult != null)
        timestamp = pluginResult.getTimestamp(); 
      result = new Pair(healthGroup, timestamp);
    } 
    return result;
  }
  
  public List<VsanClusterHealthGroup> getCachedHealthGroups(@Nonnull String objectIdSubstring) {
    DataAppAgent dataAppAgent = getDataAppAgent();
    if (dataAppAgent != null) {
      AgentStatus agentStatus = dataAppAgent.getAgentStatus();
      if (agentStatus != null) {
        List<PluginResult> cachedResults = agentStatus.getCachedResults();
        return convertCachedPluginResultsToHealthGroups(cachedResults, objectIdSubstring);
      } 
    } 
    return Collections.emptyList();
  }
  
  private List<VsanClusterHealthGroup> convertCachedPluginResultsToHealthGroups(List<PluginResult> cachedResults, String objectIdSubstring) {
    List<VsanClusterHealthGroup> cachedHealthGroups = new ArrayList<>();
    if (cachedResults != null)
      for (PluginResult pluginResult : cachedResults) {
        if (pluginResult != null) {
          String objectId = pluginResult.getObjectId();
          if (objectId != null)
            if (objectId.toLowerCase().contains(objectIdSubstring
                .toLowerCase())) {
              VsanClusterHealthGroup healthGroup = this._resultConverter.<VsanClusterHealthGroup>convertToVmodlObject(pluginResult);
              if (healthGroup != null)
                cachedHealthGroups.add(healthGroup); 
            }  
        } 
      }  
    return cachedHealthGroups;
  }
  
  private DataAppAgent getDataAppAgent() {
    DataAppAgentId dataAppAgentId = this._dataAppAgentIdProvider.getDataAppAgentId();
    if (dataAppAgentId == null) {
      _log.warn("DataApp agent id is null");
      return null;
    } 
    DataAppAgent dataAppAgent = null;
    try {
      dataAppAgent = this._dataAppAgentManager.getAgent(dataAppAgentId);
    } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.NotFound e) {
      _log.warn("DataApp agent for: " + dataAppAgentId.getCollectorId() + " is missing.");
    } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.NotRunning e) {
      _log.warn("DataApp agent for: " + dataAppAgentId.getCollectorId() + " is not started.");
    } 
    return dataAppAgent;
  }
  
  private static QueryContext createQueryContextFromMoRef(ManagedObjectReference moRef) {
    if (moRef == null)
      return null; 
    Map<String, List<Object>> objectKeyToObjects = new HashMap<>();
    objectKeyToObjects.put("objects", 
        
        Collections.singletonList(moRef));
    objectKeyToObjects.put(moRef
        .getType(), 
        Collections.singletonList(moRef));
    QueryContext queryContext = new QueryContext(objectKeyToObjects);
    return queryContext;
  }
}

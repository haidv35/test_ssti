package com.vmware.ph.phservice.cloud.dataapp.internal;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.service.DataApp;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import com.vmware.ph.phservice.common.cdf.internal.dataapp.PluginResultUtil;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AgentResultRetriever {
  private static final Log _log = LogFactory.getLog(AgentResultRetriever.class);
  
  private static final int DEFAULT_RETRY_INTERVAL_MILLIS = 5000;
  
  private static final long DEFAULT_WAIT_TIMEOUT_MILLIS = 30000L;
  
  private final long _retryIntervalMillis;
  
  private final long _waitTimeoutMillis;
  
  public AgentResultRetriever() {
    this._retryIntervalMillis = 5000L;
    this._waitTimeoutMillis = 30000L;
  }
  
  public AgentResultRetriever(long retryIntervalMillis, long waitTimeoutMillis) {
    this._retryIntervalMillis = retryIntervalMillis;
    this._waitTimeoutMillis = waitTimeoutMillis;
  }
  
  public List<PluginResult> getPluginResults(DataApp dataApp, DataAppAgentId agentId, String objectId, String dataType, Long sinceTimestamp) {
    String collectorId = agentId.getCollectorId();
    String collectorInstanceId = agentId.getCollectorInstanceId();
    String deploymentSecret = agentId.getDeploymentSecret();
    boolean newResultsAvailable = false;
    List<PluginResult> pluginResults = null;
    long startTimestamp = System.currentTimeMillis();
    while (!newResultsAvailable && !hasTimedOut(startTimestamp, this._waitTimeoutMillis)) {
      try {
        String result = dataApp.getResult(collectorId, collectorInstanceId, deploymentSecret, dataType, objectId, sinceTimestamp);
        pluginResults = PluginResultUtil.parsePluginResults(collectorId, collectorInstanceId, result);
        if (pluginResults != null) {
          newResultsAvailable = true;
          if (_log.isDebugEnabled())
            _log.debug("Successfully retrieved fresh result for DataApp " + collectorId + " and object ID '" + objectId + "'."); 
          continue;
        } 
        sleep();
      } catch (Exception e) {
        ExceptionsContextManager.store(e);
        if (_log.isWarnEnabled())
          _log.warn("Exception occurred while polling VAC result service for agent " + collectorId + " and object ID '" + objectId + "'. Retrying...", e); 
        sleep();
      } 
    } 
    if (pluginResults == null && 
      _log.isWarnEnabled())
      _log.warn("Could not retrieve any result for agent " + collectorId + " and object ID '" + objectId + "' for a total of " + TimeUnit.MILLISECONDS
          
          .toSeconds(this._waitTimeoutMillis) + " seconds."); 
    return pluginResults;
  }
  
  private static boolean hasTimedOut(long startTime, long waitTimeoutMillis) {
    return (System.currentTimeMillis() - startTime > waitTimeoutMillis);
  }
  
  private void sleep() {
    try {
      Thread.sleep(this._retryIntervalMillis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } 
  }
}

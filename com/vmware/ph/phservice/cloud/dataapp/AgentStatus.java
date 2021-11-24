package com.vmware.ph.phservice.cloud.dataapp;

import com.vmware.ph.phservice.common.cdf.dataapp.ManifestInfo;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AgentStatus {
  private boolean _isAgentRunning;
  
  private long _lastUploadTime;
  
  private ManifestSpec _manifestSpec;
  
  private Set<String> _objects;
  
  private Map<String, ManifestInfo> _objectManifests;
  
  private List<PluginResult> _cachedResults;
  
  private ExceptionsContext _exceptions;
  
  private long _lastCollectedTime;
  
  public AgentStatus() {
    this(false, 0L, 0L, new HashSet<>(), new HashMap<>(), new ArrayList<>(), new ExceptionsContext());
  }
  
  public AgentStatus(boolean isAgentRunning, long lastUploadTime, long lastCollectedTime, Set<String> objects, Map<String, ManifestInfo> objectManifests, List<PluginResult> cachedResults, ExceptionsContext exceptions) {
    this._isAgentRunning = isAgentRunning;
    this._lastUploadTime = lastUploadTime;
    this._lastCollectedTime = lastCollectedTime;
    this._cachedResults = cachedResults;
    this._objects = objects;
    this._objectManifests = objectManifests;
    this._exceptions = exceptions;
  }
  
  public boolean isAgentRunning() {
    return this._isAgentRunning;
  }
  
  public void setAgentRunning(boolean isAgentRunning) {
    this._isAgentRunning = isAgentRunning;
  }
  
  public long getLastUploadTime() {
    return this._lastUploadTime;
  }
  
  public void setLastUploadTime(long lastUploadTime) {
    this._lastUploadTime = lastUploadTime;
  }
  
  public List<PluginResult> getCachedResults() {
    return this._cachedResults;
  }
  
  public void setCachedResults(List<PluginResult> cachedResults) {
    this._cachedResults = cachedResults;
  }
  
  public ManifestSpec getManifestSpec() {
    return this._manifestSpec;
  }
  
  public void setManifestSpec(ManifestSpec manifestSpec) {
    this._manifestSpec = manifestSpec;
  }
  
  public Map<String, ManifestInfo> getObjectManifests() {
    return this._objectManifests;
  }
  
  public void setObjectManifests(Map<String, ManifestInfo> objectManifests) {
    this._objectManifests = objectManifests;
  }
  
  public ExceptionsContext getExceptions() {
    return this._exceptions;
  }
  
  public void setExceptions(ExceptionsContext exceptions) {
    this._exceptions = exceptions;
  }
  
  public Set<String> getObjects() {
    return this._objects;
  }
  
  public void setObjects(Set<String> objects) {
    this._objects = objects;
  }
  
  public long getLastCollectedTime() {
    return this._lastCollectedTime;
  }
  
  public void setLastCollectedTime(long lastCollectedTime) {
    this._lastCollectedTime = lastCollectedTime;
  }
}

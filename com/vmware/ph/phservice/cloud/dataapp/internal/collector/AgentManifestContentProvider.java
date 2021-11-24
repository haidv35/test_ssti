package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.service.DataApp;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestInfo;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;

public class AgentManifestContentProvider implements ManifestContentProvider {
  private final DataApp _dataApp;
  
  private final ManifestSpec _agentManifestSpec;
  
  private final String _objectId;
  
  public AgentManifestContentProvider(DataApp dataApp, ManifestSpec agentManifestSpec, String objectId) {
    this._dataApp = dataApp;
    this._agentManifestSpec = agentManifestSpec;
    this._objectId = objectId;
  }
  
  public boolean isEnabled() {
    return true;
  }
  
  public String getManifestContent(String collectorId, String collectorInstanceId) throws ManifestContentProvider.ManifestException {
    ManifestInfo manifestInfo = this._dataApp.getManifestInfo(collectorId, collectorInstanceId, this._agentManifestSpec, this._objectId);
    return manifestInfo.getContent();
  }
}

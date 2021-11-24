package com.vmware.ph.phservice.cloud.dataapp.internal.upload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryAgentPayloadUploadStrategy implements AgentPayloadUploadStrategy {
  private final List<String> _jsons = new ArrayList<>();
  
  public void upload(AgentJsonUploadRequest jsonUploadRequest) throws AgentPayloadUploadException {
    this._jsons.add(jsonUploadRequest.getJson());
  }
  
  public List<String> getUploadedJsons() {
    return Collections.unmodifiableList(this._jsons);
  }
}

package com.vmware.ph.phservice.cloud.dataapp.internal.upload;

public interface AgentPayloadUploadStrategy {
  void upload(AgentJsonUploadRequest paramAgentJsonUploadRequest) throws AgentPayloadUploadException;
}

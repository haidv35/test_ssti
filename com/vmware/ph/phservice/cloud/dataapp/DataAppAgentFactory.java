package com.vmware.ph.phservice.cloud.dataapp;

import com.vmware.ph.phservice.cloud.dataapp.service.DataApp;

public interface DataAppAgentFactory {
  DataAppAgent createAgent(DataApp paramDataApp, DataAppAgentIdProvider paramDataAppAgentIdProvider, DataAppAgentCreateInfo paramDataAppAgentCreateInfo);
}

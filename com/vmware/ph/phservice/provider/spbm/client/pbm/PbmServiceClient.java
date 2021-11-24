package com.vmware.ph.phservice.provider.spbm.client.pbm;

import com.vmware.ph.phservice.provider.spbm.client.XServiceClient;
import com.vmware.vim.binding.pbm.ServiceInstanceContent;
import com.vmware.vim.binding.pbm.profile.ProfileManager;
import java.util.concurrent.ExecutionException;

public interface PbmServiceClient extends XServiceClient {
  ServiceInstanceContent getServiceInstanceContent() throws InterruptedException, ExecutionException;
  
  ProfileManager getProfileManager() throws InterruptedException, ExecutionException;
}

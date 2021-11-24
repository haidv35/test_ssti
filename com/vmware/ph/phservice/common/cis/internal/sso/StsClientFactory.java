package com.vmware.ph.phservice.common.cis.internal.sso;

public interface StsClientFactory {
  StsClient createStsClient() throws Exception;
}

package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import java.net.URI;
import java.security.KeyStore;

public interface HttpConfigurationFactory {
  HttpConfiguration createConfiguration(URI paramURI, KeyStore paramKeyStore);
}

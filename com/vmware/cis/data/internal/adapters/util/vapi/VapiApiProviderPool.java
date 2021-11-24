package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.vapi.core.ApiProvider;

public interface VapiApiProviderPool extends AutoCloseable {
  ApiProvider getApiProvider(String paramString);
}

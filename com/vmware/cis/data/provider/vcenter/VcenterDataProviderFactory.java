package com.vmware.cis.data.provider.vcenter;

import com.vmware.cis.data.provider.DataProvider;

public interface VcenterDataProviderFactory {
  DataProvider getDataProviderForVcenter(String paramString);
}

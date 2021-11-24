package com.vmware.ph.phservice.provider.fcd.collector;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.ph.phservice.provider.common.vim.VimDataProvidersConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FcdDataProvidersConnectionImpl extends VimDataProvidersConnection {
  public FcdDataProvidersConnectionImpl(VimContext vimContext) {
    super(vimContext);
  }
  
  public FcdDataProvidersConnectionImpl(VimContext vimContext, VcClientProvider vcClientProvider) {
    super(vimContext, vcClientProvider);
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    List<DataProvider> dataProviders = new ArrayList<>();
    dataProviders.add(new FcdDataProviderImpl(getVcClient()));
    return Collections.unmodifiableList(dataProviders);
  }
  
  public void close() {
    super.close();
  }
}

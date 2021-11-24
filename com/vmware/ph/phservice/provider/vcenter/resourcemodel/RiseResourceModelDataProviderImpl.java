package com.vmware.ph.phservice.provider.vcenter.resourcemodel;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.adapters.vmomi.impl.VmomiDataProviderConnection;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.vim.VimVmodlUtil;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.vim.binding.vim.ServiceInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RiseResourceModelDataProviderImpl implements DataProvider {
  private static final Log _logger = LogFactory.getLog(RiseResourceModelDataProviderImpl.class);
  
  private final VcClient _vcClient;
  
  private final VmomiDataProviderConnection _resourceModelVmomiDataProviderConnection;
  
  private final Object _authenticationLock = new Object();
  
  private volatile boolean _isVcClientAuthenticated = false;
  
  public RiseResourceModelDataProviderImpl(VcClient vcClient) {
    this._vcClient = vcClient;
    this
      ._resourceModelVmomiDataProviderConnection = new VmomiDataProviderConnection(vcClient.getVlsiClient());
  }
  
  public ResultSet executeQuery(Query query) {
    authenticateVcClientIfNecessary();
    query = QueryContextUtil.removeContextFromQueryFilter(query);
    return this._resourceModelVmomiDataProviderConnection.executeQuery(query);
  }
  
  public QuerySchema getSchema() {
    authenticateVcClientIfNecessary();
    return this._resourceModelVmomiDataProviderConnection.getSchema();
  }
  
  private void authenticateVcClientIfNecessary() {
    if (!this._isVcClientAuthenticated)
      synchronized (this._authenticationLock) {
        if (!this._isVcClientAuthenticated) {
          if (_logger.isDebugEnabled())
            _logger.debug("Attempting to login to VC"); 
          ServiceInstance serviceInstance = this._vcClient.<ServiceInstance>createMo(VimVmodlUtil.SERVICE_INSTANCE_MOREF);
          serviceInstance.currentTime();
          if (_logger.isInfoEnabled())
            _logger.info("Successfully logged into VC"); 
          this._isVcClientAuthenticated = true;
        } 
      }  
  }
}

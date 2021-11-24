package com.vmware.ph.phservice.provider.vcenter.resourcemodel;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.adapters.vmomi.impl.VimDataProvider;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.vim.VimVmodlUtil;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.QueryContext;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlQueryContextUtil;
import com.vmware.vim.binding.vim.ServiceInstance;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VimRiseResourceModelDataProviderImpl implements DataProvider {
  private static final Log _logger = LogFactory.getLog(VimRiseResourceModelDataProviderImpl.class);
  
  private final VcClient _vcClient;
  
  private final DataProvider _vimResourceModelDataProvider;
  
  private final Object _authenticationLock = new Object();
  
  private volatile boolean _isVcClientAuthenticated = false;
  
  public VimRiseResourceModelDataProviderImpl(VcClient vcClient) {
    this(vcClient, new VimDataProvider(vcClient.getVlsiClient()));
  }
  
  VimRiseResourceModelDataProviderImpl(VcClient vcClient, DataProvider vimResourceModelDataProvider) {
    this._vcClient = vcClient;
    this._vimResourceModelDataProvider = vimResourceModelDataProvider;
  }
  
  public ResultSet executeQuery(Query query) {
    authenticateVcClientIfNecessary();
    query = expandContextInQuery(query);
    query = QueryContextUtil.removeContextFromQueryFilter(query);
    return this._vimResourceModelDataProvider.executeQuery(query);
  }
  
  public QuerySchema getSchema() {
    authenticateVcClientIfNecessary();
    return this._vimResourceModelDataProvider.getSchema();
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
  
  private static Query expandContextInQuery(Query query) {
    QueryContext queryContext = QueryContextUtil.getQueryContextFromQueryFilter(query);
    String resourceModelName = query.getResourceModels().iterator().next();
    List<ManagedObjectReference> moRefs = VmodlQueryContextUtil.getMoRefsFromContext(queryContext, resourceModelName);
    if (moRefs.isEmpty())
      return query; 
    Filter modelKeyFilter = QueryUtil.createFilterForPropertyAndValue("@modelKey", moRefs
        
        .get(0));
    Query queryWithModelKeyFilter = QueryUtil.createQueryWithAdditionalFilter(query, modelKeyFilter);
    return queryWithModelKeyFilter;
  }
}

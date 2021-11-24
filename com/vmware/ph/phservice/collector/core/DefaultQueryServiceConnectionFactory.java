package com.vmware.ph.phservice.collector.core;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultQueryServiceConnectionFactory implements QueryServiceConnectionFactory {
  private static final Log _log = LogFactory.getLog(DefaultQueryServiceConnectionFactory.class);
  
  public QueryServiceConnection createQueryServiceConnection(Builder<DataProvidersConnection> dataProvidersConnectionBuilder) {
    DataProvidersConnection dataProvidersConnection = getDataProvidersConnection(dataProvidersConnectionBuilder);
    if (dataProvidersConnection == null)
      return null; 
    return new DefaultQueryServiceConnection(dataProvidersConnection);
  }
  
  protected DataProvidersConnection getDataProvidersConnection(Builder<DataProvidersConnection> dataProvidersConnectionBuilder) {
    DataProvidersConnection dataProvidersConnection = null;
    if (dataProvidersConnectionBuilder != null)
      dataProvidersConnection = (DataProvidersConnection)dataProvidersConnectionBuilder.build(); 
    if (dataProvidersConnection == null)
      _log.debug("Cannot create the DataProvidersConnection. No QueryService connection will be created."); 
    return dataProvidersConnection;
  }
}

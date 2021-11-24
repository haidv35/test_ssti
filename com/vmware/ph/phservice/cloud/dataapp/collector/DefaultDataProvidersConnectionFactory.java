package com.vmware.ph.phservice.cloud.dataapp.collector;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;

public class DefaultDataProvidersConnectionFactory implements DataProvidersConnectionFactory {
  private final Builder<? extends DataProvidersConnection> _connectionBuilder;
  
  public DefaultDataProvidersConnectionFactory(Builder<? extends DataProvidersConnection> connectionBuilder) {
    this._connectionBuilder = connectionBuilder;
  }
  
  public DataProvidersConnection createDataProvidersConnection(Object contextData) {
    return this._connectionBuilder.build();
  }
}

package com.vmware.cis.data.internal.provider.profiler;

import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.cis.data.internal.provider.DataProviderConnection;
import com.vmware.cis.data.internal.provider.DataProviderConnector;
import com.vmware.cis.data.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OperationThresholdDataProviderConnector implements DataProviderConnector {
  private static final Logger _logger = LoggerFactory.getLogger(OperationThresholdDataProviderConnector.class);
  
  private final DataProviderConnector _providerConnector;
  
  private final OperationThresholdConfig _thresholdConfig;
  
  public OperationThresholdDataProviderConnector(DataProviderConnector providerConnector, OperationThresholdConfig thresholdConfig) {
    assert providerConnector != null;
    assert thresholdConfig != null;
    this._providerConnector = providerConnector;
    this._thresholdConfig = thresholdConfig;
  }
  
  public DataProviderConnection getConnection(AuthenticationTokenSource authn) {
    long startTime = System.currentTimeMillis();
    final DataProviderConnection profiledConnection = this._providerConnector.getConnection(authn);
    long executionTime = System.currentTimeMillis() - startTime;
    if (executionTime > this._thresholdConfig.getLoginThreshold())
      _logger.warn("Slow login detected for ({}): {} ms.", this._providerConnector
          .toString(), Long.valueOf(executionTime)); 
    return new DataProviderConnection() {
        public void close() throws Exception {
          long startTime = System.currentTimeMillis();
          profiledConnection.close();
          long executionTime = System.currentTimeMillis() - startTime;
          if (executionTime > OperationThresholdDataProviderConnector.this._thresholdConfig.getLogoutThreshold())
            OperationThresholdDataProviderConnector._logger.warn("Slow logout detected for ({}): {} ms.", OperationThresholdDataProviderConnector.this
                ._providerConnector.toString(), Long.valueOf(executionTime)); 
        }
        
        public DataProvider getDataProvider() {
          return new OperationThresholdDataProvider(profiledConnection
              .getDataProvider(), OperationThresholdDataProviderConnector.this
              ._thresholdConfig.getOperationThreshold());
        }
      };
  }
  
  public String toString() {
    return getClass().getSimpleName() + "(" + this._providerConnector.toString() + ")";
  }
}

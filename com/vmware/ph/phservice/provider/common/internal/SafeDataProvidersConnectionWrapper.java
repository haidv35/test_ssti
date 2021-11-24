package com.vmware.ph.phservice.provider.common.internal;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SafeDataProvidersConnectionWrapper implements DataProvidersConnection {
  private static final Log _log = LogFactory.getLog(SafeDataProvidersConnectionWrapper.class);
  
  private final DataProvidersConnection _dataProvidersConnection;
  
  public static List<DataProvidersConnection> wrapDataProvidersConnectionsSafe(List<DataProvidersConnection> dataProviderConnections) {
    List<DataProvidersConnection> wrappedDataProvidersConnections = new ArrayList<>(dataProviderConnections.size());
    for (DataProvidersConnection dataProvidersConnection : dataProviderConnections) {
      SafeDataProvidersConnectionWrapper wrappedDataProvidersConnection = new SafeDataProvidersConnectionWrapper(dataProvidersConnection);
      wrappedDataProvidersConnections.add(wrappedDataProvidersConnection);
    } 
    return wrappedDataProvidersConnections;
  }
  
  public SafeDataProvidersConnectionWrapper(DataProvidersConnection dataProvidersConnection) {
    this._dataProvidersConnection = dataProvidersConnection;
  }
  
  public List<DataProvider> getDataProviders() {
    try {
      List<DataProvider> dataProviders = this._dataProvidersConnection.getDataProviders();
      List<DataProvider> wrappedDataProviders = new ArrayList<>(dataProviders.size());
      for (DataProvider dataProvider : dataProviders)
        wrappedDataProviders.add(new SafeDataProviderWrapper(dataProvider)); 
      return wrappedDataProviders;
    } catch (Throwable e) {
      _log.error("Error while getting the data providers list from: " + this._dataProvidersConnection
          .getClass().getName(), e);
      return Collections.emptyList();
    } 
  }
  
  public void close() {
    try {
      this._dataProvidersConnection.close();
    } catch (Throwable e) {
      _log.error(
          String.format("Error while closing data provider connection [%s]. This may lead to memory leaks.", new Object[] { this._dataProvidersConnection.getClass().getName() }), e);
    } 
  }
}

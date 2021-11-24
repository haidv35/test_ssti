package com.vmware.ph.phservice.provider.common.internal;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CompositeDataProvidersConnection implements DataProvidersConnection {
  private static final Log _log = LogFactory.getLog(CompositeDataProvidersConnection.class);
  
  private final List<DataProvidersConnection> _dataProvidersConnections;
  
  private AutoCloseable[] _optionalResources;
  
  public CompositeDataProvidersConnection(List<DataProvidersConnection> dataProvidersConnections, AutoCloseable... optionalResources) {
    this._dataProvidersConnections = dataProvidersConnections;
    this._optionalResources = optionalResources;
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    List<DataProvider> dpList = new LinkedList<>();
    for (DataProvidersConnection dpc : this._dataProvidersConnections)
      dpList.addAll(dpc.getDataProviders()); 
    return dpList;
  }
  
  public void close() {
    List<AutoCloseable> toClose = new ArrayList<>();
    for (DataProvidersConnection dpc : this._dataProvidersConnections)
      toClose.add(dpc); 
    if (this._optionalResources != null)
      for (AutoCloseable optionalResource : this._optionalResources)
        toClose.add(optionalResource);  
    for (AutoCloseable closeable : toClose) {
      try {
        closeable.close();
      } catch (Exception e) {
        if (_log.isDebugEnabled())
          _log.debug(
              String.format("An exception occurred while trying to close resource of type %s", new Object[] { closeable.getClass().getCanonicalName() }), e); 
      } 
    } 
  }
}

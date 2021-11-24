package com.vmware.cis.data.internal.provider;

import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CompositeDataProviderConnection implements AutoCloseable {
  private static final Logger _logger = LoggerFactory.getLogger(CompositeDataProviderConnection.class);
  
  private final Collection<DataProviderConnection> _connections;
  
  private final Collection<DataProvider> _providers;
  
  public CompositeDataProviderConnection(Collection<DataProviderConnection> connections) {
    assert connections != null;
    this._connections = connections;
    List<DataProvider> providers = new ArrayList<>(connections.size());
    for (DataProviderConnection connection : connections)
      providers.add(connection.getDataProvider()); 
    this._providers = Collections.unmodifiableList(providers);
  }
  
  public Collection<DataProvider> getDataProviders() {
    return this._providers;
  }
  
  public void close() {
    boolean shouldThrow = false;
    for (DataProviderConnection connection : this._connections) {
      try {
        connection.close();
      } catch (Exception ex) {
        shouldThrow = true;
        _logger.error("Could not close connection to Data Provider {}", connection, ex);
      } 
    } 
    if (shouldThrow)
      throw new RuntimeException("There was an exception while closing one or more data provider connections. Check the log for more details."); 
  }
}

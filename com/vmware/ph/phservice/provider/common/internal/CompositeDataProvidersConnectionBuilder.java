package com.vmware.ph.phservice.provider.common.internal;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import java.util.List;

public class CompositeDataProvidersConnectionBuilder implements Builder<DataProvidersConnection> {
  private final List<Builder<DataProvidersConnection>> _builders;
  
  public CompositeDataProvidersConnectionBuilder(List<Builder<DataProvidersConnection>> builders) {
    this._builders = builders;
  }
  
  public DataProvidersConnection build() {
    for (Builder<DataProvidersConnection> builder : this._builders) {
      DataProvidersConnection connection = (DataProvidersConnection)builder.build();
      if (connection != null)
        return connection; 
    } 
    return null;
  }
}

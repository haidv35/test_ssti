package com.vmware.ph.phservice.provider.esx.telemetry;

import com.vmware.ph.phservice.common.ItemsStream;
import com.vmware.vim.binding.vim.host.TelemetryManager;
import com.vmware.vim.binding.vmodl.KeyAnyValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class EsxTelemetryInfoStream implements ItemsStream<KeyAnyValue> {
  private static final int DEFAULT_PAGE_SIZE = 10;
  
  private final TelemetryManager _telemetryManager;
  
  private final TelemetryManager.TelemetryFilterSpec _filterSpec;
  
  private final int _pageSize;
  
  private int _currentOffset = 0;
  
  public EsxTelemetryInfoStream(TelemetryManager telemetryManager, TelemetryManager.TelemetryFilterSpec filterSpec) {
    this(telemetryManager, filterSpec, 10);
  }
  
  EsxTelemetryInfoStream(TelemetryManager telemetryManager, TelemetryManager.TelemetryFilterSpec filterSpec, int pageSize) {
    this._telemetryManager = telemetryManager;
    this._filterSpec = filterSpec;
    this._pageSize = pageSize;
  }
  
  public List<KeyAnyValue> read(int numItems) throws IllegalArgumentException {
    List<KeyAnyValue> telemetryData = new ArrayList<>();
    TelemetryManager.TelemetryPaginationSpec paginationSpec = new TelemetryManager.TelemetryPaginationSpec();
    paginationSpec.setOffset(this._currentOffset);
    paginationSpec.setLimit(numItems);
    TelemetryManager.TelemetryInfo hostTelemetryInfo = this._telemetryManager.retrieveTelemetryData(paginationSpec, this._filterSpec);
    this._currentOffset += numItems;
    if (hostTelemetryInfo.getData() != null)
      telemetryData.addAll(Arrays.asList(hostTelemetryInfo.getData())); 
    return telemetryData;
  }
  
  public int getLimit() {
    return this._pageSize;
  }
  
  public void close() throws IOException {}
}

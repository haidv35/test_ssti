package com.vmware.ph.phservice.ceip.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetCeipStateResult {
  private boolean _ceipEnabled;
  
  public GetCeipStateResult() {}
  
  public GetCeipStateResult(boolean ceipEnabled) {
    this._ceipEnabled = ceipEnabled;
  }
  
  @JsonProperty("ceip_enabled")
  public boolean getCeipEnabled() {
    return this._ceipEnabled;
  }
  
  public void setCeipEnabled(boolean ceipEnabled) {
    this._ceipEnabled = ceipEnabled;
  }
}

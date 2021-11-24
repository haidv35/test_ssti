package com.vmware.ph.phservice.provider.appliance.healthstatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HmsHealthStatus {
  private String msgId;
  
  private List<String> params = Collections.emptyList();
  
  public HmsHealthStatus(@JsonProperty("msgId") String msgId, @JsonProperty("params") List<String> params) {
    this.msgId = msgId;
    if (params != null) {
      this.params = params;
    } else {
      this.params = Collections.emptyList();
    } 
  }
  
  public String getMsgId() {
    return this.msgId;
  }
  
  public List<String> getParams() {
    return new ArrayList<>(this.params);
  }
}

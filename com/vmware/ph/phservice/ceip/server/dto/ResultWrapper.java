package com.vmware.ph.phservice.ceip.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultWrapper<T> {
  @JsonProperty("value")
  private T _value;
  
  public ResultWrapper() {}
  
  public ResultWrapper(T value) {
    this._value = value;
  }
  
  public T getValue() {
    return this._value;
  }
  
  public void setValue(T value) {
    this._value = value;
  }
}

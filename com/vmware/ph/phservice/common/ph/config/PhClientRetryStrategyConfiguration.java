package com.vmware.ph.phservice.common.ph.config;

import java.util.Objects;

public class PhClientRetryStrategyConfiguration {
  private boolean _useSeparateRepeatableInvocationStrategy = true;
  
  private Long _minJitterDuration = Long.valueOf(500L);
  
  private Long _maxBackoffDuration = Long.valueOf(120000L);
  
  public boolean getUseSeparateRepeatableInvocationStrategy() {
    return this._useSeparateRepeatableInvocationStrategy;
  }
  
  public void setUseSeparateRepeatableInvocationStrategy(boolean useSeparateRepeatableInvocationStrategy) {
    this._useSeparateRepeatableInvocationStrategy = useSeparateRepeatableInvocationStrategy;
  }
  
  public Long getMinJitterDuration() {
    return this._minJitterDuration;
  }
  
  public void setMinJitterDuration(Long minJitterDuration) {
    this._minJitterDuration = minJitterDuration;
  }
  
  public Long getMaxBackoffDuration() {
    return this._maxBackoffDuration;
  }
  
  public void setMaxBackoffDuration(Long maxBackoffDuration) {
    this._maxBackoffDuration = maxBackoffDuration;
  }
  
  public String toString() {
    return "PhClientRetryStrategyConfiguration{_useSeparateRepeatableInvocationStrategy=" + this._useSeparateRepeatableInvocationStrategy + ", _minJitterDuration=" + this._minJitterDuration + ", _maxBackoffDuration=" + this._maxBackoffDuration + '}';
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    PhClientRetryStrategyConfiguration that = (PhClientRetryStrategyConfiguration)o;
    return (this._useSeparateRepeatableInvocationStrategy == that._useSeparateRepeatableInvocationStrategy && 
      Objects.equals(this._minJitterDuration, that._minJitterDuration) && 
      Objects.equals(this._maxBackoffDuration, that._maxBackoffDuration));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Boolean.valueOf(this._useSeparateRepeatableInvocationStrategy), this._minJitterDuration, this._maxBackoffDuration });
  }
}

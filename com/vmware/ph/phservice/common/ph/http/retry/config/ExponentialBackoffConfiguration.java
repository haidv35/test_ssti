package com.vmware.ph.phservice.common.ph.http.retry.config;

public class ExponentialBackoffConfiguration {
  private final long _initialIntervalMillis;
  
  private double _multiplier = 1.5D;
  
  private double _randomizationFactor = 0.5D;
  
  public ExponentialBackoffConfiguration(long initialIntervalMillis) {
    if (initialIntervalMillis < 10L)
      throw new IllegalArgumentException("The initial interval must be at least 10 milliseconds."); 
    this._initialIntervalMillis = initialIntervalMillis;
  }
  
  public long getInitialIntervalMillis() {
    return this._initialIntervalMillis;
  }
  
  public void setMultiplier(double multiplier) {
    if (this._multiplier < 1.0D)
      throw new IllegalArgumentException("The multiplier must be a value greater than or equal to 1.0."); 
    this._multiplier = multiplier;
  }
  
  public double getMultiplier() {
    return this._multiplier;
  }
  
  public void setRandomizationFactor(double randomizationFactor) {
    if (this._randomizationFactor < 0.0D)
      throw new IllegalArgumentException("The randomization factor must be a non-negative number."); 
    this._randomizationFactor = randomizationFactor;
  }
  
  public double getRandomizationFactor() {
    return this._randomizationFactor;
  }
}

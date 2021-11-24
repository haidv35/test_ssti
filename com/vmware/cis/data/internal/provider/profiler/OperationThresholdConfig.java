package com.vmware.cis.data.internal.provider.profiler;

public class OperationThresholdConfig {
  public static final long DEFAULT_DATA_PROVIDER_OPERATION_THRESHOLD = 2000L;
  
  public static final long DEFAULT_DATA_PROVIDER_LOGIN_THRESHOLD = 30000L;
  
  public static final long DEFAULT_DATA_PROVIDER_LOGOUT_THRESHOLD = 3000L;
  
  private final long _loginThreshold;
  
  private final long _logoutThreshold;
  
  private final long _operationThreshold;
  
  public static final class Builder {
    private Long _providerLoginThreshold = null;
    
    private Long _providerLogoutThreshold = null;
    
    private Long _providerOperationThreshold = null;
    
    public static Builder create() {
      return new Builder();
    }
    
    public Builder withProviderLoginThreshold(long providerLoginThreshold) {
      assert providerLoginThreshold >= 0L;
      this._providerLoginThreshold = Long.valueOf(providerLoginThreshold);
      return this;
    }
    
    public Builder withProviderLogoutThreshold(long providerLogoutThreshold) {
      assert providerLogoutThreshold >= 0L;
      this._providerLogoutThreshold = Long.valueOf(providerLogoutThreshold);
      return this;
    }
    
    public Builder withProviderOperationThreshold(long providerOperationThreshold) {
      assert providerOperationThreshold >= 0L;
      this._providerOperationThreshold = Long.valueOf(providerOperationThreshold);
      return this;
    }
    
    public OperationThresholdConfig build() {
      long loginThresholdToUse = (this._providerLoginThreshold != null) ? this._providerLoginThreshold.longValue() : 30000L;
      long logoutThresholdToUse = (this._providerLogoutThreshold != null) ? this._providerLogoutThreshold.longValue() : 3000L;
      long operationThresholdToUse = (this._providerOperationThreshold != null) ? this._providerOperationThreshold.longValue() : 2000L;
      return new OperationThresholdConfig(loginThresholdToUse, logoutThresholdToUse, operationThresholdToUse);
    }
  }
  
  private OperationThresholdConfig(long loginThreshold, long logoutThreshold, long operationThreshold) {
    this._loginThreshold = loginThreshold;
    this._logoutThreshold = logoutThreshold;
    this._operationThreshold = operationThreshold;
  }
  
  public long getLoginThreshold() {
    return this._loginThreshold;
  }
  
  public long getLogoutThreshold() {
    return this._logoutThreshold;
  }
  
  public long getOperationThreshold() {
    return this._operationThreshold;
  }
}

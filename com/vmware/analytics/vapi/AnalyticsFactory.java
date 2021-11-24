package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubConfigurationBase;
import com.vmware.vapi.bindings.StubFactory;

public class AnalyticsFactory {
  private StubFactory stubFactory;
  
  private StubConfiguration stubConfig;
  
  public static AnalyticsFactory getFactory(StubFactory stubFactory, StubConfiguration stubConfig) {
    AnalyticsFactory instance = new AnalyticsFactory();
    instance.stubFactory = stubFactory;
    instance.stubConfig = stubConfig;
    return instance;
  }
  
  public Ceip ceipService() {
    return (Ceip)this.stubFactory.createStub(Ceip.class, (StubConfigurationBase)this.stubConfig);
  }
  
  public DataApp dataAppService() {
    return (DataApp)this.stubFactory.createStub(DataApp.class, (StubConfigurationBase)this.stubConfig);
  }
  
  public DataAppAgent dataAppAgentService() {
    return (DataAppAgent)this.stubFactory.createStub(DataAppAgent.class, (StubConfigurationBase)this.stubConfig);
  }
  
  public Telemetry telemetryService() {
    return (Telemetry)this.stubFactory.createStub(Telemetry.class, (StubConfigurationBase)this.stubConfig);
  }
  
  public void updateStubConfiguration(StubFactory stubFactory, StubConfiguration stubConfig) {
    if (this.stubFactory == stubFactory && this.stubConfig == stubConfig)
      return; 
    this.stubFactory = stubFactory;
    this.stubConfig = stubConfig;
  }
}

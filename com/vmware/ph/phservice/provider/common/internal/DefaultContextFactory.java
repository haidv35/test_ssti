package com.vmware.ph.phservice.provider.common.internal;

public class DefaultContextFactory implements ContextFactory {
  public Context createContext(String collectorId, String collectorInstanceId, String collectionId) {
    Context context = new Context(collectorId, collectorInstanceId, collectionId);
    return context;
  }
}

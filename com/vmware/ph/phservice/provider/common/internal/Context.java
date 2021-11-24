package com.vmware.ph.phservice.provider.common.internal;

import java.util.HashMap;

public class Context extends HashMap<String, Object> {
  private static final long serialVersionUID = 1L;
  
  public static final String INSTANCE_ID_KEY = "global-instanceId";
  
  public static final String COLLECTION_ID_KEY = "global-collectionId";
  
  public Context(Context context) {
    super(context);
  }
  
  public Context(String collectorId, String collectorInstanceId, String collectionId) {
    setCollectionId(collectionId);
    setInstanceId(collectorInstanceId);
  }
  
  public String getInstanceId() {
    return (String)get("global-instanceId");
  }
  
  public String getCollectionId() {
    return (String)get("global-collectionId");
  }
  
  private void setInstanceId(String instanceId) {
    put("global-instanceId", instanceId);
  }
  
  private void setCollectionId(String id) {
    put("global-collectionId", id);
  }
}

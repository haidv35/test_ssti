package com.vmware.ph.phservice.cloud.dataapp;

import java.util.Set;

public interface DataAppAgentManager extends AutoCloseable {
  DataAppAgent createAgent(DataAppAgentId paramDataAppAgentId, DataAppAgentCreateSpec paramDataAppAgentCreateSpec) throws AlreadyExists;
  
  DataAppAgent getAgent(DataAppAgentId paramDataAppAgentId) throws NotFound, NotRunning;
  
  Set<DataAppAgent> getAgents();
  
  void destroyAgent(DataAppAgentId paramDataAppAgentId) throws NotFound;
  
  void init();
  
  void refresh();
  
  void close();
  
  public static class NotFound extends Exception {
    private static final long serialVersionUID = 1L;
  }
  
  public static class AlreadyExists extends Exception {
    private static final long serialVersionUID = 1L;
  }
  
  public static class NotRunning extends Exception {
    private static final long serialVersionUID = 1L;
  }
  
  public static class IncorrectFormat extends Exception {
    private static final long serialVersionUID = 1L;
  }
}

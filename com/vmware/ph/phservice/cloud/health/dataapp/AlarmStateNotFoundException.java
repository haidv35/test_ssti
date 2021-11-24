package com.vmware.ph.phservice.cloud.health.dataapp;

public class AlarmStateNotFoundException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public AlarmStateNotFoundException(String msg) {
    super(msg);
  }
}

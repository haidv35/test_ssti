package com.vmware.ph.phservice.common.exceptionsCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExceptionsContext extends HashMap<String, List<ExceptionData>> {
  private String _currentObjectId = null;
  
  public void store(Throwable throwable) {
    List<ExceptionData> exceptions = computeIfAbsent(this._currentObjectId, k -> new ArrayList());
    ExceptionData exceptionData = new ExceptionData(throwable);
    exceptions.add(exceptionData);
  }
  
  public void setObjectId(String objectId) {
    this._currentObjectId = objectId;
  }
}

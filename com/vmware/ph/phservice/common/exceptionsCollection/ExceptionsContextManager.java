package com.vmware.ph.phservice.common.exceptionsCollection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExceptionsContextManager {
  private static Map<Integer, ExceptionsContext> _contextIdToExceptionContexts = new ConcurrentHashMap<>();
  
  private static ThreadLocal<ExceptionsContext> _currentContext = new ThreadLocal<>();
  
  public static void createCurrentContext() {
    _currentContext.set(new ExceptionsContext());
  }
  
  public static void setCurrentContextObjectId(String objectId) {
    if (isCurrentContextCreated())
      getCurrentContext().setObjectId(objectId); 
  }
  
  public static void removeCurrentObjectId() {
    setCurrentContextObjectId(null);
  }
  
  public static <T extends Throwable> T store(T throwable) {
    if (isCurrentContextCreated())
      getCurrentContext().store((Throwable)throwable); 
    return throwable;
  }
  
  public static void flushCurrentContext(Object object) {
    if (isCurrentContextCreated()) {
      _contextIdToExceptionContexts.put(getContextKey(object), getCurrentContext());
      _currentContext.remove();
    } 
  }
  
  public static ExceptionsContext getContextForId(Object object) {
    return _contextIdToExceptionContexts.get(getContextKey(object));
  }
  
  public static ExceptionsContext getCurrentContext() {
    return _currentContext.get();
  }
  
  private static boolean isCurrentContextCreated() {
    return (getCurrentContext() != null);
  }
  
  private static Integer getContextKey(Object object) {
    if (object == null)
      throw new IllegalArgumentException("The context ID object cannot be null."); 
    return Integer.valueOf(object.hashCode());
  }
}

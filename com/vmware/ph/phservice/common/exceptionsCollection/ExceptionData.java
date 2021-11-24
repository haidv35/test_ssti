package com.vmware.ph.phservice.common.exceptionsCollection;

import com.vmware.ph.phservice.common.internal.DateUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

public class ExceptionData {
  private final String _message;
  
  private final String _stackTraceString;
  
  private final String _type;
  
  private final long _occurrenceTimeInMillis;
  
  public ExceptionData(Throwable throwable) {
    this(throwable, Long.valueOf(DateUtil.createUtcCalendar().getTimeInMillis()));
  }
  
  public ExceptionData(Throwable throwable, Long timeInMillis) {
    this._occurrenceTimeInMillis = timeInMillis.longValue();
    this._type = throwable.getClass().getName();
    this._message = throwable.getMessage();
    this._stackTraceString = getStackTraceString(throwable);
  }
  
  public String getMessage() {
    return this._message;
  }
  
  public String getStackTraceString() {
    return this._stackTraceString;
  }
  
  public String getType() {
    return this._type;
  }
  
  public long getTimeInMillis() {
    return this._occurrenceTimeInMillis;
  }
  
  private String getStackTraceString(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    throwable.printStackTrace(printWriter);
    return stringWriter.toString();
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    ExceptionData that = (ExceptionData)o;
    return (this._occurrenceTimeInMillis == that._occurrenceTimeInMillis && 
      Objects.equals(this._message, that._message) && 
      Objects.equals(this._stackTraceString, that._stackTraceString) && 
      Objects.equals(this._type, that._type));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this._message, this._stackTraceString, this._type, Long.valueOf(this._occurrenceTimeInMillis) });
  }
  
  public String toString() {
    return "ExceptionData{_message='" + this._message + '\'' + ", _stackTraceString='" + this._stackTraceString + '\'' + ", _type='" + this._type + '\'' + ", _occurrenceTimeInMillis=" + this._occurrenceTimeInMillis + '}';
  }
}

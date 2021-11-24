package com.vmware.ph.phservice.common.manifest;

public interface ManifestContentProvider {
  boolean isEnabled();
  
  String getManifestContent(String paramString1, String paramString2) throws ManifestException;
  
  public static class ManifestException extends Exception {
    private static final long serialVersionUID = 1L;
    
    private final ManifestContentProvider.ManifestExceptionType _exceptionType;
    
    public ManifestException(String message) {
      this(message, ManifestContentProvider.ManifestExceptionType.GENERAL_ERROR);
    }
    
    public ManifestException(String message, ManifestContentProvider.ManifestExceptionType exceptionType) {
      super(message);
      this._exceptionType = exceptionType;
    }
    
    public ManifestException(ManifestContentProvider.ManifestExceptionType exceptionType, Exception e) {
      super(e);
      this._exceptionType = exceptionType;
    }
    
    public ManifestException(String message, ManifestContentProvider.ManifestExceptionType exceptionType, Exception e) {
      super(message, e);
      this._exceptionType = exceptionType;
    }
    
    public ManifestContentProvider.ManifestExceptionType getManifestExceptionType() {
      return this._exceptionType;
    }
  }
  
  public enum ManifestExceptionType {
    GENERAL_ERROR, MANIFEST_NOT_FOUND_ERROR, INVALID_COLLECTOR_ERROR, SIGNATURE_ERROR;
  }
}

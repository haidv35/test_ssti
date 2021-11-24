package com.vmware.cis.data.internal.adapters.federation;

import org.apache.commons.lang.Validate;

public final class MajorMinorVersion implements Comparable<MajorMinorVersion> {
  private final int _major;
  
  private final int _minor;
  
  public static MajorMinorVersion parseVersion(String versionString) {
    Validate.notEmpty(versionString);
    String[] fields = versionString.split("\\.");
    if (fields.length < 2)
      throw new IllegalArgumentException(invalidVersionMsg(versionString)); 
    int major = parseVersionField(fields[0], versionString);
    int minor = parseVersionField(fields[1], versionString);
    return new MajorMinorVersion(major, minor);
  }
  
  public MajorMinorVersion(int major, int minor) {
    this._major = major;
    this._minor = minor;
  }
  
  public int getMajor() {
    return this._major;
  }
  
  public int getMinor() {
    return this._minor;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof MajorMinorVersion))
      return false; 
    MajorMinorVersion other = (MajorMinorVersion)obj;
    return (this._major == other._major && this._minor == other._minor);
  }
  
  public int hashCode() {
    int hash = 11;
    hash = 31 * hash + this._major;
    hash = 31 * hash + this._minor;
    return hash;
  }
  
  public String toString() {
    return String.format("%s.%s", new Object[] { Integer.valueOf(this._major), Integer.valueOf(this._minor) });
  }
  
  public int compareTo(MajorMinorVersion o) {
    if (o == null)
      throw new NullPointerException(); 
    if (this._major < o._major)
      return -1; 
    if (this._major > o._major)
      return 1; 
    if (this._minor < o._minor)
      return -1; 
    if (this._minor > o._minor)
      return 1; 
    return 0;
  }
  
  private static int parseVersionField(String field, String versionString) {
    try {
      return Integer.valueOf(field).intValue();
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(invalidVersionMsg(versionString), ex);
    } 
  }
  
  private static String invalidVersionMsg(String versionString) {
    return "Invalid version string: " + versionString;
  }
}

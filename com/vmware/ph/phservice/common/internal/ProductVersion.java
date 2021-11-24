package com.vmware.ph.phservice.common.internal;

public class ProductVersion implements Comparable<ProductVersion> {
  private int major;
  
  private int minor;
  
  private int revision;
  
  protected ProductVersion() {}
  
  public ProductVersion(int major, int minor, int revision) {
    this.major = major;
    this.minor = minor;
    this.revision = revision;
  }
  
  public ProductVersion(String version) {
    int[] parsed = parseStringVersion(version);
    this.major = parsed[0];
    this.minor = parsed[1];
    this.revision = parsed[2];
  }
  
  public String toString() {
    return this.major + "." + this.minor + "." + this.revision;
  }
  
  public int hashCode() {
    return this.major + 3 * this.minor + 23 * this.revision;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (o == null)
      return false; 
    if (o instanceof ProductVersion) {
      ProductVersion otherVersion = (ProductVersion)o;
      return (this.major == otherVersion.major && this.minor == otherVersion.minor && this.revision == otherVersion.revision);
    } 
    return false;
  }
  
  public int compareTo(ProductVersion o) {
    int result;
    if (equals(o)) {
      result = 0;
    } else if (this.major == o.major) {
      if (this.minor == o.minor) {
        result = (this.revision < o.revision) ? -1 : 1;
      } else {
        result = (this.minor < o.minor) ? -1 : 1;
      } 
    } else {
      result = (this.major < o.major) ? -1 : 1;
    } 
    return result;
  }
  
  public int getMajor() {
    return this.major;
  }
  
  public int getMinor() {
    return this.minor;
  }
  
  public int getRevision() {
    return this.revision;
  }
  
  private static int[] parseStringVersion(String version) {
    String[] splited = version.split("\\.");
    if (splited.length < 2 || splited.length > 4)
      throw new IllegalArgumentException("Unsupported version format " + version + ", we support only version in format 'major.minor[.revision][.build]' where all values are integer numbers"); 
    int[] result = new int[3];
    for (int i = 0; i < result.length; i++)
      result[i] = (splited.length > i) ? Integer.valueOf(splited[i]).intValue() : 0; 
    return result;
  }
}

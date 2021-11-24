package com.vmware.cis.data.internal.provider;

public final class SsoDomain {
  private final String _id;
  
  private final String _name;
  
  private final String _type;
  
  public SsoDomain(String id, String name, String type) {
    assert id != null;
    this._id = id;
    this._name = name;
    this._type = type;
  }
  
  public String getId() {
    return this._id;
  }
  
  public String getName() {
    return this._name;
  }
  
  public String getType() {
    return this._type;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof SsoDomain))
      return false; 
    SsoDomain other = (SsoDomain)obj;
    return this._id.equals(other._id);
  }
  
  public int hashCode() {
    return this._id.hashCode();
  }
  
  public String toString() {
    return String.format("SsoDomain[id = %s, name = %s, type = %s]", new Object[] { this._id, this._name, this._type });
  }
}

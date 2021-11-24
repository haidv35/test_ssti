package com.vmware.ph.client.api.commondataformat20;

import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

public class Payload {
  public static class Builder {
    private List<JsonLd> jsonLds = new ArrayList<>();
    
    private String collectionId = null;
    
    public Builder withCollectionId(String collectionId) {
      this.collectionId = collectionId;
      return this;
    }
    
    public Builder add(JsonLd jsonLd) {
      if (null != jsonLd)
        this.jsonLds.add(jsonLd); 
      return this;
    }
    
    public Builder add(Collection<JsonLd> jsonLds) {
      if (null != jsonLds)
        this.jsonLds.addAll(jsonLds); 
      return this;
    }
    
    public Builder add(Payload payload) {
      if (null != payload)
        this.jsonLds.addAll(payload.getJsons()); 
      return this;
    }
    
    public Payload build() {
      return new Payload(this.jsonLds, this.collectionId);
    }
    
    public String toString() {
      return Payload.class.getSimpleName() + "." + Builder.class
        .getSimpleName() + this.jsonLds;
    }
  }
  
  private List<JsonLd> _jsonLds = null;
  
  private String _collectionId = null;
  
  private Payload(Collection<JsonLd> jsonLds, String collectionId) {
    this._jsonLds = new ArrayList<>();
    if (null != jsonLds)
      this._jsonLds.addAll(jsonLds); 
    this._collectionId = collectionId;
  }
  
  public List<JsonLd> getJsons() {
    return this._jsonLds;
  }
  
  public String getCollectionId() {
    return this._collectionId;
  }
  
  public int hashCode() {
    int prime = 31;
    int result = super.hashCode();
    result = 31 * result + ((this._jsonLds == null) ? 0 : this._jsonLds.hashCode());
    result = 31 * result + ((this._collectionId == null) ? 0 : this._collectionId.hashCode());
    return result;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (null == obj)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    Payload other = (Payload)obj;
    return EqualsBuilder.reflectionEquals(this, other);
  }
  
  public String toString() {
    return getClass().getSimpleName() + "[Collection ID is '" + this._collectionId + "'; Contains (" + this._jsonLds
      .size() + " jsons): " + 
      StringUtils.join(this._jsonLds, ",") + "]";
  }
}

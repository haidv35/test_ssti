package com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity;

import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class VelocityJsonLd {
  public Builder object;
  
  public static abstract class Builder {
    public Map<String, Object> attributes = new TreeMap<>();
    
    public Builder addProperty(String name, Object value) {
      return withProperty(name, value);
    }
    
    public Builder withProperty(String name, Object value) {
      this.attributes.put(name, value);
      return this;
    }
  }
  
  public static class BuilderFromTypeAndId extends Builder {
    public final String type;
    
    public final String id;
    
    BuilderFromTypeAndId(String type, String id) {
      this.type = type;
      this.id = id;
    }
  }
  
  public static class BuilderFromMoRef extends Builder {
    public final ManagedObjectReference moRef;
    
    BuilderFromMoRef(ManagedObjectReference moRef) {
      this.moRef = moRef;
    }
  }
  
  public static class BuilderFromResultItem extends BuilderFromMoRef {
    BuilderFromResultItem(NamedPropertiesResourceItem ri) {
      super((ManagedObjectReference)ri.getResourceObject());
      List<String> propertyNames = ri.getActualPropertyNames();
      List<Object> propertyValues = ri.getActualPropertyValues();
      for (int i = 0; i < propertyNames.size(); i++)
        addProperty(propertyNames.get(i), propertyValues.get(i)); 
    }
  }
  
  public Builder newObject(NamedPropertiesResourceItem ri) {
    Builder obj = new BuilderFromResultItem(ri);
    this.object = obj;
    return obj;
  }
  
  public Builder newObject(ManagedObjectReference moRef) {
    Builder obj = new BuilderFromMoRef(moRef);
    this.object = obj;
    return obj;
  }
  
  public Builder newObject(String type, String id) {
    Builder obj = new BuilderFromTypeAndId(type, id);
    this.object = obj;
    return obj;
  }
}

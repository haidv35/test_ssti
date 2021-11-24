package com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class Mappings {
  public static class ListWrapper<T> {
    private final List<T> items;
    
    public ListWrapper() {
      this.items = new ArrayList<>();
    }
    
    public ListWrapper(List<T> items) {
      this.items = items;
    }
    
    @XmlAnyElement(lax = true)
    public List<T> getItems() {
      return this.items;
    }
    
    public String toString() {
      return "ListWrapper [items=" + this.items + "]";
    }
    
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }
    
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(obj, this);
    }
  }
  
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Wrapper<T> {
    private final T value;
    
    public Wrapper() {
      this.value = null;
    }
    
    public Wrapper(T value) {
      this.value = value;
    }
    
    public T getValue() {
      return this.value;
    }
    
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }
    
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(obj, this);
    }
    
    public String toString() {
      return "Wrapper [value=" + this.value + "]";
    }
  }
}

package com.vmware.cis.data.api.binding;

import com.vmware.cis.data.model.Property;

public abstract class IdentifiableQueryBinding {
  @Property("@modelKey")
  public Object provider;
}

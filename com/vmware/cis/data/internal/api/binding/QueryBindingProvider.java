package com.vmware.cis.data.internal.api.binding;

import java.util.Collection;

public interface QueryBindingProvider {
  Collection<?> fetch(Collection<?> paramCollection);
}

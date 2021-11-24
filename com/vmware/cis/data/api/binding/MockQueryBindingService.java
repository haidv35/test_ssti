package com.vmware.cis.data.api.binding;

import com.vmware.cis.data.internal.api.binding.QueryBindingProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.Validate;

public final class MockQueryBindingService {
  public static QueryBindingService forBindings(Object... bindings) {
    Validate.noNullElements(bindings);
    final List<Object> bindingList = Arrays.asList(bindings);
    return new QueryBindingService(new QueryBindingProvider() {
          public Collection<?> fetch(Collection<?> keys) {
            if (keys.size() > bindingList.size())
              return bindingList; 
            return bindingList.subList(0, keys.size());
          }
        });
  }
}

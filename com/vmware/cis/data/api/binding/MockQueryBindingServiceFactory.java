package com.vmware.cis.data.api.binding;

import com.vmware.cis.data.internal.api.binding.QueryBindingProvider;
import com.vmware.cis.data.query.util.QueryBindingServiceFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.Validate;

public final class MockQueryBindingServiceFactory implements QueryBindingServiceFactory {
  private int _invocationIndex = 0;
  
  private final List<Collection<?>> _expectedKeys = new ArrayList<>();
  
  private final List<Collection<?>> _expectedResultBindings = new ArrayList<>();
  
  public QueryBindingService getQueryBindingService() {
    return new QueryBindingService(new QueryBindingProvider() {
          public Collection<?> fetch(Collection<?> keys) {
            assert !keys.contains(null);
            if (MockQueryBindingServiceFactory.this._invocationIndex >= MockQueryBindingServiceFactory.this._expectedResultBindings.size())
              throw new AssertionError("The query binding service has been called more times than the recorded expectations."); 
            int currentInvocation = MockQueryBindingServiceFactory.this._invocationIndex++;
            Set<Object> keysSet = new LinkedHashSet(keys);
            Set<Object> expectedKeysSet = new LinkedHashSet(MockQueryBindingServiceFactory.this._expectedKeys.get(currentInvocation));
            if (!expectedKeysSet.equals(keysSet)) {
              String errorMessage = String.format("The keys are not the same. Expected: %s, but was: %s", new Object[] { expectedKeysSet
                    
                    .toString(), keysSet.toString() });
              throw new AssertionError(errorMessage);
            } 
            return MockQueryBindingServiceFactory.this._expectedResultBindings.get(currentInvocation);
          }
        });
  }
  
  public void expectKeysAndReturn(Collection<?> keys, Collection<?> bindings) {
    Validate.noNullElements(keys);
    Validate.noNullElements(bindings);
    Validate.isTrue((bindings.size() <= keys.size()));
    this._expectedKeys.add(keys);
    this._expectedResultBindings.add(bindings);
  }
  
  public void expectKeyAndReturn(Object key, Object binding) {
    Validate.notNull(key);
    this._expectedKeys.add(Arrays.asList(new Object[] { key }));
    this._expectedResultBindings.add(Arrays.asList(new Object[] { binding }));
  }
}

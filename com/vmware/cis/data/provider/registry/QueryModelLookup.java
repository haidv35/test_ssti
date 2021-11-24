package com.vmware.cis.data.provider.registry;

import java.util.Collection;

public interface QueryModelLookup {
  Collection<Class<?>> getRegisteredQueryModels();
}

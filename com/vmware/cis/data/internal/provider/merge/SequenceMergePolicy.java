package com.vmware.cis.data.internal.provider.merge;

import java.util.Collection;
import java.util.Iterator;

public interface SequenceMergePolicy<T> {
  Iterator<T> merge(Collection<Iterator<T>> paramCollection);
}

package com.vmware.cis.data.internal.provider.merge;

import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.collections4.iterators.IteratorChain;

public final class UnorderedSequenceMergePolicy<T> implements SequenceMergePolicy<T> {
  public Iterator<T> merge(Collection<Iterator<T>> sequences) {
    assert sequences != null;
    IteratorChain<T> chain = new IteratorChain();
    for (Iterator<T> iterator : sequences)
      chain.addIterator(iterator); 
    return (Iterator<T>)chain;
  }
}

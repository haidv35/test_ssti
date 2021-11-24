package com.vmware.cis.data.internal.provider.merge;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public final class OrderedSequenceMergePolicy<T> implements SequenceMergePolicy<T> {
  private final Comparator<T> _itemComparator;
  
  public OrderedSequenceMergePolicy(Comparator<T> itemComparator) {
    assert itemComparator != null;
    this._itemComparator = itemComparator;
  }
  
  public Iterator<T> merge(Collection<Iterator<T>> sequences) {
    assert sequences != null;
    final PriorityQueue<ItemElement<T>> queue = new PriorityQueue<>(sequences.size(), new ItemContextComparator<>(this._itemComparator));
    for (Iterator<T> itemIterator : sequences) {
      if (itemIterator.hasNext())
        queue.add(new ItemElement<>(itemIterator)); 
    } 
    return new Iterator<T>() {
        public boolean hasNext() {
          return !queue.isEmpty();
        }
        
        public T next() {
          OrderedSequenceMergePolicy.ItemElement<T> head = queue.remove();
          if (head.itemIterator.hasNext())
            queue.add(new OrderedSequenceMergePolicy.ItemElement<>(head.itemIterator)); 
          return head.item;
        }
        
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
  }
  
  private static class ItemElement<T> {
    final T item;
    
    final Iterator<T> itemIterator;
    
    public ItemElement(Iterator<T> itemIterator) {
      assert itemIterator != null;
      T nextItem = itemIterator.next();
      assert nextItem != null;
      this.item = nextItem;
      this.itemIterator = itemIterator;
    }
  }
  
  private static final class ItemContextComparator<T> implements Comparator<ItemElement<T>> {
    private final Comparator<T> _itemComparator;
    
    public ItemContextComparator(Comparator<T> itemComparator) {
      assert itemComparator != null;
      this._itemComparator = itemComparator;
    }
    
    public int compare(OrderedSequenceMergePolicy.ItemElement<T> o1, OrderedSequenceMergePolicy.ItemElement<T> o2) {
      assert o1 != null;
      assert o2 != null;
      return this._itemComparator.compare(o1.item, o2.item);
    }
  }
}

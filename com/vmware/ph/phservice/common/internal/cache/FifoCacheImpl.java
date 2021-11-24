package com.vmware.ph.phservice.common.internal.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Deprecated
public class FifoCacheImpl<K, V> implements Cache<K, V> {
  private final ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();
  
  private final Lock _readLock = this._rwLock.readLock();
  
  private final Lock _writeLock = this._rwLock.writeLock();
  
  private final FifoTable<K, V> _table;
  
  private final CacheInvalidateCallback<V> _invalidateCallback;
  
  public FifoCacheImpl(int capacity) {
    this(capacity, null);
  }
  
  public FifoCacheImpl(int capacity, CacheInvalidateCallback<V> invalidateCallback) {
    this._table = new FifoTable<>(capacity);
    this._invalidateCallback = invalidateCallback;
  }
  
  public void put(K key, V value) {
    this._writeLock.lock();
    try {
      this._table.put(key, value);
    } finally {
      this._writeLock.unlock();
    } 
  }
  
  public V get(K key) {
    this._readLock.lock();
    try {
      return this._table.get(key);
    } finally {
      this._readLock.unlock();
    } 
  }
  
  public Collection<K> getAllKeys() {
    this._readLock.lock();
    try {
      return this._table.keySet();
    } finally {
      this._readLock.unlock();
    } 
  }
  
  public Collection<V> getAllValues() {
    this._readLock.lock();
    try {
      return this._table.values();
    } finally {
      this._readLock.unlock();
    } 
  }
  
  public Map<K, V> export() {
    this._readLock.lock();
    try {
      return (Map)Collections.unmodifiableMap(new HashMap<>(this._table));
    } finally {
      this._readLock.unlock();
    } 
  }
  
  public boolean contains(K key) {
    this._readLock.lock();
    try {
      return this._table.containsKey(key);
    } finally {
      this._readLock.unlock();
    } 
  }
  
  public void invalidate(K key) {
    V value = null;
    this._writeLock.lock();
    try {
      value = this._table.remove(key);
    } finally {
      this._writeLock.unlock();
    } 
    if (this._invalidateCallback != null && value != null)
      this._invalidateCallback.cacheValueInvalidated(value); 
  }
  
  public void invalidate() {
    ArrayList<V> valuesCopy = null;
    this._writeLock.lock();
    try {
      if (this._invalidateCallback != null)
        valuesCopy = new ArrayList<>(this._table.values()); 
      this._table.clear();
    } finally {
      this._writeLock.unlock();
    } 
    if (this._invalidateCallback != null && valuesCopy != null)
      for (V value : valuesCopy)
        this._invalidateCallback.cacheValueInvalidated(value);  
  }
  
  private static class FifoTable<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 1L;
    
    private final int _capacity;
    
    public FifoTable(int capacity) {
      super(capacity + 1, 0.75F, false);
      this._capacity = capacity;
    }
    
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
      return (size() > this._capacity);
    }
  }
}

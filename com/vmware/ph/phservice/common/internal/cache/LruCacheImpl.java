package com.vmware.ph.phservice.common.internal.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Deprecated
public class LruCacheImpl<K, V> implements Cache<K, V> {
  private final ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();
  
  private final Lock _readLock = this._rwLock.readLock();
  
  private final Lock _writeLock = this._rwLock.writeLock();
  
  private final LruTable<K, V> _table;
  
  public LruCacheImpl(int capacity) {
    this._table = new LruTable<>(capacity);
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
    this._writeLock.lock();
    try {
      this._table.remove(key);
    } finally {
      this._writeLock.unlock();
    } 
  }
  
  public void invalidate() {
    this._writeLock.lock();
    try {
      this._table.clear();
    } finally {
      this._writeLock.unlock();
    } 
  }
  
  static class LruTable<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 1L;
    
    private final int _capacity;
    
    public LruTable(int capacity) {
      super(capacity + 1, 0.75F, true);
      this._capacity = capacity;
    }
    
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
      return (size() > this._capacity);
    }
  }
}

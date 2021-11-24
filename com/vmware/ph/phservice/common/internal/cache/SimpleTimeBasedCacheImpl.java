package com.vmware.ph.phservice.common.internal.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Deprecated
public class SimpleTimeBasedCacheImpl<K, V> implements Cache<K, V> {
  private final ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();
  
  private final Lock _readLock = this._rwLock.readLock();
  
  private final Lock _writeLock = this._rwLock.writeLock();
  
  private final long _expirationIntervalMs;
  
  private final Map<K, V> _table;
  
  private final CacheInvalidateCallback<V> _invalidateCallback;
  
  private Date _lastCacheInvalidation;
  
  public SimpleTimeBasedCacheImpl(long expirationIntervalMs, int capacity) {
    this(expirationIntervalMs, capacity, null);
  }
  
  public SimpleTimeBasedCacheImpl(long expirationIntervalMs, int capacity, CacheInvalidateCallback<V> invalidateCallback) {
    this._expirationIntervalMs = expirationIntervalMs;
    this._table = new LruCacheImpl.LruTable<>(capacity);
    this._invalidateCallback = invalidateCallback;
    this._lastCacheInvalidation = new Date();
  }
  
  public void put(K key, V value) {
    invalidateCacheIfExpired();
    this._writeLock.lock();
    try {
      this._table.put(key, value);
    } finally {
      this._writeLock.unlock();
    } 
  }
  
  public V get(K key) {
    invalidateCacheIfExpired();
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
    invalidateCacheIfExpired();
    this._readLock.lock();
    try {
      return this._table.containsKey(key);
    } finally {
      this._readLock.unlock();
    } 
  }
  
  public void invalidate(K key) {
    invalidateCacheIfExpired();
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
      this._lastCacheInvalidation = new Date();
    } finally {
      this._writeLock.unlock();
    } 
    if (this._invalidateCallback != null && valuesCopy != null)
      for (V value : valuesCopy)
        this._invalidateCallback.cacheValueInvalidated(value);  
  }
  
  private void invalidateCacheIfExpired() {
    if (isCacheExpired())
      invalidate(); 
  }
  
  private boolean isCacheExpired() {
    this._readLock.lock();
    try {
      Date cacheExpirationTime = new Date(this._lastCacheInvalidation.getTime() + this._expirationIntervalMs);
      Date now = new Date();
      return now.after(cacheExpirationTime);
    } finally {
      this._readLock.unlock();
    } 
  }
}

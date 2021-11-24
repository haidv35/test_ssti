package com.vmware.ph.phservice.common.cdf;

import com.vmware.ph.client.api.commondataformat.PayloadEnvelope;
import com.vmware.ph.client.api.commondataformat20.Payload;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RetainingPayloadUploader implements PayloadUploader {
  private final List<Payload> _payloads = new LinkedList<>();
  
  public boolean isEnabled() {
    return true;
  }
  
  public Future<?> uploadPayload(Payload payload, PayloadEnvelope payloadEnvelope, String uploadId) {
    this._payloads.add(payload);
    return new ConstantFuture(null);
  }
  
  public List<Payload> getPayloads() {
    return this._payloads;
  }
  
  private static final class ConstantFuture<T> implements Future<T> {
    private final T value;
    
    ConstantFuture(T value) {
      this.value = value;
    }
    
    public boolean isDone() {
      return true;
    }
    
    public T get() {
      return this.value;
    }
    
    public T get(long timeout, TimeUnit unit) {
      return this.value;
    }
    
    public boolean isCancelled() {
      return false;
    }
    
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }
  }
}

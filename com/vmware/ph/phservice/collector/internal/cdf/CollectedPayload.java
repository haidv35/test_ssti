package com.vmware.ph.phservice.collector.internal.cdf;

import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.ContainsErrorInfo;
import java.util.Collection;
import org.apache.commons.lang.builder.ToStringBuilder;

public final class CollectedPayload extends ContainsErrorInfo {
  private final Payload _payload;
  
  private final boolean _isLastInBatch;
  
  private CollectedPayload(Payload payload, Exception collectorError, boolean fatalError, boolean isLastInBatch) {
    super(collectorError, fatalError);
    this._payload = (payload == null) ? (new Payload.Builder()).build() : payload;
    this._isLastInBatch = isLastInBatch;
  }
  
  public Payload getPayload() {
    return this._payload;
  }
  
  public boolean isLastInBatch() {
    return this._isLastInBatch;
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  
  public static final class Builder extends ContainsErrorInfo.Builder<Builder> {
    protected Payload.Builder _payloadBuilder = new Payload.Builder();
    
    protected boolean _isLastInBatch = false;
    
    public Builder setPayload(Payload.Builder payload) {
      this._payloadBuilder = payload;
      return this;
    }
    
    public Builder setPayload(Collection<JsonLd> payload) {
      this._payloadBuilder = (new Payload.Builder()).add(payload);
      return this;
    }
    
    public Builder setPayload(Payload payload) {
      this._payloadBuilder = (new Payload.Builder()).add(payload);
      return this;
    }
    
    public Builder setLastInBatch(boolean isLastInBatch) {
      this._isLastInBatch = isLastInBatch;
      return this;
    }
    
    public CollectedPayload build() {
      return new CollectedPayload(this._payloadBuilder.build(), this._error, this._fatal, this._isLastInBatch);
    }
  }
}

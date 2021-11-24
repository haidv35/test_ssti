package com.vmware.ph.phservice.provider.vsan.internal;

import java.util.Collections;
import java.util.List;

public class VsanPerfEntityMetrics {
  private final String _entityRefId;
  
  private final List<Object[]> _entityMetrics;
  
  public VsanPerfEntityMetrics(String entityRefId, List<Object[]> entityMetrics) {
    this._entityRefId = entityRefId;
    this._entityMetrics = Collections.unmodifiableList(entityMetrics);
  }
  
  public String getEntityRefId() {
    return this._entityRefId;
  }
  
  public List<Object[]> getEntityMetrics() {
    return this._entityMetrics;
  }
}

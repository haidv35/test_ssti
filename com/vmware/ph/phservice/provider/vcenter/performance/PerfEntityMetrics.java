package com.vmware.ph.phservice.provider.vcenter.performance;

import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.Collections;
import java.util.List;

public class PerfEntityMetrics {
  private final ManagedObjectReference _entityMoRef;
  
  private final List<Object[]> _entityMetrics;
  
  public PerfEntityMetrics(ManagedObjectReference entityMoRef, List<Object[]> entityMetrics) {
    this._entityMoRef = entityMoRef;
    this._entityMetrics = Collections.unmodifiableList(entityMetrics);
  }
  
  public ManagedObjectReference getEntityMoRef() {
    return this._entityMoRef;
  }
  
  public List<Object[]> getEntityMetrics() {
    return this._entityMetrics;
  }
}

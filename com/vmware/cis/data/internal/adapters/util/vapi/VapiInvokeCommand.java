package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.core.AsyncHandle;
import com.vmware.vapi.core.ExecutionContext;
import com.vmware.vapi.core.MethodResult;
import com.vmware.vapi.data.DataValue;

final class VapiInvokeCommand {
  private final ApiProvider _api;
  
  private final String _serviceId;
  
  private final String _operationId;
  
  private final DataValue _input;
  
  VapiInvokeCommand(ApiProvider api, String serviceId, String operationId, DataValue input) {
    assert api != null;
    assert serviceId != null;
    assert operationId != null;
    assert input != null;
    this._api = api;
    this._serviceId = serviceId;
    this._operationId = operationId;
    this._input = input;
  }
  
  void execute(ExecutionContext ctx, AsyncHandle<MethodResult> asyncHandle) {
    assert ctx != null;
    assert asyncHandle != null;
    this._api.invoke(this._serviceId, this._operationId, this._input, ctx, asyncHandle);
  }
  
  String getServiceId() {
    return this._serviceId;
  }
  
  String getOperationId() {
    return this._operationId;
  }
}

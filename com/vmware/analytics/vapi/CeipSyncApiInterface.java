package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.server.InvocationContext;
import com.vmware.vapi.core.AsyncHandle;
import com.vmware.vapi.core.MethodDefinition;
import com.vmware.vapi.core.MethodResult;
import com.vmware.vapi.data.DataValue;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.data.VoidValue;
import com.vmware.vapi.internal.bindings.ApiInterfaceSkeleton;
import com.vmware.vapi.internal.bindings.ApiMethodSkeleton;
import com.vmware.vapi.internal.bindings.StructValueExtractor;
import com.vmware.vapi.internal.bindings.TypeConverter;
import com.vmware.vapi.internal.bindings.TypeConverterImpl;
import com.vmware.vapi.internal.util.Validate;
import com.vmware.vapi.provider.ApiMethod;

public class CeipSyncApiInterface extends ApiInterfaceSkeleton {
  private CeipSyncProvider impl;
  
  private class GetApiMethod extends ApiMethodSkeleton {
    public GetApiMethod() {
      super(CeipSyncApiInterface.this.getIdentifier(), "get", CeipDefinitions.__getInput, CeipDefinitions.__getOutput, CeipSyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      try {
        asyncHandle.setResult(MethodResult.newResult(CeipSyncApiInterface.this.getTypeConverter().convertToVapi(Boolean.valueOf(CeipSyncApiInterface.this.impl.get(invocationContext)), CeipDefinitions.__getOutput, new TypeConverter.ConversionContext(invocationContext
                  
                  .getExecutionContext()))));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  private class SetApiMethod extends ApiMethodSkeleton {
    public SetApiMethod() {
      super(CeipSyncApiInterface.this.getIdentifier(), "set", CeipDefinitions.__setInput, CeipDefinitions.__setOutput, CeipSyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, CeipDefinitions.__setInput, CeipSyncApiInterface.this.getTypeConverter());
      boolean status = ((Boolean)extr.valueForField("status")).booleanValue();
      try {
        CeipSyncApiInterface.this.impl.set(status, invocationContext);
        asyncHandle.setResult(MethodResult.newResult((DataValue)VoidValue.getInstance()));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  public CeipSyncApiInterface() {
    this((Class<? extends CeipSyncProvider>)null);
  }
  
  public CeipSyncApiInterface(Class<? extends CeipSyncProvider> implClass) {
    this((CeipSyncProvider)createImplInstance(implClass, "com.vmware.analytics.vapi.impl.CeipImpl", CeipSyncProvider.class));
  }
  
  public CeipSyncApiInterface(CeipSyncProvider impl) {
    this(impl, (TypeConverter)new TypeConverterImpl());
  }
  
  public CeipSyncApiInterface(CeipSyncProvider impl, TypeConverter converter) {
    super("com.vmware.analytics.ceip", converter);
    Validate.notNull(impl);
    this.impl = impl;
    registerMethod((ApiMethod)new GetApiMethod());
    registerMethod((ApiMethod)new SetApiMethod());
  }
}

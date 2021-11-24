package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.server.AsyncContext;
import com.vmware.vapi.bindings.server.InvocationContext;
import com.vmware.vapi.core.AsyncHandle;
import com.vmware.vapi.core.MethodDefinition;
import com.vmware.vapi.core.MethodResult;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.internal.bindings.ApiInterfaceSkeleton;
import com.vmware.vapi.internal.bindings.ApiMethodSkeleton;
import com.vmware.vapi.internal.bindings.StructValueExtractor;
import com.vmware.vapi.internal.bindings.TypeConverter;
import com.vmware.vapi.internal.bindings.TypeConverterImpl;
import com.vmware.vapi.internal.bindings.server.impl.AsyncContextImpl;
import com.vmware.vapi.internal.util.Validate;
import com.vmware.vapi.provider.ApiMethod;

public class CeipApiInterface extends ApiInterfaceSkeleton {
  private CeipProvider impl;
  
  private class GetApiMethod extends ApiMethodSkeleton {
    public GetApiMethod() {
      super(CeipApiInterface.this.getIdentifier(), "get", CeipDefinitions.__getInput, CeipDefinitions.__getOutput, CeipApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      CeipApiInterface.this.impl.get((AsyncContext<Boolean>)new AsyncContextImpl(CeipApiInterface.this.getTypeConverter(), CeipDefinitions.__getOutput, invocationContext, asyncHandle, this));
    }
  }
  
  private class SetApiMethod extends ApiMethodSkeleton {
    public SetApiMethod() {
      super(CeipApiInterface.this.getIdentifier(), "set", CeipDefinitions.__setInput, CeipDefinitions.__setOutput, CeipApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, CeipDefinitions.__setInput, CeipApiInterface.this.getTypeConverter());
      boolean status = ((Boolean)extr.valueForField("status")).booleanValue();
      CeipApiInterface.this.impl.set(status, (AsyncContext<Void>)new AsyncContextImpl(CeipApiInterface.this.getTypeConverter(), CeipDefinitions.__setOutput, invocationContext, asyncHandle, this));
    }
  }
  
  public CeipApiInterface() {
    this((Class<? extends CeipProvider>)null);
  }
  
  public CeipApiInterface(Class<? extends CeipProvider> implClass) {
    this((CeipProvider)createImplInstance(implClass, "com.vmware.analytics.vapi.impl.CeipImpl", CeipProvider.class));
  }
  
  public CeipApiInterface(CeipProvider impl) {
    this(impl, (TypeConverter)new TypeConverterImpl());
  }
  
  public CeipApiInterface(CeipProvider impl, TypeConverter converter) {
    super("com.vmware.analytics.ceip", converter);
    Validate.notNull(impl);
    this.impl = impl;
    registerMethod((ApiMethod)new GetApiMethod());
    registerMethod((ApiMethod)new SetApiMethod());
  }
}

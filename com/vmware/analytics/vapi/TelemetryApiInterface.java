package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Structure;
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
import java.util.List;

public class TelemetryApiInterface extends ApiInterfaceSkeleton {
  private TelemetryProvider impl;
  
  private class SendApiMethod extends ApiMethodSkeleton {
    public SendApiMethod() {
      super(TelemetryApiInterface.this.getIdentifier(), "send", TelemetryDefinitions.__sendInput, TelemetryDefinitions.__sendOutput, TelemetryApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, TelemetryDefinitions.__sendInput, TelemetryApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String version = (String)extr.valueForField("version");
      Structure record = (Structure)extr.valueForField("record");
      TelemetryApiInterface.this.impl.send(collectorId, collectorInstanceId, version, record, (AsyncContext<Void>)new AsyncContextImpl(TelemetryApiInterface.this.getTypeConverter(), TelemetryDefinitions.__sendOutput, invocationContext, asyncHandle, this));
    }
  }
  
  private class SendMultipleApiMethod extends ApiMethodSkeleton {
    public SendMultipleApiMethod() {
      super(TelemetryApiInterface.this.getIdentifier(), "send_multiple", TelemetryDefinitions.__sendMultipleInput, TelemetryDefinitions.__sendMultipleOutput, TelemetryApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, TelemetryDefinitions.__sendMultipleInput, TelemetryApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String version = (String)extr.valueForField("version");
      List<Structure> records = (List<Structure>)extr.valueForField("records");
      TelemetryApiInterface.this.impl.sendMultiple(collectorId, collectorInstanceId, version, records, (AsyncContext<Void>)new AsyncContextImpl(TelemetryApiInterface.this.getTypeConverter(), TelemetryDefinitions.__sendMultipleOutput, invocationContext, asyncHandle, this));
    }
  }
  
  private class GetLevelApiMethod extends ApiMethodSkeleton {
    public GetLevelApiMethod() {
      super(TelemetryApiInterface.this.getIdentifier(), "get_level", TelemetryDefinitions.__getLevelInput, TelemetryDefinitions.__getLevelOutput, TelemetryApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, TelemetryDefinitions.__getLevelInput, TelemetryApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      TelemetryApiInterface.this.impl.getLevel(collectorId, collectorInstanceId, (AsyncContext<String>)new AsyncContextImpl(TelemetryApiInterface.this.getTypeConverter(), TelemetryDefinitions.__getLevelOutput, invocationContext, asyncHandle, this));
    }
  }
  
  public TelemetryApiInterface() {
    this((Class<? extends TelemetryProvider>)null);
  }
  
  public TelemetryApiInterface(Class<? extends TelemetryProvider> implClass) {
    this((TelemetryProvider)createImplInstance(implClass, "com.vmware.analytics.vapi.impl.TelemetryImpl", TelemetryProvider.class));
  }
  
  public TelemetryApiInterface(TelemetryProvider impl) {
    this(impl, (TypeConverter)new TypeConverterImpl());
  }
  
  public TelemetryApiInterface(TelemetryProvider impl, TypeConverter converter) {
    super("com.vmware.analytics.telemetry", converter);
    Validate.notNull(impl);
    this.impl = impl;
    registerMethod((ApiMethod)new SendApiMethod());
    registerMethod((ApiMethod)new SendMultipleApiMethod());
    registerMethod((ApiMethod)new GetLevelApiMethod());
  }
}

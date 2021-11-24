package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Structure;
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
import java.util.List;

public class TelemetrySyncApiInterface extends ApiInterfaceSkeleton {
  private TelemetrySyncProvider impl;
  
  private class SendApiMethod extends ApiMethodSkeleton {
    public SendApiMethod() {
      super(TelemetrySyncApiInterface.this.getIdentifier(), "send", TelemetryDefinitions.__sendInput, TelemetryDefinitions.__sendOutput, TelemetrySyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, TelemetryDefinitions.__sendInput, TelemetrySyncApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String version = (String)extr.valueForField("version");
      Structure record = (Structure)extr.valueForField("record");
      try {
        TelemetrySyncApiInterface.this.impl.send(collectorId, collectorInstanceId, version, record, invocationContext);
        asyncHandle.setResult(MethodResult.newResult((DataValue)VoidValue.getInstance()));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  private class SendMultipleApiMethod extends ApiMethodSkeleton {
    public SendMultipleApiMethod() {
      super(TelemetrySyncApiInterface.this.getIdentifier(), "send_multiple", TelemetryDefinitions.__sendMultipleInput, TelemetryDefinitions.__sendMultipleOutput, TelemetrySyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, TelemetryDefinitions.__sendMultipleInput, TelemetrySyncApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String version = (String)extr.valueForField("version");
      List<Structure> records = (List<Structure>)extr.valueForField("records");
      try {
        TelemetrySyncApiInterface.this.impl.sendMultiple(collectorId, collectorInstanceId, version, records, invocationContext);
        asyncHandle.setResult(MethodResult.newResult((DataValue)VoidValue.getInstance()));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  private class GetLevelApiMethod extends ApiMethodSkeleton {
    public GetLevelApiMethod() {
      super(TelemetrySyncApiInterface.this.getIdentifier(), "get_level", TelemetryDefinitions.__getLevelInput, TelemetryDefinitions.__getLevelOutput, TelemetrySyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, TelemetryDefinitions.__getLevelInput, TelemetrySyncApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      try {
        asyncHandle.setResult(MethodResult.newResult(TelemetrySyncApiInterface.this.getTypeConverter().convertToVapi(TelemetrySyncApiInterface.this.impl.getLevel(collectorId, collectorInstanceId, invocationContext), TelemetryDefinitions.__getLevelOutput, new TypeConverter.ConversionContext(invocationContext
                  
                  .getExecutionContext()))));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  public TelemetrySyncApiInterface() {
    this((Class<? extends TelemetrySyncProvider>)null);
  }
  
  public TelemetrySyncApiInterface(Class<? extends TelemetrySyncProvider> implClass) {
    this((TelemetrySyncProvider)createImplInstance(implClass, "com.vmware.analytics.vapi.impl.TelemetryImpl", TelemetrySyncProvider.class));
  }
  
  public TelemetrySyncApiInterface(TelemetrySyncProvider impl) {
    this(impl, (TypeConverter)new TypeConverterImpl());
  }
  
  public TelemetrySyncApiInterface(TelemetrySyncProvider impl, TypeConverter converter) {
    super("com.vmware.analytics.telemetry", converter);
    Validate.notNull(impl);
    this.impl = impl;
    registerMethod((ApiMethod)new SendApiMethod());
    registerMethod((ApiMethod)new SendMultipleApiMethod());
    registerMethod((ApiMethod)new GetLevelApiMethod());
  }
}

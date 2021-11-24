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

public class DataAppSyncApiInterface extends ApiInterfaceSkeleton {
  private DataAppSyncProvider impl;
  
  private class SendApiMethod extends ApiMethodSkeleton {
    public SendApiMethod() {
      super(DataAppSyncApiInterface.this.getIdentifier(), "send", DataAppDefinitions.__sendInput, DataAppDefinitions.__sendOutput, DataAppSyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppDefinitions.__sendInput, DataAppSyncApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String collectionId = (String)extr.valueForField("collection_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      String dataType = (String)extr.valueForField("data_type");
      String objectId = (String)extr.valueForField("object_id");
      byte[] payload = (byte[])extr.valueForField("payload");
      try {
        DataAppSyncApiInterface.this.impl.send(collectorId, collectorInstanceId, collectionId, deploymentSecret, pluginType, dataType, objectId, payload, invocationContext);
        asyncHandle.setResult(MethodResult.newResult((DataValue)VoidValue.getInstance()));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  private class GetResultsApiMethod extends ApiMethodSkeleton {
    public GetResultsApiMethod() {
      super(DataAppSyncApiInterface.this.getIdentifier(), "get_results", DataAppDefinitions.__getResultsInput, DataAppDefinitions.__getResultsOutput, DataAppSyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppDefinitions.__getResultsInput, DataAppSyncApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String dataType = (String)extr.valueForField("data_type");
      String objectId = (String)extr.valueForField("object_id");
      Long sinceTimestamp = (Long)extr.valueForField("since_timestamp");
      String pluginType = (String)extr.valueForField("plugin_type");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      try {
        asyncHandle.setResult(MethodResult.newResult(DataAppSyncApiInterface.this.getTypeConverter().convertToVapi(DataAppSyncApiInterface.this.impl.getResults(collectorId, collectorInstanceId, dataType, objectId, sinceTimestamp, pluginType, deploymentSecret, invocationContext), DataAppDefinitions.__getResultsOutput, new TypeConverter.ConversionContext(invocationContext
                  
                  .getExecutionContext()))));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  public DataAppSyncApiInterface() {
    this((Class<? extends DataAppSyncProvider>)null);
  }
  
  public DataAppSyncApiInterface(Class<? extends DataAppSyncProvider> implClass) {
    this((DataAppSyncProvider)createImplInstance(implClass, "com.vmware.analytics.vapi.impl.DataAppImpl", DataAppSyncProvider.class));
  }
  
  public DataAppSyncApiInterface(DataAppSyncProvider impl) {
    this(impl, (TypeConverter)new TypeConverterImpl());
  }
  
  public DataAppSyncApiInterface(DataAppSyncProvider impl, TypeConverter converter) {
    super("com.vmware.analytics.data_app", converter);
    Validate.notNull(impl);
    this.impl = impl;
    registerMethod((ApiMethod)new SendApiMethod());
    registerMethod((ApiMethod)new GetResultsApiMethod());
  }
}

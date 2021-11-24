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

public class DataAppApiInterface extends ApiInterfaceSkeleton {
  private DataAppProvider impl;
  
  private class SendApiMethod extends ApiMethodSkeleton {
    public SendApiMethod() {
      super(DataAppApiInterface.this.getIdentifier(), "send", DataAppDefinitions.__sendInput, DataAppDefinitions.__sendOutput, DataAppApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppDefinitions.__sendInput, DataAppApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String collectionId = (String)extr.valueForField("collection_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      String dataType = (String)extr.valueForField("data_type");
      String objectId = (String)extr.valueForField("object_id");
      byte[] payload = (byte[])extr.valueForField("payload");
      DataAppApiInterface.this.impl.send(collectorId, collectorInstanceId, collectionId, deploymentSecret, pluginType, dataType, objectId, payload, (AsyncContext<Void>)new AsyncContextImpl(DataAppApiInterface.this.getTypeConverter(), DataAppDefinitions.__sendOutput, invocationContext, asyncHandle, this));
    }
  }
  
  private class GetResultsApiMethod extends ApiMethodSkeleton {
    public GetResultsApiMethod() {
      super(DataAppApiInterface.this.getIdentifier(), "get_results", DataAppDefinitions.__getResultsInput, DataAppDefinitions.__getResultsOutput, DataAppApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppDefinitions.__getResultsInput, DataAppApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String dataType = (String)extr.valueForField("data_type");
      String objectId = (String)extr.valueForField("object_id");
      Long sinceTimestamp = (Long)extr.valueForField("since_timestamp");
      String pluginType = (String)extr.valueForField("plugin_type");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      DataAppApiInterface.this.impl.getResults(collectorId, collectorInstanceId, dataType, objectId, sinceTimestamp, pluginType, deploymentSecret, (AsyncContext<Structure>)new AsyncContextImpl(DataAppApiInterface.this.getTypeConverter(), DataAppDefinitions.__getResultsOutput, invocationContext, asyncHandle, this));
    }
  }
  
  public DataAppApiInterface() {
    this((Class<? extends DataAppProvider>)null);
  }
  
  public DataAppApiInterface(Class<? extends DataAppProvider> implClass) {
    this((DataAppProvider)createImplInstance(implClass, "com.vmware.analytics.vapi.impl.DataAppImpl", DataAppProvider.class));
  }
  
  public DataAppApiInterface(DataAppProvider impl) {
    this(impl, (TypeConverter)new TypeConverterImpl());
  }
  
  public DataAppApiInterface(DataAppProvider impl, TypeConverter converter) {
    super("com.vmware.analytics.data_app", converter);
    Validate.notNull(impl);
    this.impl = impl;
    registerMethod((ApiMethod)new SendApiMethod());
    registerMethod((ApiMethod)new GetResultsApiMethod());
  }
}

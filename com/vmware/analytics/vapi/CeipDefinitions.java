package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.type.BooleanType;
import com.vmware.vapi.bindings.type.StructType;
import com.vmware.vapi.bindings.type.Type;
import com.vmware.vapi.bindings.type.VoidType;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.internal.bindings.OperationDef;
import com.vmware.vapi.internal.data.ConstraintValidator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CeipDefinitions {
  public static final StructType __getInput = __getInputInit();
  
  public static final Type __getOutput = (Type)new BooleanType();
  
  public static final OperationDef __getDef = __getDefInit();
  
  public static final StructType __setInput = __setInputInit();
  
  public static final Type __setOutput = (Type)new VoidType();
  
  public static final OperationDef __setDef = __setDefInit();
  
  private static StructType __getInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __getDefInit() {
    OperationDef operationDef = new OperationDef("get", "/ceip", "GET", null, null);
    return operationDef;
  }
  
  private static StructType __setInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    fields.put("status", new BooleanType());
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __setDefInit() {
    OperationDef operationDef = new OperationDef("set", "/ceip", "PUT", null, null);
    operationDef.registerRequestParam("status", "status");
    return operationDef;
  }
  
  public static final List<OperationDef> __operationDefs = Arrays.asList(new OperationDef[] { __getDef, __setDef });
}

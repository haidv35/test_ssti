package com.vmware.cis.data.internal.adapters.vapi.impl;

import com.vmware.vapi.data.BlobValue;
import com.vmware.vapi.data.BooleanValue;
import com.vmware.vapi.data.DataValue;
import com.vmware.vapi.data.DoubleValue;
import com.vmware.vapi.data.IntegerValue;
import com.vmware.vapi.data.ListValue;
import com.vmware.vapi.data.OptionalValue;
import com.vmware.vapi.data.SecretValue;
import com.vmware.vapi.data.StringValue;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.data.ValueVisitor;
import com.vmware.vapi.data.VoidValue;
import java.util.ArrayList;
import java.util.List;

final class VapiValueConverter {
  public static Object fromDataValue(DataValue dataValue) {
    if (dataValue != null) {
      CoreValueVisitor visitor = new CoreValueVisitor();
      dataValue.accept(visitor);
      return visitor.getPlainObject();
    } 
    return null;
  }
  
  private static class CoreValueVisitor implements ValueVisitor {
    private Object _coreValue;
    
    private CoreValueVisitor() {}
    
    public Object getPlainObject() {
      return this._coreValue;
    }
    
    public void visit(VoidValue voidValue) {
      this._coreValue = voidValue;
    }
    
    public void visit(BooleanValue booleanValue) {
      this._coreValue = Boolean.valueOf(booleanValue.getValue());
    }
    
    public void visit(IntegerValue integerValue) {
      this._coreValue = Long.valueOf(integerValue.getValue());
    }
    
    public void visit(DoubleValue doubleValue) {
      this._coreValue = Double.valueOf(doubleValue.getValue());
    }
    
    public void visit(StringValue stringValue) {
      this._coreValue = stringValue.getValue();
    }
    
    public void visit(SecretValue secretValue) {
      this._coreValue = secretValue.getValue();
    }
    
    public void visit(BlobValue blobValue) {
      this._coreValue = blobValue.getValue();
    }
    
    public void visit(OptionalValue optionalValue) {
      if (!optionalValue.isSet()) {
        this._coreValue = null;
        return;
      } 
      this._coreValue = VapiValueConverter.fromDataValue(optionalValue.getValue());
    }
    
    public void visit(ListValue listValue) {
      List<Object> list = new ArrayList(listValue.size());
      for (DataValue dataValue : listValue)
        list.add(VapiValueConverter.fromDataValue(dataValue)); 
      this._coreValue = list;
    }
    
    public void visit(StructValue structValue) {
      this._coreValue = structValue;
    }
  }
}

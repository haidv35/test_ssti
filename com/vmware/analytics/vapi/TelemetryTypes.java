package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.StaticStructure;
import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.bindings.type.StructType;
import com.vmware.vapi.client.exception.BindingsException;
import com.vmware.vapi.data.DataValue;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.internal.bindings.BindingsUtil;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

public interface TelemetryTypes {
  public static final String _VAPI_SERVICE_ID = "com.vmware.analytics.telemetry";
  
  public static final class TelemetryRecord implements Serializable, StaticStructure {
    private static final long serialVersionUID = 1L;
    
    private String type;
    
    private String id;
    
    protected StructValue __dynamicStructureFields;
    
    public TelemetryRecord() {}
    
    protected TelemetryRecord(StructValue __dynamicStructureFields) {
      this.__dynamicStructureFields = __dynamicStructureFields;
    }
    
    public String getType() {
      return this.type;
    }
    
    public void setType(String type) {
      this.type = type;
    }
    
    public String getId() {
      return this.id;
    }
    
    public void setId(String id) {
      this.id = id;
    }
    
    public StructType _getType() {
      return TelemetryDefinitions.telemetryRecord;
    }
    
    public StructValue _getDataValue() {
      StructValue dataValue;
      if (this.__dynamicStructureFields != null) {
        dataValue = this.__dynamicStructureFields.copy();
      } else {
        dataValue = createEmptyStructValue();
      } 
      _updateDataValue(dataValue);
      return dataValue;
    }
    
    protected void _updateDataValue(StructValue structValue) {
      structValue.setField("@type", 
          BindingsUtil.toDataValue(this.type, 
            _getType().getField("@type")));
      structValue.setField("@id", 
          BindingsUtil.toDataValue(this.id, 
            _getType().getField("@id")));
    }
    
    public void _validate() {
      _getType().validate(_getDataValue());
    }
    
    public boolean equals(Object obj) {
      return BindingsUtil.areEqual(this, obj);
    }
    
    public int hashCode() {
      return BindingsUtil.computeHashCode(this);
    }
    
    public String toString() {
      return BindingsUtil.convertToString(this, this.__dynamicStructureFields);
    }
    
    public boolean _hasTypeNameOf(Class<? extends Structure> clazz) {
      return BindingsUtil.hasTypeNameOf(_getDataValue(), clazz);
    }
    
    public <T extends Structure> T _convertTo(Class<T> clazz) {
      return (T)BindingsUtil.convertTo((Structure)this, clazz);
    }
    
    public void _setDynamicField(String fieldName, DataValue fieldValue) {
      if (_getType().getFieldNames().contains(fieldName))
        throw new BindingsException("The structure contains static field with name " + fieldName); 
      if (this.__dynamicStructureFields == null)
        this.__dynamicStructureFields = createEmptyStructValue(); 
      this.__dynamicStructureFields.setField(fieldName, fieldValue);
    }
    
    public DataValue _getDynamicField(String fieldName) {
      if (this.__dynamicStructureFields == null || !this.__dynamicStructureFields.getFieldNames().contains(fieldName))
        throw new BindingsException("The structure doesn't contain dynamic field with name " + fieldName); 
      return this.__dynamicStructureFields.getField(fieldName);
    }
    
    public Set<String> _getDynamicFieldNames() {
      if (this.__dynamicStructureFields == null)
        return Collections.emptySet(); 
      return this.__dynamicStructureFields.getFieldNames();
    }
    
    public static StructType _getClassType() {
      return TelemetryDefinitions.telemetryRecord;
    }
    
    public String _getCanonicalName() {
      if (this.__dynamicStructureFields != null)
        return this.__dynamicStructureFields.getName(); 
      return TelemetryDefinitions.telemetryRecord.getName();
    }
    
    public static String _getCanonicalTypeName() {
      return _getClassType().getName();
    }
    
    private static StructValue createEmptyStructValue() {
      return new StructValue(_getCanonicalTypeName());
    }
    
    public static TelemetryRecord _newInstance(StructValue structValue) {
      return new TelemetryRecord(structValue);
    }
    
    public static TelemetryRecord _newInstance2(StructValue structValue) {
      return new TelemetryRecord(structValue);
    }
    
    public static final class Builder {
      private String type;
      
      private String id;
      
      public Builder(String type, String id) {
        this.type = type;
        this.id = id;
      }
      
      public TelemetryTypes.TelemetryRecord build() {
        TelemetryTypes.TelemetryRecord result = new TelemetryTypes.TelemetryRecord();
        result.setType(this.type);
        result.setId(this.id);
        return result;
      }
    }
  }
  
  public static final class NestedTelemetryRecord implements Serializable, StaticStructure {
    private static final long serialVersionUID = 1L;
    
    private String type;
    
    private String id;
    
    protected StructValue __dynamicStructureFields;
    
    public NestedTelemetryRecord() {}
    
    protected NestedTelemetryRecord(StructValue __dynamicStructureFields) {
      this.__dynamicStructureFields = __dynamicStructureFields;
    }
    
    public String getType() {
      return this.type;
    }
    
    public void setType(String type) {
      this.type = type;
    }
    
    public String getId() {
      return this.id;
    }
    
    public void setId(String id) {
      this.id = id;
    }
    
    public StructType _getType() {
      return TelemetryDefinitions.nestedTelemetryRecord;
    }
    
    public StructValue _getDataValue() {
      StructValue dataValue;
      if (this.__dynamicStructureFields != null) {
        dataValue = this.__dynamicStructureFields.copy();
      } else {
        dataValue = createEmptyStructValue();
      } 
      _updateDataValue(dataValue);
      return dataValue;
    }
    
    protected void _updateDataValue(StructValue structValue) {
      structValue.setField("@type", 
          BindingsUtil.toDataValue(this.type, 
            _getType().getField("@type")));
      structValue.setField("@id", 
          BindingsUtil.toDataValue(this.id, 
            _getType().getField("@id")));
    }
    
    public void _validate() {
      _getType().validate(_getDataValue());
    }
    
    public boolean equals(Object obj) {
      return BindingsUtil.areEqual(this, obj);
    }
    
    public int hashCode() {
      return BindingsUtil.computeHashCode(this);
    }
    
    public String toString() {
      return BindingsUtil.convertToString(this, this.__dynamicStructureFields);
    }
    
    public boolean _hasTypeNameOf(Class<? extends Structure> clazz) {
      return BindingsUtil.hasTypeNameOf(_getDataValue(), clazz);
    }
    
    public <T extends Structure> T _convertTo(Class<T> clazz) {
      return (T)BindingsUtil.convertTo((Structure)this, clazz);
    }
    
    public void _setDynamicField(String fieldName, DataValue fieldValue) {
      if (_getType().getFieldNames().contains(fieldName))
        throw new BindingsException("The structure contains static field with name " + fieldName); 
      if (this.__dynamicStructureFields == null)
        this.__dynamicStructureFields = createEmptyStructValue(); 
      this.__dynamicStructureFields.setField(fieldName, fieldValue);
    }
    
    public DataValue _getDynamicField(String fieldName) {
      if (this.__dynamicStructureFields == null || !this.__dynamicStructureFields.getFieldNames().contains(fieldName))
        throw new BindingsException("The structure doesn't contain dynamic field with name " + fieldName); 
      return this.__dynamicStructureFields.getField(fieldName);
    }
    
    public Set<String> _getDynamicFieldNames() {
      if (this.__dynamicStructureFields == null)
        return Collections.emptySet(); 
      return this.__dynamicStructureFields.getFieldNames();
    }
    
    public static StructType _getClassType() {
      return TelemetryDefinitions.nestedTelemetryRecord;
    }
    
    public String _getCanonicalName() {
      if (this.__dynamicStructureFields != null)
        return this.__dynamicStructureFields.getName(); 
      return TelemetryDefinitions.nestedTelemetryRecord.getName();
    }
    
    public static String _getCanonicalTypeName() {
      return _getClassType().getName();
    }
    
    private static StructValue createEmptyStructValue() {
      return new StructValue(_getCanonicalTypeName());
    }
    
    public static NestedTelemetryRecord _newInstance(StructValue structValue) {
      return new NestedTelemetryRecord(structValue);
    }
    
    public static NestedTelemetryRecord _newInstance2(StructValue structValue) {
      return new NestedTelemetryRecord(structValue);
    }
    
    public static final class Builder {
      private String type;
      
      private String id;
      
      public Builder setType(String type) {
        this.type = type;
        return this;
      }
      
      public Builder setId(String id) {
        this.id = id;
        return this;
      }
      
      public TelemetryTypes.NestedTelemetryRecord build() {
        TelemetryTypes.NestedTelemetryRecord result = new TelemetryTypes.NestedTelemetryRecord();
        result.setType(this.type);
        result.setId(this.id);
        return result;
      }
    }
  }
}

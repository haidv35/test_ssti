package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.ApiEnumeration;
import com.vmware.vapi.bindings.StaticStructure;
import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.bindings.type.StructType;
import com.vmware.vapi.client.exception.BindingsException;
import com.vmware.vapi.data.DataValue;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.internal.bindings.BindingsUtil;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface DataAppAgentTypes {
  public static final String _VAPI_SERVICE_ID = "com.vmware.analytics.data_app_agent";
  
  public static final class AuditStatus extends ApiEnumeration<AuditStatus> {
    public enum Values {
      STARTED, PROCESSING, DONE, ERROR, _UNKNOWN;
    }
    
    public static final AuditStatus STARTED = new AuditStatus("STARTED");
    
    public static final AuditStatus PROCESSING = new AuditStatus("PROCESSING");
    
    public static final AuditStatus DONE = new AuditStatus("DONE");
    
    public static final AuditStatus ERROR = new AuditStatus("ERROR");
    
    private static final long serialVersionUID = 1L;
    
    private static final AuditStatus[] $VALUES = new AuditStatus[] { STARTED, PROCESSING, DONE, ERROR };
    
    private static final Map<String, AuditStatus> $NAME_TO_VALUE_MAP = ApiEnumeration.buildNameMap((ApiEnumeration[])$VALUES);
    
    private AuditStatus() {
      super(Values._UNKNOWN.name());
    }
    
    private AuditStatus(String name) {
      super(name);
    }
    
    public static AuditStatus[] values() {
      return (AuditStatus[])$VALUES.clone();
    }
    
    public static AuditStatus valueOf(String name) {
      if (name == null)
        throw new NullPointerException(); 
      AuditStatus predefined = $NAME_TO_VALUE_MAP.get(name);
      if (predefined != null)
        return predefined; 
      return new AuditStatus(name);
    }
    
    public boolean isUnknown() {
      return (getEnumValue() == Values._UNKNOWN);
    }
    
    public Values getEnumValue() {
      try {
        return Values.valueOf(name());
      } catch (IllegalArgumentException ex) {
        return Values._UNKNOWN;
      } 
    }
    
    private Object readResolve() {
      return valueOf(name());
    }
  }
  
  public static final class ManifestSpec implements Serializable, StaticStructure {
    private static final long serialVersionUID = 1L;
    
    private String resourceId;
    
    private String dataType;
    
    private String objectId;
    
    private String versionDataType;
    
    private String versionObjectId;
    
    protected StructValue __dynamicStructureFields;
    
    public ManifestSpec() {}
    
    protected ManifestSpec(StructValue __dynamicStructureFields) {
      this.__dynamicStructureFields = __dynamicStructureFields;
    }
    
    public String getResourceId() {
      return this.resourceId;
    }
    
    public void setResourceId(String resourceId) {
      this.resourceId = resourceId;
    }
    
    public String getDataType() {
      return this.dataType;
    }
    
    public void setDataType(String dataType) {
      this.dataType = dataType;
    }
    
    public String getObjectId() {
      return this.objectId;
    }
    
    public void setObjectId(String objectId) {
      this.objectId = objectId;
    }
    
    public String getVersionDataType() {
      return this.versionDataType;
    }
    
    public void setVersionDataType(String versionDataType) {
      this.versionDataType = versionDataType;
    }
    
    public String getVersionObjectId() {
      return this.versionObjectId;
    }
    
    public void setVersionObjectId(String versionObjectId) {
      this.versionObjectId = versionObjectId;
    }
    
    public StructType _getType() {
      return DataAppAgentDefinitions.manifestSpec;
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
      structValue.setField("resourceId", 
          BindingsUtil.toDataValue(this.resourceId, 
            _getType().getField("resourceId")));
      structValue.setField("dataType", 
          BindingsUtil.toDataValue(this.dataType, 
            _getType().getField("dataType")));
      structValue.setField("objectId", 
          BindingsUtil.toDataValue(this.objectId, 
            _getType().getField("objectId")));
      structValue.setField("versionDataType", 
          BindingsUtil.toDataValue(this.versionDataType, 
            _getType().getField("versionDataType")));
      structValue.setField("versionObjectId", 
          BindingsUtil.toDataValue(this.versionObjectId, 
            _getType().getField("versionObjectId")));
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
      return DataAppAgentDefinitions.manifestSpec;
    }
    
    public String _getCanonicalName() {
      if (this.__dynamicStructureFields != null)
        return this.__dynamicStructureFields.getName(); 
      return DataAppAgentDefinitions.manifestSpec.getName();
    }
    
    public static String _getCanonicalTypeName() {
      return _getClassType().getName();
    }
    
    private static StructValue createEmptyStructValue() {
      return new StructValue(_getCanonicalTypeName());
    }
    
    public static ManifestSpec _newInstance(StructValue structValue) {
      return new ManifestSpec(structValue);
    }
    
    public static ManifestSpec _newInstance2(StructValue structValue) {
      return new ManifestSpec(structValue);
    }
    
    public static final class Builder {
      private String resourceId;
      
      private String dataType;
      
      private String objectId;
      
      private String versionDataType;
      
      private String versionObjectId;
      
      public Builder(String resourceId, String dataType, String objectId) {
        this.resourceId = resourceId;
        this.dataType = dataType;
        this.objectId = objectId;
      }
      
      public Builder setVersionDataType(String versionDataType) {
        this.versionDataType = versionDataType;
        return this;
      }
      
      public Builder setVersionObjectId(String versionObjectId) {
        this.versionObjectId = versionObjectId;
        return this;
      }
      
      public DataAppAgentTypes.ManifestSpec build() {
        DataAppAgentTypes.ManifestSpec result = new DataAppAgentTypes.ManifestSpec();
        result.setResourceId(this.resourceId);
        result.setDataType(this.dataType);
        result.setObjectId(this.objectId);
        result.setVersionDataType(this.versionDataType);
        result.setVersionObjectId(this.versionObjectId);
        return result;
      }
    }
  }
  
  public static final class CreateSpec implements Serializable, StaticStructure {
    private static final long serialVersionUID = 1L;
    
    private DataAppAgentTypes.ManifestSpec manifestSpec;
    
    private String objectType;
    
    private Boolean collectionTriggerDataNeeded;
    
    private Boolean deploymentDataNeeded;
    
    private Boolean resultNeeded;
    
    private Boolean signalCollectionCompleted;
    
    private String localManifestPath;
    
    private String localPayloadPath;
    
    private String localObfuscationMapPath;
    
    protected StructValue __dynamicStructureFields;
    
    public CreateSpec() {}
    
    protected CreateSpec(StructValue __dynamicStructureFields) {
      this.__dynamicStructureFields = __dynamicStructureFields;
    }
    
    public DataAppAgentTypes.ManifestSpec getManifestSpec() {
      return this.manifestSpec;
    }
    
    public void setManifestSpec(DataAppAgentTypes.ManifestSpec manifestSpec) {
      this.manifestSpec = manifestSpec;
    }
    
    public String getObjectType() {
      return this.objectType;
    }
    
    public void setObjectType(String objectType) {
      this.objectType = objectType;
    }
    
    public Boolean getCollectionTriggerDataNeeded() {
      return this.collectionTriggerDataNeeded;
    }
    
    public void setCollectionTriggerDataNeeded(Boolean collectionTriggerDataNeeded) {
      this.collectionTriggerDataNeeded = collectionTriggerDataNeeded;
    }
    
    public Boolean getDeploymentDataNeeded() {
      return this.deploymentDataNeeded;
    }
    
    public void setDeploymentDataNeeded(Boolean deploymentDataNeeded) {
      this.deploymentDataNeeded = deploymentDataNeeded;
    }
    
    public Boolean getResultNeeded() {
      return this.resultNeeded;
    }
    
    public void setResultNeeded(Boolean resultNeeded) {
      this.resultNeeded = resultNeeded;
    }
    
    public Boolean getSignalCollectionCompleted() {
      return this.signalCollectionCompleted;
    }
    
    public void setSignalCollectionCompleted(Boolean signalCollectionCompleted) {
      this.signalCollectionCompleted = signalCollectionCompleted;
    }
    
    public String getLocalManifestPath() {
      return this.localManifestPath;
    }
    
    public void setLocalManifestPath(String localManifestPath) {
      this.localManifestPath = localManifestPath;
    }
    
    public String getLocalPayloadPath() {
      return this.localPayloadPath;
    }
    
    public void setLocalPayloadPath(String localPayloadPath) {
      this.localPayloadPath = localPayloadPath;
    }
    
    public String getLocalObfuscationMapPath() {
      return this.localObfuscationMapPath;
    }
    
    public void setLocalObfuscationMapPath(String localObfuscationMapPath) {
      this.localObfuscationMapPath = localObfuscationMapPath;
    }
    
    public StructType _getType() {
      return DataAppAgentDefinitions.createSpec;
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
      structValue.setField("manifestSpec", 
          BindingsUtil.toDataValue(this.manifestSpec, 
            _getType().getField("manifestSpec")));
      structValue.setField("objectType", 
          BindingsUtil.toDataValue(this.objectType, 
            _getType().getField("objectType")));
      structValue.setField("collectionTriggerDataNeeded", 
          BindingsUtil.toDataValue(this.collectionTriggerDataNeeded, 
            _getType().getField("collectionTriggerDataNeeded")));
      structValue.setField("deploymentDataNeeded", 
          BindingsUtil.toDataValue(this.deploymentDataNeeded, 
            _getType().getField("deploymentDataNeeded")));
      structValue.setField("resultNeeded", 
          BindingsUtil.toDataValue(this.resultNeeded, 
            _getType().getField("resultNeeded")));
      structValue.setField("signalCollectionCompleted", 
          BindingsUtil.toDataValue(this.signalCollectionCompleted, 
            _getType().getField("signalCollectionCompleted")));
      structValue.setField("localManifestPath", 
          BindingsUtil.toDataValue(this.localManifestPath, 
            _getType().getField("localManifestPath")));
      structValue.setField("localPayloadPath", 
          BindingsUtil.toDataValue(this.localPayloadPath, 
            _getType().getField("localPayloadPath")));
      structValue.setField("localObfuscationMapPath", 
          BindingsUtil.toDataValue(this.localObfuscationMapPath, 
            _getType().getField("localObfuscationMapPath")));
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
      return DataAppAgentDefinitions.createSpec;
    }
    
    public String _getCanonicalName() {
      if (this.__dynamicStructureFields != null)
        return this.__dynamicStructureFields.getName(); 
      return DataAppAgentDefinitions.createSpec.getName();
    }
    
    public static String _getCanonicalTypeName() {
      return _getClassType().getName();
    }
    
    private static StructValue createEmptyStructValue() {
      return new StructValue(_getCanonicalTypeName());
    }
    
    public static CreateSpec _newInstance(StructValue structValue) {
      return new CreateSpec(structValue);
    }
    
    public static CreateSpec _newInstance2(StructValue structValue) {
      return new CreateSpec(structValue);
    }
    
    public static final class Builder {
      private DataAppAgentTypes.ManifestSpec manifestSpec;
      
      private String objectType;
      
      private Boolean collectionTriggerDataNeeded;
      
      private Boolean deploymentDataNeeded;
      
      private Boolean resultNeeded;
      
      private Boolean signalCollectionCompleted;
      
      private String localManifestPath;
      
      private String localPayloadPath;
      
      private String localObfuscationMapPath;
      
      public Builder setManifestSpec(DataAppAgentTypes.ManifestSpec manifestSpec) {
        this.manifestSpec = manifestSpec;
        return this;
      }
      
      public Builder setObjectType(String objectType) {
        this.objectType = objectType;
        return this;
      }
      
      public Builder setCollectionTriggerDataNeeded(Boolean collectionTriggerDataNeeded) {
        this.collectionTriggerDataNeeded = collectionTriggerDataNeeded;
        return this;
      }
      
      public Builder setDeploymentDataNeeded(Boolean deploymentDataNeeded) {
        this.deploymentDataNeeded = deploymentDataNeeded;
        return this;
      }
      
      public Builder setResultNeeded(Boolean resultNeeded) {
        this.resultNeeded = resultNeeded;
        return this;
      }
      
      public Builder setSignalCollectionCompleted(Boolean signalCollectionCompleted) {
        this.signalCollectionCompleted = signalCollectionCompleted;
        return this;
      }
      
      public Builder setLocalManifestPath(String localManifestPath) {
        this.localManifestPath = localManifestPath;
        return this;
      }
      
      public Builder setLocalPayloadPath(String localPayloadPath) {
        this.localPayloadPath = localPayloadPath;
        return this;
      }
      
      public Builder setLocalObfuscationMapPath(String localObfuscationMapPath) {
        this.localObfuscationMapPath = localObfuscationMapPath;
        return this;
      }
      
      public DataAppAgentTypes.CreateSpec build() {
        DataAppAgentTypes.CreateSpec result = new DataAppAgentTypes.CreateSpec();
        result.setManifestSpec(this.manifestSpec);
        result.setObjectType(this.objectType);
        result.setCollectionTriggerDataNeeded(this.collectionTriggerDataNeeded);
        result.setDeploymentDataNeeded(this.deploymentDataNeeded);
        result.setResultNeeded(this.resultNeeded);
        result.setSignalCollectionCompleted(this.signalCollectionCompleted);
        result.setLocalManifestPath(this.localManifestPath);
        result.setLocalPayloadPath(this.localPayloadPath);
        result.setLocalObfuscationMapPath(this.localObfuscationMapPath);
        return result;
      }
    }
  }
  
  public static final class CollectRequestSpec implements Serializable, StaticStructure {
    private static final long serialVersionUID = 1L;
    
    private String manifestContent;
    
    private String objectId;
    
    private String contextData;
    
    protected StructValue __dynamicStructureFields;
    
    public CollectRequestSpec() {}
    
    protected CollectRequestSpec(StructValue __dynamicStructureFields) {
      this.__dynamicStructureFields = __dynamicStructureFields;
    }
    
    public String getManifestContent() {
      return this.manifestContent;
    }
    
    public void setManifestContent(String manifestContent) {
      this.manifestContent = manifestContent;
    }
    
    public String getObjectId() {
      return this.objectId;
    }
    
    public void setObjectId(String objectId) {
      this.objectId = objectId;
    }
    
    public String getContextData() {
      return this.contextData;
    }
    
    public void setContextData(String contextData) {
      this.contextData = contextData;
    }
    
    public StructType _getType() {
      return DataAppAgentDefinitions.collectRequestSpec;
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
      structValue.setField("manifestContent", 
          BindingsUtil.toDataValue(this.manifestContent, 
            _getType().getField("manifestContent")));
      structValue.setField("objectId", 
          BindingsUtil.toDataValue(this.objectId, 
            _getType().getField("objectId")));
      structValue.setField("contextData", 
          BindingsUtil.toDataValue(this.contextData, 
            _getType().getField("contextData")));
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
      return DataAppAgentDefinitions.collectRequestSpec;
    }
    
    public String _getCanonicalName() {
      if (this.__dynamicStructureFields != null)
        return this.__dynamicStructureFields.getName(); 
      return DataAppAgentDefinitions.collectRequestSpec.getName();
    }
    
    public static String _getCanonicalTypeName() {
      return _getClassType().getName();
    }
    
    private static StructValue createEmptyStructValue() {
      return new StructValue(_getCanonicalTypeName());
    }
    
    public static CollectRequestSpec _newInstance(StructValue structValue) {
      return new CollectRequestSpec(structValue);
    }
    
    public static CollectRequestSpec _newInstance2(StructValue structValue) {
      return new CollectRequestSpec(structValue);
    }
    
    public static final class Builder {
      private String manifestContent;
      
      private String objectId;
      
      private String contextData;
      
      public Builder setManifestContent(String manifestContent) {
        this.manifestContent = manifestContent;
        return this;
      }
      
      public Builder setObjectId(String objectId) {
        this.objectId = objectId;
        return this;
      }
      
      public Builder setContextData(String contextData) {
        this.contextData = contextData;
        return this;
      }
      
      public DataAppAgentTypes.CollectRequestSpec build() {
        DataAppAgentTypes.CollectRequestSpec result = new DataAppAgentTypes.CollectRequestSpec();
        result.setManifestContent(this.manifestContent);
        result.setObjectId(this.objectId);
        result.setContextData(this.contextData);
        return result;
      }
    }
  }
  
  public static final class AuditResult implements Serializable, StaticStructure {
    private static final long serialVersionUID = 1L;
    
    private DataAppAgentTypes.AuditStatus status;
    
    private String result;
    
    protected StructValue __dynamicStructureFields;
    
    public AuditResult() {}
    
    protected AuditResult(StructValue __dynamicStructureFields) {
      this.__dynamicStructureFields = __dynamicStructureFields;
    }
    
    public DataAppAgentTypes.AuditStatus getStatus() {
      return this.status;
    }
    
    public void setStatus(DataAppAgentTypes.AuditStatus status) {
      this.status = status;
    }
    
    public String getResult() {
      return this.result;
    }
    
    public void setResult(String result) {
      this.result = result;
    }
    
    public StructType _getType() {
      return DataAppAgentDefinitions.auditResult;
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
      structValue.setField("status", 
          BindingsUtil.toDataValue(this.status, 
            _getType().getField("status")));
      structValue.setField("result", 
          BindingsUtil.toDataValue(this.result, 
            _getType().getField("result")));
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
      return DataAppAgentDefinitions.auditResult;
    }
    
    public String _getCanonicalName() {
      if (this.__dynamicStructureFields != null)
        return this.__dynamicStructureFields.getName(); 
      return DataAppAgentDefinitions.auditResult.getName();
    }
    
    public static String _getCanonicalTypeName() {
      return _getClassType().getName();
    }
    
    private static StructValue createEmptyStructValue() {
      return new StructValue(_getCanonicalTypeName());
    }
    
    public static AuditResult _newInstance(StructValue structValue) {
      return new AuditResult(structValue);
    }
    
    public static AuditResult _newInstance2(StructValue structValue) {
      return new AuditResult(structValue);
    }
    
    public static final class Builder {
      private DataAppAgentTypes.AuditStatus status;
      
      private String result;
      
      public Builder(DataAppAgentTypes.AuditStatus status, String result) {
        this.status = status;
        this.result = result;
      }
      
      public DataAppAgentTypes.AuditResult build() {
        DataAppAgentTypes.AuditResult result = new DataAppAgentTypes.AuditResult();
        result.setStatus(this.status);
        result.setResult(this.result);
        return result;
      }
    }
  }
}

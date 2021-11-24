package com.vmware.cis.data.internal.adapters.pc;

import com.vmware.cis.data.internal.provider.util.property.PropertyByName;
import com.vmware.vim.binding.vim.cluster.ConfigInfoEx;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FormulaRegistry {
  private static final String TYPE_VIRTUAL_MACHINE = "VirtualMachine";
  
  private static final String TYPE_FOLDER = "Folder";
  
  private static final String TYPE_HOST = "HostSystem";
  
  private static final String TYPE_RESOURCE_POOL = "ResourcePool";
  
  private static final String TYPE_DATASTORE = "Datastore";
  
  private static final String TYPE_DATACENTER = "Datacenter";
  
  private static final String TYPE_COMPUTE_RESOURCE = "ComputeResource";
  
  private static final String TYPE_NETWORK = "Network";
  
  private static final String TYPE_CLUSTER_COMPUTE_RESOURCE = "ClusterComputeResource";
  
  private static final String PROPERTY_IS_NORMAL_VM_OR_PRIMARY = "isNormalVMOrPrimaryFTVM";
  
  private static final String PROPERTY_IS_ROOT_FOLDER = "isRootFolder";
  
  private static final String PROPERTY_IS_DATASTORE_FOLDER = "isDatastoreFolder";
  
  private static final String PROPERTY_IS_DATACENTER_FOLDER = "isDatacenterFolder";
  
  private static final String PROPERTY_IS_COMPUTERESOURCE_FOLDER = "isComputeResourceFolder";
  
  private static final String PROPERTY_IS_NETWORK_FOLDER = "isNetworkFolder";
  
  private static final String PROPERTY_IS_STANDALONE = "isStandalone";
  
  private static final String PROPERTY_IS_ROOT_RP = "isRootRP";
  
  private static final String PROPERTY_IS_VM_FOLDER = "isVirtualMachineFolder";
  
  private static final String PROPERTY_DAS_ENABLED = "configurationEx/dasConfig/enabled";
  
  private static final String PROPERTY_DRS_ENABLED = "configurationEx/drsConfig/enabled";
  
  private static final String PROPERTY_CONFIG = "config";
  
  private static final String PROPERTY_CONFIG_ROLE = "config/ftInfo/role";
  
  private static final String PROPERTY_CONFIG_TEMPLATE = "config/template";
  
  private static final String PROPERTY_PARENT = "parent";
  
  private static final String PROPERTY_CHILD_TYPE = "childType";
  
  private static final String PROPERTY_CONFIG_EX = "configurationEx";
  
  private static final Map<String, Map<String, Formula>> _propertiesByModel = new LinkedHashMap<>();
  
  static {
    _propertiesByModel.put("VirtualMachine", new HashMap<>());
    _propertiesByModel.put("Folder", new HashMap<>());
    _propertiesByModel.put("HostSystem", new HashMap<>());
    _propertiesByModel.put("ResourcePool", new HashMap<>());
    _propertiesByModel.put("ClusterComputeResource", new HashMap<>());
    addVmFormulas();
    addFolderIsRoot();
    addFolderType();
    addHostIsStandalone();
    addResourcePoolIsRootRP();
    addClusterComputeResourceDasEnabled();
    addClusterComputeResourceDrsEnabled();
  }
  
  public static Formula getComputedProperty(String model, String property) {
    Map<String, Formula> formulasByProperty = _propertiesByModel.get(model);
    if (formulasByProperty == null)
      return null; 
    return formulasByProperty.get(property);
  }
  
  private static void addVmFormulas() {
    Map<String, Formula> formulasByProperty = _propertiesByModel.get("VirtualMachine");
    final Collection<String> requiredProperties = Arrays.asList(new String[] { "config", "config/ftInfo/role", "config/template" });
    Formula formula = new Formula() {
        public Object computeValue(PropertyByName item) {
          Object config = item.getValue("config");
          if (config == null)
            return Boolean.valueOf(true); 
          Object role = item.getValue("config/ftInfo/role");
          if (role != null) {
            if (!(role instanceof Integer))
              return null; 
            if (((Integer)role).intValue() > 1)
              return Boolean.valueOf(false); 
          } 
          Object template = item.getValue("config/template");
          if (template == null)
            return null; 
          if (!(template instanceof Boolean))
            return null; 
          return Boolean.valueOf(!((Boolean)template).booleanValue());
        }
        
        public Collection<String> getRequiredProperties() {
          return requiredProperties;
        }
      };
    formulasByProperty.put("isNormalVMOrPrimaryFTVM", formula);
  }
  
  private static void addFolderIsRoot() {
    Map<String, Formula> formulasByProperty = _propertiesByModel.get("Folder");
    final Collection<String> requiredProperties = Arrays.asList(new String[] { "parent" });
    Formula formula = new Formula() {
        public Object computeValue(PropertyByName item) {
          Object config = item.getValue("parent");
          return Boolean.valueOf((config == null));
        }
        
        public Collection<String> getRequiredProperties() {
          return requiredProperties;
        }
      };
    formulasByProperty.put("isRootFolder", formula);
  }
  
  private static void addFolderType() {
    Map<String, Formula> formulasByProperty = _propertiesByModel.get("Folder");
    Collection<String> requiredProperties = Arrays.asList(new String[] { "childType", "parent" });
    formulasByProperty.put("isComputeResourceFolder", new FolderTypeFormula(requiredProperties, "ComputeResource"));
    formulasByProperty.put("isDatacenterFolder", new FolderTypeFormula(requiredProperties, "Datacenter"));
    formulasByProperty.put("isDatastoreFolder", new FolderTypeFormula(requiredProperties, "Datastore"));
    formulasByProperty.put("isNetworkFolder", new FolderTypeFormula(requiredProperties, "Network"));
    formulasByProperty.put("isVirtualMachineFolder", new FolderTypeFormula(requiredProperties, "VirtualMachine"));
  }
  
  private static void addHostIsStandalone() {
    Map<String, Formula> formulasByProperty = _propertiesByModel.get("HostSystem");
    final Collection<String> requiredProperties = Arrays.asList(new String[] { "parent" });
    Formula formula = new Formula() {
        public Object computeValue(PropertyByName item) {
          Object parent = item.getValue("parent");
          if (parent == null)
            return null; 
          if (!(parent instanceof ManagedObjectReference))
            return null; 
          ManagedObjectReference parentRef = (ManagedObjectReference)parent;
          return Boolean.valueOf("ComputeResource".equals(parentRef.getType()));
        }
        
        public Collection<String> getRequiredProperties() {
          return requiredProperties;
        }
      };
    formulasByProperty.put("isStandalone", formula);
  }
  
  private static void addResourcePoolIsRootRP() {
    Map<String, Formula> formulasByProperty = _propertiesByModel.get("ResourcePool");
    final Collection<String> requiredProperties = Arrays.asList(new String[] { "parent" });
    Formula formula = new Formula() {
        public Object computeValue(PropertyByName item) {
          Object parent = item.getValue("parent");
          if (parent == null)
            return null; 
          if (!(parent instanceof ManagedObjectReference))
            return null; 
          ManagedObjectReference parentRef = (ManagedObjectReference)parent;
          return Boolean.valueOf(("ComputeResource".equals(parentRef.getType()) || "ClusterComputeResource"
              .equals(parentRef.getType())));
        }
        
        public Collection<String> getRequiredProperties() {
          return requiredProperties;
        }
      };
    formulasByProperty.put("isRootRP", formula);
  }
  
  private static void addClusterComputeResourceDasEnabled() {
    Map<String, Formula> formulasByProperty = _propertiesByModel.get("ClusterComputeResource");
    final Collection<String> requiredProperties = Arrays.asList(new String[] { "configurationEx" });
    Formula formula = new Formula() {
        public Object computeValue(PropertyByName item) {
          Object config = item.getValue("configurationEx");
          if (config == null)
            return null; 
          ConfigInfoEx configInfo = (ConfigInfoEx)config;
          if (configInfo.getDasConfig() == null)
            return null; 
          return configInfo.getDasConfig().getEnabled();
        }
        
        public Collection<String> getRequiredProperties() {
          return requiredProperties;
        }
      };
    formulasByProperty.put("configurationEx/dasConfig/enabled", formula);
  }
  
  private static void addClusterComputeResourceDrsEnabled() {
    Map<String, Formula> formulasByProperty = _propertiesByModel.get("ClusterComputeResource");
    final Collection<String> requiredProperties = Arrays.asList(new String[] { "configurationEx" });
    Formula formula = new Formula() {
        public Object computeValue(PropertyByName item) {
          Object config = item.getValue("configurationEx");
          if (config == null)
            return null; 
          ConfigInfoEx configInfo = (ConfigInfoEx)config;
          if (configInfo.getDrsConfig() == null)
            return null; 
          return configInfo.getDrsConfig().getEnabled();
        }
        
        public Collection<String> getRequiredProperties() {
          return requiredProperties;
        }
      };
    formulasByProperty.put("configurationEx/drsConfig/enabled", formula);
  }
  
  static final class FolderTypeFormula implements Formula {
    private final String _type;
    
    private final Collection<String> _requiredProperties;
    
    FolderTypeFormula(Collection<String> requiredProperties, String type) {
      this._requiredProperties = requiredProperties;
      this._type = type;
    }
    
    public Object computeValue(PropertyByName item) {
      if (item.getValue("parent") == null)
        return Boolean.valueOf(false); 
      Object childType = item.getValue("childType");
      if (childType == null)
        return null; 
      if (!(childType instanceof String[]))
        return null; 
      String[] childTypeArray = (String[])childType;
      for (int i = 0; i < childTypeArray.length; i++) {
        if (this._type.equals(childTypeArray[i]))
          return Boolean.valueOf(true); 
      } 
      return Boolean.valueOf(false);
    }
    
    public Collection<String> getRequiredProperties() {
      return this._requiredProperties;
    }
  }
  
  static interface Formula {
    Object computeValue(PropertyByName param1PropertyByName);
    
    Collection<String> getRequiredProperties();
  }
}

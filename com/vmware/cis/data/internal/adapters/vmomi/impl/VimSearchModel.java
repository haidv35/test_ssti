package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.internal.provider.ext.search.SearchChildModelDescriptor;
import com.vmware.cis.data.internal.provider.ext.search.SearchModelDescriptor;
import java.util.Arrays;

final class VimSearchModel {
  static final String VIM_SEARCH = "VimSearch";
  
  private static final String CCR = "ClusterComputeResource";
  
  private static final String DC = "Datacenter";
  
  private static final String DS = "Datastore";
  
  private static final String DVP = "DistributedVirtualPortgroup";
  
  private static final String DVS = "DistributedVirtualSwitch";
  
  private static final String NON_SYS_FOLDER = "NonSystemFolder";
  
  private static final String HOST = "HostSystem";
  
  private static final String NW = "Network";
  
  private static final String ONW = "OpaqueNetwork";
  
  private static final String NON_ROOT_RP = "NonRootResourcePool";
  
  private static final String SPOD = "StoragePod";
  
  private static final String VAPP = "VirtualApp";
  
  private static final String VM = "VirtualMachine";
  
  private static final String VDVS = "VmwareDistributedVirtualSwitch";
  
  private static final String CONFIG_MANAGED_BY = "config/managedBy";
  
  private static final String CONFIG_FTINFO_PRIMARY_VM = "config/ftInfo/primaryVM";
  
  private static final String CONFIG_TEMPLATE = "config/template";
  
  private static final String GUEST_IP_ADDRESS = "guest/ipAddress";
  
  private static final String NAME = "name";
  
  static final String VIM_SEARCH_TERM = "VimSearch/searchTerm";
  
  static final String VIM_SEARCH_NAME = "VimSearch/name";
  
  static SearchModelDescriptor createVimSearchModel() {
    return new SearchModelDescriptor("VimSearch", 
        
        Arrays.asList(new SearchChildModelDescriptor[] { 
            SearchChildModelDescriptor.childModel("ClusterComputeResource", "name"), 
            SearchChildModelDescriptor.childModel("Datacenter", "name"), 
            SearchChildModelDescriptor.childModel("Datastore", "name"), 
            SearchChildModelDescriptor.childModel("DistributedVirtualPortgroup", "name"), 
            SearchChildModelDescriptor.childModel("DistributedVirtualSwitch", "name"), 
            SearchChildModelDescriptor.childModel("NonSystemFolder", "name"), 
            SearchChildModelDescriptor.childModel("HostSystem", "name"), 
            SearchChildModelDescriptor.childModel("Network", "name"), 
            SearchChildModelDescriptor.childModel("OpaqueNetwork", "name"), 
            SearchChildModelDescriptor.childModel("NonRootResourcePool", "name"), 
            SearchChildModelDescriptor.childModel("StoragePod", "name"), 
            SearchChildModelDescriptor.childModel("VirtualApp", "name"), 
            SearchChildModelDescriptor.childModel("VmwareDistributedVirtualSwitch", "name"), 
            SearchChildModelDescriptor.childModel("VirtualMachine")
            .matchIgnoreCase("name")
            .matchIgnoreCase("guest/ipAddress")
            .selectable("name")
            .selectable("config/managedBy")
            .selectable("config/template")
            .selectable("config/ftInfo/primaryVM")
            .build() }));
  }
}

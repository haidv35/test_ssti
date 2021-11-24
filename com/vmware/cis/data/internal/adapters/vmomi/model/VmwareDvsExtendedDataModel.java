package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;

@QueryModel("VmwareDistributedVirtualSwitch")
public class VmwareDvsExtendedDataModel {
  @Property("VmwareDistributedVirtualSwitch/config/uplinkPortPolicy/uplinkPortName")
  public String[] dvsUplinkPortNames;
  
  @Property("VmwareDistributedVirtualSwitch/summary/hostMember")
  public ManagedObjectReference[] host;
  
  @Property("VmwareDistributedVirtualSwitch/summary/vm")
  public ManagedObjectReference[] vm;
  
  @Property("VmwareDistributedVirtualSwitch/parent")
  public ManagedObjectReference folder;
  
  @Property("VmwareDistributedVirtualSwitch/config/productInfo/version")
  public String dvsVersion;
  
  @Relationship({"VmwareDistributedVirtualSwitch/summary/hostMember", "HostSystem/parent"})
  @Property("ClusterComputeResource/@modelKey")
  public ManagedObjectReference[] cluster;
  
  @Relationship({"VmwareDistributedVirtualSwitch/config/uplinkPortgroup"})
  @Property("DistributedVirtualPortgroup/@modelKey")
  public ManagedObjectReference[] uplinkPortgroup;
  
  @Relationship({"VmwareDistributedVirtualSwitch/parent", "Folder/parent"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference grandparentDc;
}

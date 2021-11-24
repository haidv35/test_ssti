package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;

@QueryModel("DistributedVirtualSwitch")
public class DvsExtendedDataModel {
  @Property("DistributedVirtualSwitch/config/uplinkPortPolicy/uplinkPortName")
  public String[] dvsUplinkPortNames;
  
  @Property("DistributedVirtualSwitch/summary/hostMember")
  public ManagedObjectReference[] host;
  
  @Property("DistributedVirtualSwitch/summary/vm")
  public ManagedObjectReference[] vm;
  
  @Property("DistributedVirtualSwitch/parent")
  public ManagedObjectReference folder;
  
  @Property("DistributedVirtualSwitch/config/productInfo/version")
  public String dvsVersion;
  
  @Relationship({"DistributedVirtualSwitch/config/uplinkPortgroup"})
  @Property("DistributedVirtualPortgroup/@modelKey")
  public ManagedObjectReference[] uplinkPortgroup;
  
  @Relationship({"DistributedVirtualSwitch/summary/hostMember", "HostSystem/parent"})
  @Property("ClusterComputeResource/@modelKey")
  public ManagedObjectReference[] cluster;
  
  @Relationship({"DistributedVirtualSwitch/parent", "Folder/parent"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference grandparentDc;
}

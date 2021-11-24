package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;

@QueryModel("Network")
public class NetworkExtendedDataModel {
  @Property("Network/parent")
  public ManagedObjectReference folder;
  
  @Relationship({"Network/vm", "VirtualMachine/parentVApp"})
  @Property("VirtualApp/@modelKey")
  public ManagedObjectReference[] vapp;
  
  @Relationship({"Network/host", "HostSystem/parent"})
  @Property("ClusterComputeResource/@modelKey")
  public ManagedObjectReference[] cluster;
  
  @Relationship({"Network/parent", "Folder/parent"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference grandparentDc;
}

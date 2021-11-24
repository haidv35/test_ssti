package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;

@QueryModel("OpaqueNetwork")
public class OpaqueNetworkExtendedDataModel {
  @Property("OpaqueNetwork/parent")
  public ManagedObjectReference folder;
  
  @Relationship({"OpaqueNetwork/vm", "VirtualMachine/parentVApp"})
  @Property("VirtualApp/@modelKey")
  public ManagedObjectReference[] vapp;
  
  @Relationship({"OpaqueNetwork/host", "HostSystem/parent"})
  @Property("ClusterComputeResource/@modelKey")
  public ManagedObjectReference[] cluster;
  
  @Relationship({"OpaqueNetwork/parent", "Folder/parent"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference grandparentDc;
}

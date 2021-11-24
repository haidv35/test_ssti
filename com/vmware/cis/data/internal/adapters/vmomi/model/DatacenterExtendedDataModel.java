package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;

@QueryModel("Datacenter")
public class DatacenterExtendedDataModel {
  @Property("Datacenter/network")
  public ManagedObjectReference[] allNetwork;
  
  @Relationship({"Datacenter/datastoreFolder", "StoragePod/parent~"})
  @Property("StoragePod/@modelKey")
  public ManagedObjectReference[] childrenDsClusters;
  
  @Relationship({"HostNetwork/dc~"})
  @Property("HostNetwork/@modelKey")
  public ManagedObjectReference[] hostnetwork;
  
  @Relationship({"Network/dc~"})
  @Property("Network/@modelKey")
  public ManagedObjectReference[] standardnetwork;
  
  @Relationship({"OpaqueNetwork/dc~"})
  @Property("OpaqueNetwork/@modelKey")
  public ManagedObjectReference[] opaquenetwork;
  
  @Relationship({"DistributedVirtualPortgroup/dc~"})
  @Property("DistributedVirtualPortgroup/@modelKey")
  public ManagedObjectReference[] dvpg;
  
  @Relationship({"ScheduledTask/info/taskObject~"})
  @Property("ScheduledTask/@modelKey")
  public ManagedObjectReference[] scheduledTask;
}

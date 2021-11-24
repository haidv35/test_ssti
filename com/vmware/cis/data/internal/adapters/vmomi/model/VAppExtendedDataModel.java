package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;

@QueryModel("VirtualApp")
public class VAppExtendedDataModel {
  @Property("VirtualApp/summary/vAppState")
  public String powerState;
  
  @Property("VirtualApp/summary/suspended")
  public Boolean isSuspended;
  
  @Property("VirtualApp/network")
  public String allNetwork;
  
  @Relationship({"VirtualApp/owner", "HostSystem/parentComputeResource~"})
  @Property("HostSystem/@modelKey")
  public ManagedObjectReference host;
  
  @Relationship({"VirtualApp/owner", "HostSystem/parent~"})
  @Property("HostSystem/@modelKey")
  public ManagedObjectReference[] allHosts;
  
  @Relationship({"VirtualApp/network"})
  @Property("DistributedVirtualPortgroup/@modelKey")
  public ManagedObjectReference[] dvPortGroup;
  
  @Relationship({"VirtualApp/parent"})
  @Property("Folder/@modelKey")
  public ManagedObjectReference parentfolder;
  
  @Relationship({"VirtualApp/parent"})
  @Property("ResourcePool/@modelKey")
  public ManagedObjectReference parentRp;
  
  @Relationship({"ResourcePool/parent~"})
  @Property("ResourcePool/@modelKey")
  public ManagedObjectReference[] rps;
  
  @Relationship({"VirtualApp/parent~"})
  @Property("VirtualApp/@modelKey")
  public ManagedObjectReference[] vApp;
  
  @Relationship({"VirtualApp/network"})
  @Property("HostNetwork/@modelKey")
  public ManagedObjectReference[] hostNetwork;
  
  @Relationship({"ScheduledTask/info/taskObject~"})
  @Property("ScheduledTask/@modelKey")
  public ManagedObjectReference[] scheduledTask;
  
  @Relationship({"VirtualApp/computeResource", "AnyComputeResource/datastore"})
  @Property("Datastore/@modelKey")
  public ManagedObjectReference[] computeResourceDatastore;
  
  @Relationship({"VirtualApp/parentFolder", "Folder/parent"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference grandparentDc;
}

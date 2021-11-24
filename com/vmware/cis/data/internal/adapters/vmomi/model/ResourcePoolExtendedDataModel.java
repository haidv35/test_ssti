package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.model.DerivedProperty;
import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.cis.data.model.SourceProperty;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;

@QueryModel("ResourcePool")
public class ResourcePoolExtendedDataModel {
  @Relationship({"ResourcePool/parent"})
  @Property("ResourcePool/@modelKey")
  public ManagedObjectReference parentRp;
  
  @Relationship({"ResourcePool/parent~"})
  @Property("ResourcePool/@modelKey")
  public ManagedObjectReference[] rps;
  
  @Relationship({"VirtualApp/parent~"})
  @Property("VirtualApp/@modelKey")
  public ManagedObjectReference[] vApp;
  
  @Relationship({"ResourcePool/owner", "HostSystem/parentComputeResource~"})
  @Property("HostSystem/@modelKey")
  public ManagedObjectReference host;
  
  @Relationship({"ResourcePool/parent", "HostSystem/parentComputeResource~"})
  @Property("HostSystem/@modelKey")
  public ManagedObjectReference parentHost;
  
  @Relationship({"ResourcePool/owner", "HostSystem/parent~"})
  @Property("HostSystem/@modelKey")
  public ManagedObjectReference[] allHosts;
  
  @Relationship({"ScheduledTask/info/taskObject~"})
  @Property("ScheduledTask/@modelKey")
  public ManagedObjectReference[] scheduledTask;
  
  @Relationship({"ResourcePool/computeResource", "AnyComputeResource/datastore"})
  @Property("Datastore/@modelKey")
  public ManagedObjectReference[] computeResourceDatastore;
  
  @Relationship({"ResourcePool/parent", "ResourcePool/parent"})
  @Property("ClusterComputeResource/@modelKey")
  public ManagedObjectReference grandparentCluster;
  
  @Relationship({"ResourcePool/parent", "ResourcePool/parent"})
  @Property("ComputeResource/host")
  public ManagedObjectReference standaloneHostOfUserRp;
  
  @DerivedProperty("ResourcePool/rpsLength")
  public static int getRpsLength(@SourceProperty("ResourcePool/rps") ManagedObjectReference[] rps) {
    if (rps != null)
      return rps.length; 
    return 0;
  }
  
  @DerivedProperty("ResourcePool/vAppLength")
  public static int getVAppLength(@SourceProperty("ResourcePool/vApp") ManagedObjectReference[] vapp) {
    if (vapp != null)
      return vapp.length; 
    return 0;
  }
}

package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.model.DerivedProperty;
import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.cis.data.model.SourceProperty;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;

@QueryModel("StoragePod")
public class DsClusterExtendedDataModel {
  @Property("StoragePod/summary/freeSpace")
  public Long free;
  
  @Property("StoragePod/summary/capacity")
  public Long capacity;
  
  @Property("StoragePod/childEntity")
  public ManagedObjectReference[] datastore;
  
  @Relationship({"StoragePod/childEntity", "Datastore/vm"})
  @Property("VirtualMachine/@modelKey")
  public ManagedObjectReference[] vm;
  
  @Relationship({"StoragePod/parent"})
  @Property("Folder/@modelKey")
  public ManagedObjectReference folder;
  
  @Relationship({"Datastore/parentStoragePod~", "Datastore/hostKey", "HostSystem/parent"})
  @Property("ClusterComputeResource/@modelKey")
  public ManagedObjectReference[] cluster;
  
  @Relationship({"Datastore/parentStoragePod~", "Datastore/hostKey", "HostSystem/rps"})
  @Property("ResourcePool/@modelKey")
  public ManagedObjectReference[] resourcePool;
  
  @Relationship({"Datastore/parentStoragePod~", "Datastore/hostKey", "HostSystem/vapp"})
  @Property("VirtualApp/@modelKey")
  public ManagedObjectReference[] vapp;
  
  @Relationship({"Datastore/parentStoragePod~", "Datastore/hostKey"})
  @Property("HostSystem/@modelKey")
  public ManagedObjectReference[] host;
  
  @Relationship({"Datastore/parentStoragePod~", "Datastore/hostKey"})
  @Property("HostSystem/@modelKey")
  public ManagedObjectReference[] hostKey;
  
  @Relationship({"StoragePod/parent", "Folder/parent"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference grandparentDc;
  
  @DerivedProperty("StoragePod/vmLength")
  public static int getVmLength(@SourceProperty("StoragePod/vm") ManagedObjectReference[] vm) {
    if (vm != null)
      return vm.length; 
    return 0;
  }
}

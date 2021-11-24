package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.model.DerivedProperty;
import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.cis.data.model.SourceProperty;
import com.vmware.vim.binding.vim.cluster.VmOrchestrationInfo;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.Calendar;

@QueryModel("ClusterComputeResource")
public class ClusterExtendedDataModel {
  @Property("ClusterComputeResource/configurationEx/dasConfig/enabled")
  public Boolean haEnabled;
  
  @Property("ClusterComputeResource/configurationEx/drsConfig/enabled")
  public Boolean drsEnabled;
  
  @Property("ClusterComputeResource/configurationEx/vsanConfigInfo/enabled")
  public Boolean vsanEnabled;
  
  @Property("ClusterComputeResource/network")
  public ManagedObjectReference[] allNetwork;
  
  @Relationship({"ClusterComputeResource/network"})
  @Property("Network/@modelKey")
  public ManagedObjectReference[] standardnetwork;
  
  @Relationship({"ClusterComputeResource/network"})
  @Property("OpaqueNetwork/@modelKey")
  public ManagedObjectReference[] opaquenetwork;
  
  @Relationship({"ClusterComputeResource/network"})
  @Property("DistributedVirtualPortgroup/@modelKey")
  public ManagedObjectReference[] pg;
  
  @Relationship({"ClusterComputeResource/parent"})
  @Property("Folder/@modelKey")
  public ManagedObjectReference folder;
  
  @Relationship({"ClusterComputeResource/datastore", "Datastore/parent"})
  @Property("StoragePod/@modelKey")
  public ManagedObjectReference[] storagePod;
  
  @Relationship({"ScheduledTask/info/taskObject~"})
  @Property("ScheduledTask/@modelKey")
  public ManagedObjectReference[] scheduledTask;
  
  @Relationship({"ClusterComputeResource/resourcePool"})
  @Property("ResourcePool/rpcontents")
  public ManagedObjectReference[] rootRpContents;
  
  @Relationship({"ClusterComputeResource/hp"})
  @Property("HostProfile/name")
  public String associatedProfileName;
  
  @Relationship({"ClusterComputeResource/hp"})
  @Property("HostProfile/createdTime")
  public Calendar associatedProfileCreatedTime;
  
  @Relationship({"ClusterComputeResource/hp"})
  @Property("HostProfile/modifiedTime")
  public Calendar associatedProfileModifiedTime;
  
  @Relationship({"ClusterComputeResource/parent", "Folder/parent"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference grandparentDc;
  
  @DerivedProperty("ClusterComputeResource/vmOrchestrationVms")
  public static ManagedObjectReference[] getVmOrchestrationVms(@SourceProperty("ClusterComputeResource/configurationEx/vmOrchestration") VmOrchestrationInfo[] vmOrchestrationInfos) {
    if (vmOrchestrationInfos == null)
      return null; 
    ManagedObjectReference[] vms = new ManagedObjectReference[vmOrchestrationInfos.length];
    for (int i = 0; i < vmOrchestrationInfos.length; i++) {
      VmOrchestrationInfo vmOrchestrationInfo = vmOrchestrationInfos[i];
      vms[i] = vmOrchestrationInfo.getVm();
    } 
    return vms;
  }
}

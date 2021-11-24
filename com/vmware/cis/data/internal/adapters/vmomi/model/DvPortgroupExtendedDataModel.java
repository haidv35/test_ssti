package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;

@QueryModel("DistributedVirtualPortgroup")
public class DvPortgroupExtendedDataModel {
  @Property("DistributedVirtualPortgroup/parent")
  public ManagedObjectReference folder;
  
  @Property("DistributedVirtualPortgroup/config/type")
  public String portBindingType;
  
  @Property("DistributedVirtualPortgroup/config/defaultPortConfig/vlan/pvlanId")
  public int dvpgPvlanSecondaryId;
  
  @Property("DistributedVirtualPortgroup/config/distributedVirtualSwitch")
  public ManagedObjectReference dvs;
  
  @Property("DistributedVirtualPortgroup/config/defaultPortConfig/lacpPolicy/enable/value")
  public boolean lacpV1Enabled;
  
  @Relationship({"DistributedVirtualPortgroup/host", "HostSystem/parent"})
  @Property("ClusterComputeResource/@modelKey")
  public ManagedObjectReference[] cluster;
  
  @Relationship({"DistributedVirtualPortgroup/vm", "VirtualMachine/parentVApp"})
  @Property("VirtualApp/@modelKey")
  public ManagedObjectReference[] vapp;
  
  @Relationship({"DistributedVirtualPortgroup/config/distributedVirtualSwitch"})
  @Property("AnyDistributedVirtualSwitch/config/uuid")
  public String dvsUuid;
  
  @Relationship({"DistributedVirtualPortgroup/config/distributedVirtualSwitch"})
  @Property("AnyDistributedVirtualSwitch/name")
  public String dvsName;
  
  @Relationship({"DistributedVirtualPortgroup/config/distributedVirtualSwitch"})
  @Property("AnyDistributedVirtualSwitch/capability/dvPortGroupOperationSupported")
  public boolean dvPortGroupOperationSupported;
  
  @Relationship({"DistributedVirtualPortgroup/config/distributedVirtualSwitch"})
  @Property("AnyDistributedVirtualSwitch/capability/featuresSupported/backupRestoreCapability/backupRestoreSupported")
  public boolean dvPortGroupBackupRestoreSupported;
  
  @Relationship({"DistributedVirtualPortgroup/parent", "Folder/parent"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference grandparentDc;
}

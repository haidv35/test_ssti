package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.model.DerivedProperty;
import com.vmware.cis.data.model.PredicateProperty;
import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.cis.data.model.SourceProperty;
import com.vmware.vim.binding.vim.encryption.CryptoKeyId;
import com.vmware.vim.binding.vim.vm.RuntimeInfo;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;

@QueryModel("VirtualMachine")
public class VmExtendedDataModel {
  @Property("VirtualMachine/runtime/consolidationNeeded")
  public Boolean consolidationNeeded;
  
  @Property("VirtualMachine/config/version")
  public String hwVersion;
  
  @Property("VirtualMachine/guest/toolsVersionStatus2")
  public String toolsVersionStatus;
  
  @Property("VirtualMachine/config/template")
  public Boolean template;
  
  @Property("VirtualMachine/config/ftInfo/role")
  public String ftRole;
  
  @Property("VirtualMachine/runtime/powerState")
  public String powerState;
  
  @Property("VirtualMachine/runtime/faultToleranceState")
  public String ftState;
  
  @Property("VirtualMachine/runtime/question")
  public String question;
  
  @Property("VirtualMachine/config/annotation")
  public String annotation;
  
  @Property("VirtualMachine/config/guestFullName")
  public String guestOS;
  
  @Property("VirtualMachine/guest/ipAddress")
  public String ip;
  
  @Property("VirtualMachine/runtime/host")
  public String host;
  
  @Property("VirtualMachine/parentVApp")
  public String vapp;
  
  @Property("VirtualMachine/network")
  public String allNetwork;
  
  @Property("VirtualMachine/parent")
  public String parentFolder;
  
  @Property("VirtualMachine/summary/storage/committed")
  public Long usedSpace;
  
  @Property("VirtualMachine/config/files/vmPathName")
  public String vmPathName;
  
  @Property("VirtualMachine/runtime/connectionState")
  public String connectionState;
  
  @Property("VirtualMachine/config/ftInfo/primaryVM")
  public String primaryVM;
  
  @Property("VirtualMachine/config/ftInfo/secondaries")
  public String secondaries;
  
  @Property("VirtualMachine/summary/quickStats/overallCpuUsage")
  public Long hostCpuUsage;
  
  @Property("VirtualMachine/summary/quickStats/hostMemoryUsage")
  public Long hostMemoryUsage;
  
  @Relationship({"VirtualMachine/runtime/host"})
  @Property("HostSystem/name")
  public String hostName;
  
  @Relationship({"VirtualMachine/datastore"})
  @Property("Datastore/name")
  public String[] datastoreNames;
  
  @Relationship({"VirtualMachine/runtime/host", "HostSystem/parent"})
  @Property("ClusterComputeResource/name")
  public String vmClusterName;
  
  @Relationship({"VirtualMachine/network"})
  @Property("DistributedVirtualPortgroup/@modelKey")
  public ManagedObjectReference[] dvPortGroup;
  
  @Relationship({"VirtualMachine/network"})
  @Property("Network/@modelKey")
  public ManagedObjectReference[] standardnetwork;
  
  @Relationship({"VirtualMachine/datastore", "Datastore/parent"})
  @Property("StoragePod/@modelKey")
  public ManagedObjectReference[] storagePod;
  
  @Relationship({"VirtualMachine/network"})
  @Property("OpaqueNetwork/@modelKey")
  public ManagedObjectReference[] opaquenetwork;
  
  @Relationship({"ScheduledTask/info/taskObject~"})
  @Property("ScheduledTask/@modelKey")
  public ManagedObjectReference[] scheduledTask;
  
  @Relationship({"VirtualMachine/network"})
  @Property("HostNetwork/@modelKey")
  public ManagedObjectReference[] hostNetwork;
  
  @Relationship({"VirtualMachine/parent", "Folder/parent"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference grandparentDc;
  
  @Relationship({"VirtualMachine/resourcePool", "ResourcePool/parent"})
  @Property("ClusterComputeResource/@modelKey")
  public ManagedObjectReference rpParentCluster;
  
  @PredicateProperty("VirtualMachine/hasSnapshot")
  public static PropertyPredicate getHasSnapshot() {
    return new PropertyPredicate("VirtualMachine/snapshot", PropertyPredicate.ComparisonOperator.UNSET, 
        Boolean.valueOf(false));
  }
  
  @DerivedProperty("VirtualMachine/haProtected")
  public static String getHaProtected(@SourceProperty("VirtualMachine/runtime/dasVmProtection") RuntimeInfo.DasProtectionState dasProtectionState) {
    if (dasProtectionState == null)
      return "na"; 
    return String.valueOf(dasProtectionState.dasProtected);
  }
  
  @DerivedProperty("VirtualMachine/haProtectedLabel")
  public static String getHaProtectedLabel(@SourceProperty("VirtualMachine/runtime/dasVmProtection") RuntimeInfo.DasProtectionState dasProtectionState) {
    return getHaProtected(dasProtectionState);
  }
  
  @DerivedProperty("VirtualMachine/isEncryptedVm")
  public static Boolean getIsEncryptedVm(@SourceProperty("VirtualMachine/config/keyId") CryptoKeyId keyId) {
    return Boolean.valueOf((keyId != null));
  }
  
  @DerivedProperty("VirtualMachine/guestMemoryPercentage")
  public static long getGuestMemoryPercentage(@SourceProperty("VirtualMachine/summary/config/memorySizeMB") Integer memorySizeMB, @SourceProperty("VirtualMachine/summary/quickStats/guestMemoryUsage") Integer guestMemoryUsage) {
    if (isNullOrZero(memorySizeMB) || isNullOrZero(guestMemoryUsage))
      return 0L; 
    return Math.round((guestMemoryUsage.intValue() * 100 / memorySizeMB.intValue()));
  }
  
  private static boolean isNullOrZero(Integer i) {
    return (i == null || i.intValue() == 0);
  }
}

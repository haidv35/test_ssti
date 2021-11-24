package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.model.DerivedProperty;
import com.vmware.cis.data.model.PredicateProperty;
import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.cis.data.model.SourceProperty;
import com.vmware.vim.binding.vim.Datastore;
import com.vmware.vim.binding.vim.host.VmfsDatastoreInfo;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;

@QueryModel("Datastore")
public class DatastoreExtendedDataModel {
  @Property("Datastore/capability/perFileThinProvisioningSupported")
  public Boolean thinProvisioningSupported;
  
  @Property("Datastore/host")
  public Datastore.HostMount hostMount;
  
  @Property("Datastore/summary/url")
  public String url;
  
  @Property("Datastore/parentStoragePod")
  public ManagedObjectReference parentPod;
  
  @Relationship({"Datastore/parent"})
  @Property("StoragePod/name")
  public String dsClusterName;
  
  @Relationship({"Datastore/parent"})
  @Property("Folder/@modelKey")
  public ManagedObjectReference folder;
  
  @Relationship({"Datastore/hostKey", "HostSystem/parent"})
  @Property("ClusterComputeResource/@modelKey")
  public ManagedObjectReference[] cluster;
  
  @Relationship({"Datastore/hostKey", "HostSystem/parent"})
  @Property("ComputeResource/@modelKey")
  public ManagedObjectReference[] computeResource;
  
  @Relationship({"Datastore/hostKey", "HostSystem/rps"})
  @Property("ResourcePool/@modelKey")
  public ManagedObjectReference[] resourcePool;
  
  @Relationship({"Datastore/hostKey", "HostSystem/vapp"})
  @Property("VirtualApp/@modelKey")
  public ManagedObjectReference[] hostVapp;
  
  @Relationship({"Datastore/parent", "Folder/parent"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference grandparentDc;
  
  @PredicateProperty("Datastore/belongsToDsCluster")
  public static PropertyPredicate getBelongsToDsCluster() {
    return new PropertyPredicate("Datastore/parentType", PropertyPredicate.ComparisonOperator.EQUAL, "StoragePod");
  }
  
  @PredicateProperty("Datastore/isSystemDatastore")
  public static PropertyPredicate getIsSystemDatastore() {
    return new PropertyPredicate("Datastore/summary/type", PropertyPredicate.ComparisonOperator.EQUAL, "PMEM");
  }
  
  @DerivedProperty("Datastore/specificType")
  public static String getSpecificType(@SourceProperty("Datastore/info") Object info, @SourceProperty("Datastore/summary/type") String summaryType) {
    if (info instanceof VmfsDatastoreInfo) {
      VmfsDatastoreInfo vmfsInfo = (VmfsDatastoreInfo)info;
      if (vmfsInfo.vmfs.majorVersion > 0)
        return "VMFS " + vmfsInfo.vmfs.majorVersion; 
      return "VMFS";
    } 
    if (info instanceof com.vmware.vim.binding.vim.host.VvolDatastoreInfo)
      return "VVol"; 
    return summaryType;
  }
}

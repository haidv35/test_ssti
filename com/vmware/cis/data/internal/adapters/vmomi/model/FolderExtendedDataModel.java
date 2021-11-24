package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.model.PredicateProperty;
import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.Arrays;

@QueryModel("Folder")
public class FolderExtendedDataModel {
  @Property("Folder/uiDisplayName")
  public String name;
  
  @Property("Folder/isComputeResourceFolder")
  public String isHostFolder;
  
  @Property("Folder/isDatastoreFolder")
  public String isStorageFolder;
  
  @Property("Folder/isVirtualMachineFolder")
  public String isVmFolder;
  
  @Property("Folder/allStandardNetwork")
  public String allstandardnetwork;
  
  @Relationship({"DistributedVirtualPortgroup/parent~"})
  @Property("DistributedVirtualPortgroup/@modelKey")
  public ManagedObjectReference[] dvpg;
  
  @Relationship({"Datastore/parent~"})
  @Property("Datastore/@modelKey")
  public ManagedObjectReference[] ds;
  
  @Relationship({"ClusterComputeResource/parent~"})
  @Property("ClusterComputeResource/@modelKey")
  public ManagedObjectReference[] cluster;
  
  @Relationship({"VirtualMachine/parent~"})
  @Property("VirtualMachine/@modelKey")
  public ManagedObjectReference[] childvm;
  
  @Relationship({"VirtualApp/parentFolder~"})
  @Property("VirtualApp/@modelKey")
  public ManagedObjectReference[] childvapp;
  
  @Relationship({"Datacenter/parent~"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference[] childdc;
  
  @Relationship({"AnyDistributedVirtualSwitch/parent~"})
  @Property("AnyDistributedVirtualSwitch/@modelKey")
  public ManagedObjectReference[] dvs;
  
  @Relationship({"AnyNetwork/parent~"})
  @Property("AnyNetwork/@modelKey")
  public ManagedObjectReference[] nw;
  
  @Relationship({"ScheduledTask/info/taskObject~"})
  @Property("ScheduledTask/@modelKey")
  public ManagedObjectReference[] scheduledTask;
  
  @Relationship({"Folder/parent", "Folder/parent"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference grandparentDc;
  
  @PredicateProperty("Folder/isDatacenterOrRootFolder")
  public static Filter getIsDatacenterOrRootFolder() {
    PropertyPredicate isDatacenterFolder = new PropertyPredicate("Folder/isDatacenterFolder", PropertyPredicate.ComparisonOperator.EQUAL, Boolean.valueOf(true));
    PropertyPredicate isRootFolder = new PropertyPredicate("Folder/isRootFolder", PropertyPredicate.ComparisonOperator.EQUAL, Boolean.valueOf(true));
    return new Filter(Arrays.asList(new PropertyPredicate[] { isDatacenterFolder, isRootFolder }, ), LogicalOperator.OR);
  }
  
  @PredicateProperty("Folder/isNonrootHostFolder")
  public static Filter getIsNonrootHostFolder() {
    PropertyPredicate isNonRootFolderOfType = new PropertyPredicate("Folder/parentType", PropertyPredicate.ComparisonOperator.EQUAL, "Folder");
    PropertyPredicate isHostFolder = new PropertyPredicate("Folder/isComputeResourceFolder", PropertyPredicate.ComparisonOperator.EQUAL, Boolean.valueOf(true));
    return new Filter(Arrays.asList(new PropertyPredicate[] { isNonRootFolderOfType, isHostFolder }, ), LogicalOperator.AND);
  }
  
  @PredicateProperty("Folder/isNonrootVmFolder")
  public static Filter getIsNonrootVmFolder() {
    PropertyPredicate isNonRootFolderOfType = new PropertyPredicate("Folder/parentType", PropertyPredicate.ComparisonOperator.EQUAL, "Folder");
    PropertyPredicate isVmFolder = new PropertyPredicate("Folder/isVirtualMachineFolder", PropertyPredicate.ComparisonOperator.EQUAL, Boolean.valueOf(true));
    return new Filter(Arrays.asList(new PropertyPredicate[] { isNonRootFolderOfType, isVmFolder }, ), LogicalOperator.AND);
  }
  
  @PredicateProperty("Folder/isNonrootDatastoreFolder")
  public static Filter getIsNonrootDatastoreFolder() {
    PropertyPredicate isNonRootFolder = new PropertyPredicate("Folder/parentType", PropertyPredicate.ComparisonOperator.EQUAL, "Folder");
    PropertyPredicate isDatastoreFolder = new PropertyPredicate("Folder/isDatastoreFolder", PropertyPredicate.ComparisonOperator.EQUAL, Boolean.valueOf(true));
    return new Filter(Arrays.asList(new PropertyPredicate[] { isNonRootFolder, isDatastoreFolder }, ), LogicalOperator.AND);
  }
  
  @PredicateProperty("Folder/isNonrootNetworkFolder")
  public static Filter getIsNonrootNetworkFolder() {
    PropertyPredicate isNonRootFolder = new PropertyPredicate("Folder/parentType", PropertyPredicate.ComparisonOperator.EQUAL, "Folder");
    PropertyPredicate isNetworkFolder = new PropertyPredicate("Folder/isNetworkFolder", PropertyPredicate.ComparisonOperator.EQUAL, Boolean.valueOf(true));
    return new Filter(Arrays.asList(new PropertyPredicate[] { isNonRootFolder, isNetworkFolder }, ), LogicalOperator.AND);
  }
}

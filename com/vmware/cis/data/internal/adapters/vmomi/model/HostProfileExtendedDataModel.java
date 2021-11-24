package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;

@QueryModel("HostProfile")
public class HostProfileExtendedDataModel {
  @Property("HostProfile/config/annotation")
  public String annotation;
  
  @Relationship({"ScheduledTask/info/taskObject~"})
  @Property("ScheduledTask/@modelKey")
  public ManagedObjectReference[] scheduledTask;
  
  @Relationship({"ClusterComputeResource/hp~"})
  @Property("ClusterComputeResource/@modelKey")
  public ManagedObjectReference[] cluster;
  
  @Relationship({"HostSystem/hp~"})
  @Property("HostSystem/@modelKey")
  public ManagedObjectReference[] host;
  
  @Relationship({"HostProfile/rootFolder"})
  @Property("Folder/uiDisplayName")
  public String vCenterName;
}

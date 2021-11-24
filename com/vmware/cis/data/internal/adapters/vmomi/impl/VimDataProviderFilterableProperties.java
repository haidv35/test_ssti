package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.api.QuerySchema;
import java.util.LinkedHashMap;
import java.util.Map;

final class VimDataProviderFilterableProperties {
  static final QuerySchema SCHEMA;
  
  static {
    Map<String, QuerySchema.ModelInfo> infoByModel = new LinkedHashMap<>();
    Map<String, QuerySchema.PropertyInfo> infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("configuration/rule", QuerySchema.PropertyInfo.forFilterableByUnsetProperty());
    infoByProperty.put("configurationEx/dasConfig/enabled", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("configurationEx/drsConfig/enabled", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("configurationEx/vsanConfigInfo/enabled", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("datastore/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("datastoreCount", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("environmentBrowser", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("freeCpuCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("freeMemoryCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("freeStorageCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("grandparentDc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("host/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("hp", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("resourcePool", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("storageTotalB", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.DOUBLE));
    infoByProperty.put("summary/numCpuCores", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("totalCpuCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("totalMemoryCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("totalStorageCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("usedCpuCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("usedMemoryCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("usedStorageCapacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("vm/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    QuerySchema.ModelInfo info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("ClusterComputeResource", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("configurationEx/dasConfig/enabled", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("datastore/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("environmentBrowser", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("host/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("resourcePool", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("ComputeResource", info);
    infoByProperty = new LinkedHashMap<>();
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("CustomFieldsManager", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("datastoreFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("host/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("hostFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("networkFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("rootFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("vm/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("vmFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("Datacenter", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("browser", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("capability/perFileThinProvisioningSupported", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("driveType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("dsClusterName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("freeSpacePercent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("grandparentDc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("host/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("info/freeSpace", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("info/nas/remoteHost", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("info/nas/remotePath", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("info/url", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("overallStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentStoragePod", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("provisionedSpace", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("specificType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("summary/accessible", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("summary/capacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("summary/freeSpace", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("summary/maintenanceMode", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("summary/type", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("vStorageSupported", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("vmTemplateCount", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("Datastore", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/defaultPortConfig/lacpPolicy/enable/value", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/defaultPortConfig/lacpPolicy/mode/value", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("config/defaultPortConfig/networkResourcePoolKey/value", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("config/defaultPortConfig/vlan/pvlanId", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("config/distributedVirtualSwitch", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("config/numPorts", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("config/policy/networkResourcePoolOverrideAllowed", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/policy/uplinkTeamingOverrideAllowed", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/type", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("config/vmVnicNetworkResourcePoolKey", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("dvpgAggregatedVlan", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("dvpgIsPvlanPresent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("dvpgPvlanPrimaryId", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("dvpgPvlanType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("dvpgVlan", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("dvpgVlanTrunk", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("dvpgVlanType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("grandparentDc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("host/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("isExtendedNetwork", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("isUplinkPortgroup", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("key", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("networkResourceManagementVersion", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("networkType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("overallStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("summary/ipPoolName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("tag", QuerySchema.PropertyInfo.forFilterableByUnsetProperty());
    infoByProperty.put("hasNetworkAssignPrivilege", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("DistributedVirtualPortgroup", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("availableUplinkCount", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("capability/dvPortGroupOperationSupported", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("capability/featuresSupported/backupRestoreCapability/backupRestoreSupported", 
        
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/productInfo/version", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("config/uuid", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("grandparentDc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("networkResourceManagementVersion", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("numberOfAccessiblePortgroups", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("rootFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("totalNetworkResourcePoolReservation", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("uuid", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("DistributedVirtualSwitch", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("datastoreBrowser", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("EnvironmentBrowser", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("allNormalVMOrPrimaryFTVM/length", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("datacenterName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("host/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("grandparentDc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("isComputeResourceFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("isDatacenterFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("isDatastoreFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("isNetworkFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("isRootFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("isSystemFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("isVirtualAppFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("isVirtualMachineFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("rootFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("uiDisplayName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("Folder", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("certificateInfo/notAfter", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("certificateInfo/notBefore", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("HostCertificateManager", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("referenceHost", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("rootFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("HostProfile", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("associatedProfileName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("capability/ftSupported", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("capability/tpmSupported", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("capability/tpmVersion", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("capability/txtEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/product/fullName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("config/product/version", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("configManager/certificateManager", 
        QuerySchema.PropertyInfo.forFilterableByUnsetProperty());
    infoByProperty.put("cpuUsage", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("currentEVCModeLabel", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("dasHostState", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("datastoreBrowser", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("dcOrUserCreatedHostFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("esxFullName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("hardware/cpuInfo/numCpuPackages", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("hardware/memorySize", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("hostClusterName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("hp", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("grandgrandparentDcOfStandaloneHost", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("grandparentFolderOfStandaloneHost", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("inDomain", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("isStandalone", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("memoryUsage", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentComputeResource", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("pciPassthruSupported", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("runtime/connectionState", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("runtime/dasHostState", QuerySchema.PropertyInfo.forFilterableByUnsetProperty());
    infoByProperty.put("runtime/powerState", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("summary/hardware/numNics", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("summary/overallStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("summary/quickStats/uptime", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("summary/tpmAttestation/status", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("stateLabel", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("tpmAttestationMessage", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("HostSystem", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("host/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("grandparentDc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("isExtendedNetwork", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("networkType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("overallStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("summary/ipPoolName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("vm/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("hasNetworkAssignPrivilege", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("Network", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("host/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("grandparentDc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("isExtendedNetwork", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("networkType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("overallStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("summary/ipPoolName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("summary/opaqueNetworkId", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("summary/opaqueNetworkType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("vm/length", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("hasNetworkAssignPrivilege", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("OpaqueNetwork", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("cluster", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("computeResource", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("config/cpuAllocation/expandableReservation", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/cpuAllocation/limit", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("config/cpuAllocation/reservation", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("config/cpuAllocation/shares/level", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("config/cpuAllocation/shares/shares", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("config/memoryAllocation/expandableReservation", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/memoryAllocation/limit", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("config/memoryAllocation/reservation", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("config/memoryAllocation/shares/level", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("config/memoryAllocation/shares/shares", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("grandparentCluster", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("host", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("isNonRootRP", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("isRootRP", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("owner", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("parentVApp", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("standaloneHostOfUserRp", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("ResourcePool", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("info/taskObject", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("ScheduledTask", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("grandparentDc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("overallStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("summary/capacity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("summary/freeSpace", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("vmTemplateCount", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("StoragePod", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("cluster", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("computeResource", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("config/memoryAllocation/expandableReservation", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/memoryAllocation/limit", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("config/memoryAllocation/reservation", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("cr", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("config/memoryAllocation/shares/shares", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("config/memoryAllocation/shares/level", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("config/cpuAllocation/shares/shares", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("config/cpuAllocation/shares/level", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("grandparentDc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("host", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("owner", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("parentVApp", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("VirtualApp", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("alarmActionsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("cluster", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("config", QuerySchema.PropertyInfo.forFilterableByUnsetProperty());
    infoByProperty.put("config/annotation", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("config/flags/vbsEnabled", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/ftInfo", QuerySchema.PropertyInfo.forFilterableByUnsetProperty());
    infoByProperty.put("config/ftInfo/primaryVM", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("config/ftInfo/role", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("config/guestFullName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("config/hardware/numCPU", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("config/instanceUuid", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("config/keyId", QuerySchema.PropertyInfo.forFilterableByUnsetProperty());
    infoByProperty.put("config/managedBy", QuerySchema.PropertyInfo.forFilterableByUnsetProperty());
    infoByProperty.put("config/memoryAllocation/expandableReservation", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/memoryAllocation/limit", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("config/memoryAllocation/reservation", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("config/template", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/version", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("currentEVCModeLabel", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("detailedToolsVersionStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("environmentBrowser", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("grandparentDc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("guest/guestFullName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("guest/hostName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("guest/ipAddress", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("guest/toolsVersionStatus2", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("guestMemoryPercentage", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("hostName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("isNormalVM", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("isNormalVMOrPrimaryFTVM", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("isVMBlockedByQuestion", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("parentVApp", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("provisionedSpace", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("reservationMemoryForVM", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("resourcePool", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("rpParentCluster", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("runtime/connectionState", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("runtime/consolidationNeeded", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("runtime/faultToleranceState", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("runtime/host", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("runtime/powerState", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM));
    infoByProperty.put("snapshot", QuerySchema.PropertyInfo.forFilterableByUnsetProperty());
    infoByProperty.put("standalonehost", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("summary/config/annotation", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("summary/config/ftInfo", QuerySchema.PropertyInfo.forFilterableByUnsetProperty());
    infoByProperty.put("summary/config/memorySizeMB", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("summary/config/numCpu", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("summary/config/numEthernetCards", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("summary/config/numVmiopBackings", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("summary/config/product", QuerySchema.PropertyInfo.forFilterableByUnsetProperty());
    infoByProperty.put("summary/config/template", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("summary/config/tpmPresent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("summary/config/uuid", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("summary/overallStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("summary/quickStats/hostMemoryUsage", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("summary/quickStats/overallCpuUsage", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("summary/quickStats/uptimeSeconds", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("summary/storage/committed", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("toolsRunningStatus", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("toolsUpgradeInfo", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("userResourcePool", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("vmClusterName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("vmDatacenterName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("vmHostType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("vmQuestionId", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("VirtualMachine", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("vm", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("VirtualMachineSnapshot", info);
    infoByProperty = new LinkedHashMap<>();
    infoByProperty.put("availableUplinkCount", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("capability/dvPortGroupOperationSupported", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("capability/featuresSupported/backupRestoreCapability/backupRestoreSupported", 
        
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("config/productInfo/version", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("config/uuid", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("dc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("grandparentDc", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("lacpIpfixOverrideAllowed", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("lacpVlanOverrideAllowed", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    infoByProperty.put("name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("networkResourceManagementVersion", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("numberOfAccessiblePortgroups", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    infoByProperty.put("parent", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("parentType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProperty.put("rootFolder", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    infoByProperty.put("totalNetworkResourcePoolReservation", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG));
    infoByProperty.put("uuid", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    info = new QuerySchema.ModelInfo(infoByProperty);
    infoByModel.put("VmwareDistributedVirtualSwitch", info);
    SCHEMA = QuerySchema.forModels(infoByModel);
  }
}

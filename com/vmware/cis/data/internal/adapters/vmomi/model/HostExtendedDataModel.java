package com.vmware.cis.data.internal.adapters.vmomi.model;

import com.vmware.cis.data.model.DerivedProperty;
import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import com.vmware.cis.data.model.SourceProperty;
import com.vmware.vim.binding.vim.AboutInfo;
import com.vmware.vim.binding.vim.host.IpConfig;
import com.vmware.vim.binding.vim.host.PciPassthruInfo;
import com.vmware.vim.binding.vim.host.PhysicalNic;
import com.vmware.vim.binding.vim.host.VirtualNic;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

@QueryModel("HostSystem")
public class HostExtendedDataModel {
  @Property("HostSystem/runtime/powerState")
  public String powerState;
  
  @Property("HostSystem/runtime/inMaintenanceMode")
  public Boolean inMaintenanceMode;
  
  @Property("HostSystem/runtime/inMaintenanceMode")
  public String inMaintenanceModeString;
  
  @Property("HostSystem/runtime/connectionState")
  public String connectionState;
  
  @Property("HostSystem/network")
  public String[] allNetwork;
  
  @Property("HostSystem/config/product")
  public AboutInfo product;
  
  @Property("HostSystem/config/network/consoleVnic")
  public VirtualNic[] nics;
  
  @Property("HostSystem/configManager/storageSystem")
  public String storageSystem;
  
  @Property("HostSystem/configManager/datastoreSystem")
  public String datastoreSystem;
  
  @Property("HostSystem/configManager/cacheConfigurationManager")
  public String cacheConfigurationManager;
  
  @Property("HostSystem/configManager/diagnosticSystem")
  public String diagnosticSystem;
  
  @Property("HostSystem/configManager/firewallSystem")
  public String firewallSystem;
  
  @Property("HostSystem/hp")
  public ManagedObjectReference associatedProfile;
  
  @Relationship({"HostSystem/parent"})
  @Property("ClusterComputeResource/@modelKey")
  public ManagedObjectReference cluster;
  
  @Relationship({"HostSystem/parent"})
  @Property("ClusterComputeResource/name")
  public String hostClusterName;
  
  @Relationship({"HostSystem/parent", "ComputeResource/resourcePool"})
  @Property("ResourcePool/@modelKey")
  public ManagedObjectReference rootRp;
  
  @Relationship({"HostSystem/parent", "ComputeResource/resourcePool"})
  @Property("ResourcePool/@modelKey")
  public ManagedObjectReference rp;
  
  @Relationship({"HostSystem/network"})
  @Property("DistributedVirtualPortgroup/@modelKey")
  public ManagedObjectReference[] dvpg;
  
  @Relationship({"HostSystem/network"})
  @Property("Network/@modelKey")
  public ManagedObjectReference[] standardnetwork;
  
  @Relationship({"HostSystem/network"})
  @Property("OpaqueNetwork/@modelKey")
  public ManagedObjectReference[] opaquenetwork;
  
  @Relationship({"HostSystem/datastore", "Datastore/parent"})
  @Property("StoragePod/@modelKey")
  public ManagedObjectReference[] storagePod;
  
  @Relationship({"HostSystem/configManager/certificateManager"})
  @Property("HostCertificateManager/certificateInfo/notBefore")
  public Date certificateValidFrom;
  
  @Relationship({"HostSystem/configManager/certificateManager"})
  @Property("HostCertificateManager/certificateInfo/notAfter")
  public Date certificateValidTo;
  
  @Relationship({"HostSystem/configManager/certificateManager"})
  @Property("HostCertificateManager/certificateInfo/status")
  public String certificateStatus;
  
  @Relationship({"HostSystem/configManager/certificateManager"})
  @Property("HostCertificateManager/certificateInfo/issuer")
  public String certificateIssuer;
  
  @Relationship({"HostSystem/configManager/certificateManager"})
  @Property("HostCertificateManager/certificateInfo/subject")
  public String certificateSubject;
  
  @Relationship({"HostSystem/parent", "AnyComputeResource/resourcePool"})
  @Property("ResourcePool/@modelKey")
  public ManagedObjectReference resourcePool;
  
  @Relationship({"HostSystem/network"})
  @Property("DistributedVirtualPortgroup/config/distributedVirtualSwitch")
  public ManagedObjectReference[] dvs;
  
  @Relationship({"HostSystem/network"})
  @Property("HostNetwork/@modelKey")
  public ManagedObjectReference[] hostNetwork;
  
  @Relationship({"HostSystem/hp"})
  @Property("HostProfile/name")
  public String associatedProfileName;
  
  @Relationship({"HostSystem/hp"})
  @Property("HostProfile/createdTime")
  public Calendar associatedProfileCreatedTime;
  
  @Relationship({"HostSystem/hp"})
  @Property("HostProfile/modifiedTime")
  public Calendar associatedProfileModifiedTime;
  
  @Property("HostSystem/hostProfileComplianceStatus")
  public String hostCompliance;
  
  @Relationship({"HostSystem/parent", "ComputeResource/resourcePool"})
  @Property("ResourcePool/rpcontents")
  public ManagedObjectReference[] rootRpContents;
  
  @Relationship({"ScheduledTask/info/taskObject~"})
  @Property("ScheduledTask/@modelKey")
  public ManagedObjectReference[] scheduledTask;
  
  @Relationship({"HostSystem/configManager/pciPassthruSystem"})
  @Property("HostPciPassthruSystem/pciPassthruInfo")
  public PciPassthruInfo[] pciPassthruInfo;
  
  @Relationship({"HostSystem/parent", "ComputeResource/parent", "Folder/parent"})
  @Property("Datacenter/@modelKey")
  public ManagedObjectReference grandgrandparentDcOfStandaloneHost;
  
  @Relationship({"HostSystem/parent", "ComputeResource/parent"})
  @Property("Folder/@modelKey")
  public ManagedObjectReference grandparentFolderOfStandaloneHost;
  
  @DerivedProperty("HostSystem/ip")
  public static String[] getIp(@SourceProperty("HostSystem/config/network/vnic") VirtualNic[] vnic, @SourceProperty("HostSystem/config/network/consoleVnic") VirtualNic[] consoleVnic, @SourceProperty("HostSystem/config/network/pnic") PhysicalNic[] pnic) {
    List<String> ipAll = getVnicIpV4Addresses(vnic);
    ipAll.addAll(getVnicIpV4Addresses(consoleVnic));
    ipAll.addAll(getPnicIpV4Addresses(pnic));
    return ipAll.<String>toArray(new String[ipAll.size()]);
  }
  
  @DerivedProperty("HostSystem/ipv6")
  public static String[] getIpv6(@SourceProperty("HostSystem/config/network/vnic") VirtualNic[] vnic, @SourceProperty("HostSystem/config/network/consoleVnic") VirtualNic[] consoleVnic, @SourceProperty("HostSystem/config/network/pnic") PhysicalNic[] pnic) {
    List<String> ipAll = getVnicIpV6Addresses(vnic);
    ipAll.addAll(getVnicIpV6Addresses(consoleVnic));
    ipAll.addAll(getPnicIpV6Addresses(pnic));
    return ipAll.<String>toArray(new String[ipAll.size()]);
  }
  
  @DerivedProperty("HostSystem/encryptionStatus")
  public static String getEncryptionStatus(@SourceProperty("HostSystem/runtime/cryptoState") String cryptoState) {
    if (cryptoState == null)
      return "na"; 
    return cryptoState;
  }
  
  @DerivedProperty("HostSystem/cpuUsage")
  public static Integer getCpuUsage(@SourceProperty("HostSystem/hardware/cpuInfo/numCpuCores") Short numCpu, @SourceProperty("HostSystem/hardware/cpuInfo/hz") Long cpuHz, @SourceProperty("HostSystem/summary/quickStats/overallCpuUsage") Integer overallCpuUsage) {
    if (isNullOrZero(numCpu) || isNullOrZero(cpuHz) || isNullOrZero(overallCpuUsage))
      return Integer.valueOf(0); 
    BigDecimal numerator = new BigDecimal(overallCpuUsage.intValue() * 100);
    BigDecimal denominator = new BigDecimal(numCpu.shortValue() * cpuHz.longValue() / 1000000L);
    return Integer.valueOf(numerator.divideToIntegralValue(denominator).intValue());
  }
  
  @DerivedProperty("HostSystem/memoryUsage")
  public static Integer getMemoryUsage(@SourceProperty("HostSystem/summary/quickStats/overallMemoryUsage") Integer overallMemoryUsage, @SourceProperty("HostSystem/hardware/memorySize") Long memorySize) {
    if (isNullOrZero(overallMemoryUsage) || isNullOrZero(memorySize))
      return Integer.valueOf(0); 
    BigDecimal numerator = new BigDecimal(overallMemoryUsage.intValue() * 100);
    BigDecimal denominator = new BigDecimal(memorySize.longValue() / 1048576L);
    return Integer.valueOf(numerator.divideToIntegralValue(denominator).intValue());
  }
  
  @DerivedProperty("HostSystem/pciPassthruSupported")
  public static boolean getPciPassthruSupported(@SourceProperty("HostSystem/pciPassthruInfo") PciPassthruInfo[] hostPciPassthruInfo) {
    if (hostPciPassthruInfo == null || hostPciPassthruInfo.length == 0)
      return false; 
    for (PciPassthruInfo pciPassthruInfo : hostPciPassthruInfo) {
      if (pciPassthruInfo.isPassthruCapable())
        return true; 
    } 
    return false;
  }
  
  @DerivedProperty("HostSystem/vsanFaultDomainName")
  public static String getVsanFaultDomain(@SourceProperty("HostSystem/config/vsanHostConfig/enabled") Boolean vsanHostConfigEnabled, @SourceProperty("HostSystem/config/vsanHostConfig/faultDomainInfo/name") String faultDomainName) {
    if (vsanHostConfigEnabled == null || !vsanHostConfigEnabled.booleanValue())
      return null; 
    return faultDomainName;
  }
  
  private static boolean isNullOrZero(Number i) {
    return (i == null || i.longValue() == 0L);
  }
  
  private static List<String> getVnicIpV4Addresses(VirtualNic[] vnic) {
    if (ArrayUtils.isEmpty((Object[])vnic))
      return new ArrayList<>(0); 
    List<String> ipAll = new ArrayList<>();
    for (int i = 0; i < vnic.length; i++) {
      VirtualNic.Specification vnicSpec = (vnic[i]).spec;
      if (vnicSpec != null && vnicSpec.ip != null)
        if (!StringUtils.isEmpty(vnicSpec.ip.ipAddress))
          ipAll.add(vnicSpec.ip.ipAddress);  
    } 
    return ipAll;
  }
  
  private static List<String> getVnicIpV6Addresses(VirtualNic[] vnic) {
    if (ArrayUtils.isEmpty((Object[])vnic))
      return new ArrayList<>(0); 
    List<String> ipAll = new ArrayList<>();
    for (int i = 0; i < vnic.length; i++) {
      VirtualNic.Specification vnicSpec = (vnic[i]).spec;
      if (vnicSpec != null && vnicSpec.ip != null) {
        List<String> ipv6s = getIPv6(vnicSpec.ip.ipV6Config);
        if (ipv6s.size() > 0)
          ipAll.addAll(ipv6s); 
      } 
    } 
    return ipAll;
  }
  
  private static List<String> getPnicIpV4Addresses(PhysicalNic[] pnic) {
    if (ArrayUtils.isEmpty((Object[])pnic))
      return new ArrayList<>(0); 
    List<String> ipAll = new ArrayList<>();
    for (int i = 0; i < pnic.length; i++) {
      PhysicalNic.Specification pnicSpec = (pnic[i]).spec;
      if (pnicSpec != null && pnicSpec.ip != null)
        if (!StringUtils.isEmpty(pnicSpec.ip.ipAddress))
          ipAll.add(pnicSpec.ip.ipAddress);  
    } 
    return ipAll;
  }
  
  private static List<String> getPnicIpV6Addresses(PhysicalNic[] pnic) {
    if (ArrayUtils.isEmpty((Object[])pnic))
      return new ArrayList<>(0); 
    List<String> ipAll = new ArrayList<>();
    for (int i = 0; i < pnic.length; i++) {
      PhysicalNic.Specification pnicSpec = (pnic[i]).spec;
      if (pnicSpec != null && pnicSpec.ip != null) {
        List<String> ipv6s = getIPv6(pnicSpec.ip.ipV6Config);
        if (ipv6s.size() > 0)
          ipAll.addAll(ipv6s); 
      } 
    } 
    return ipAll;
  }
  
  private static List<String> getIPv6(IpConfig.IpV6AddressConfiguration ipV6Config) {
    if (ipV6Config == null || ArrayUtils.isEmpty((Object[])ipV6Config.ipV6Address))
      return new ArrayList<>(0); 
    IpConfig.IpV6Address[] ipV6Addr = ipV6Config.ipV6Address;
    List<String> ipv6s = new ArrayList<>();
    for (int i = 0; i < ipV6Addr.length; i++) {
      if (!StringUtils.isEmpty((ipV6Addr[i]).ipAddress))
        ipv6s.add((ipV6Addr[i]).ipAddress); 
    } 
    return ipv6s;
  }
}

package com.vmware.ph.phservice.common.vim.vc.util;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.internal.ProductVersion;
import com.vmware.ph.phservice.common.vim.pc.VimPropertyCollectorReader;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorReader;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorUtil;
import com.vmware.vim.binding.vim.HostSystem;
import com.vmware.vim.binding.vim.fault.HostConfigFault;
import com.vmware.vim.binding.vim.host.ConfigInfo;
import com.vmware.vim.binding.vim.host.ConfigManager;
import com.vmware.vim.binding.vim.host.InternalConfigManager;
import com.vmware.vim.binding.vim.host.IpConfig;
import com.vmware.vim.binding.vim.host.NetCapabilities;
import com.vmware.vim.binding.vim.host.NetworkInfo;
import com.vmware.vim.binding.vim.host.NetworkSystem;
import com.vmware.vim.binding.vim.host.TelemetryManager;
import com.vmware.vim.binding.vim.host.VirtualNic;
import com.vmware.vim.binding.vim.host.VirtualNicManager;
import com.vmware.vim.binding.vim.option.OptionValue;
import com.vmware.vim.binding.vim.view.ContainerView;
import com.vmware.vim.binding.vim.vsan.host.ConfigInfo;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.query.InvalidProperty;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HostReader {
  private static final String VSAN_WITNESS_APPLIANCE_HOST_OPTION_KEY = "Misc.vsanWitnessVirtualAppliance";
  
  private static final Log _log = LogFactory.getLog(HostReader.class);
  
  private final Client _vlsiClient;
  
  private final VmodlTypeMap _vmodlTypeMap;
  
  public HostReader(VcClient vcClient) {
    this(vcClient.getVlsiClient(), vcClient.getVmodlContext().getVmodlTypeMap());
  }
  
  public HostReader(VmomiClient vimClient) {
    this(vimClient.getVlsiClient(), vimClient.getVmodlContext().getVmodlTypeMap());
  }
  
  public HostReader(Client vlsiClient, VmodlTypeMap vmodlTypeMap) {
    this._vlsiClient = vlsiClient;
    this._vmodlTypeMap = vmodlTypeMap;
  }
  
  public List<ManagedObjectReference> getHostMoRefs() {
    List<String> hostPropertyNames = Arrays.asList(new String[] { "name" });
    List<PropertyCollectorReader.PcResourceItem> results = getHosts(hostPropertyNames);
    LinkedList<ManagedObjectReference> hostMoRefs = new LinkedList<>();
    if (results != null)
      for (PropertyCollectorReader.PcResourceItem pcResourceItem : results) {
        ManagedObjectReference moRef = (ManagedObjectReference)pcResourceItem.getPropertyValues().get(0);
        hostMoRefs.add(moRef);
      }  
    return hostMoRefs;
  }
  
  public List<PropertyCollectorReader.PcResourceItem> getHosts(List<String> hostPropertyNames) {
    VimPropertyCollectorReader pcReader = new VimPropertyCollectorReader(this._vlsiClient);
    ManagedObjectReference containerViewMoRef = pcReader.createContainerView();
    List<Pair<VmodlType, String>> traversalChain = Arrays.asList((Pair<VmodlType, String>[])new Pair[] { new Pair<>(this._vmodlTypeMap.getVmodlType(ContainerView.class), "view") });
    PropertyCollector.FilterSpec hostsFilterSpec = PropertyCollectorUtil.createTraversableFilterSpec(this._vmodlTypeMap
        .getVmodlType("HostSystem"), hostPropertyNames, containerViewMoRef, traversalChain, this._vmodlTypeMap);
    try {
      List<PropertyCollectorReader.PcResourceItem> results = null;
      try {
        results = pcReader.retrieveContent(hostsFilterSpec, hostPropertyNames, 0, -1);
      } catch (InvalidProperty e) {
        if (_log.isDebugEnabled())
          _log.debug("Failed to read hosts: ", (Throwable)e); 
      } 
      return results;
    } finally {
      pcReader.destroyContainerView(containerViewMoRef);
    } 
  }
  
  public TelemetryManager getTelemetryManager(ManagedObjectReference hostMoRef) {
    HostSystem host = (HostSystem)this._vlsiClient.createStub(HostSystem.class, hostMoRef);
    InternalConfigManager internalConfigManager = host.retrieveInternalConfigManager();
    ManagedObjectReference telemetryManagerMoRef = internalConfigManager.getTelemetryManager();
    if (telemetryManagerMoRef != null)
      return (TelemetryManager)this._vlsiClient.createStub(TelemetryManager.class, telemetryManagerMoRef); 
    return null;
  }
  
  public String getHostUuid(ManagedObjectReference hostMoRef) {
    Objects.requireNonNull(hostMoRef);
    HostSystem host = (HostSystem)this._vlsiClient.createStub(HostSystem.class, hostMoRef);
    String hostUuid = host.getSummary().getHardware().getUuid();
    return hostUuid;
  }
  
  public ProductVersion getHostVersion(ManagedObjectReference hostMoRef) {
    Objects.requireNonNull(hostMoRef);
    HostSystem host = (HostSystem)this._vlsiClient.createStub(HostSystem.class, hostMoRef);
    String hostVersion = host.getConfig().getProduct().getVersion();
    ProductVersion version = new ProductVersion(hostVersion);
    return version;
  }
  
  public boolean isConnected(ManagedObjectReference hostMoRef) {
    HostSystem host = (HostSystem)this._vlsiClient.createStub(HostSystem.class, hostMoRef);
    HostSystem.ConnectionState hostConnectionState = host.getRuntime().getConnectionState();
    return (hostConnectionState == HostSystem.ConnectionState.connected);
  }
  
  public boolean isVsanEnabled(ManagedObjectReference hostMoRef) {
    HostSystem host = (HostSystem)this._vlsiClient.createStub(HostSystem.class, hostMoRef);
    ConfigInfo vsanHostConfigInfo = host.getConfig().getVsanHostConfig();
    boolean isVsanEnabled = (vsanHostConfigInfo != null && vsanHostConfigInfo.getEnabled().booleanValue());
    return isVsanEnabled;
  }
  
  public boolean isVsanWitness(ManagedObjectReference hostMoRef) {
    HostSystem host = (HostSystem)this._vlsiClient.createStub(HostSystem.class, hostMoRef);
    OptionValue[] configOptions = host.getConfig().getOption();
    boolean isVsanWitness = false;
    for (OptionValue configOption : configOptions) {
      if ("Misc.vsanWitnessVirtualAppliance".equals(configOption.getKey())) {
        Object value = configOption.getValue();
        if (value instanceof Integer) {
          isVsanWitness = (((Integer)value).intValue() != 0);
          break;
        } 
        if (value instanceof Long)
          isVsanWitness = (((Long)value).longValue() != 0L); 
        break;
      } 
    } 
    return isVsanWitness;
  }
  
  public List<String> getManagementIps(ManagedObjectReference hostMoRef) {
    VirtualNic[] hostNics;
    Objects.requireNonNull(hostMoRef);
    HostSystem host = (HostSystem)this._vlsiClient.createStub(HostSystem.class, hostMoRef);
    if (usesServiceConsole(host)) {
      hostNics = getServiceConsoleNics(host);
    } else {
      hostNics = getManagmentNetworkNics(host);
    } 
    List<String> hostIpv4s = new ArrayList<>();
    List<String> hostIpv6s = new ArrayList<>();
    for (VirtualNic hostNic : hostNics) {
      String ipAddress = getIpAddress(hostNic);
      if (ipAddress != null && !ipAddress.isEmpty())
        hostIpv4s.add(ipAddress); 
      IpConfig.IpV6Address[] ipV6Addresses = getIpV6Addresses(hostNic);
      if (ipV6Addresses != null)
        for (IpConfig.IpV6Address ipV6Address : ipV6Addresses) {
          String ipv6Address = ipV6Address.getIpAddress();
          if (!ipv6Address.isEmpty())
            hostIpv6s.add(ipv6Address); 
        }  
    } 
    List<String> hostIps = new ArrayList<>();
    hostIps.addAll(hostIpv4s);
    hostIps.addAll(hostIpv6s);
    return hostIps;
  }
  
  private VirtualNic[] getManagmentNetworkNics(HostSystem host) {
    ConfigManager configManager = host.getConfigManager();
    ManagedObjectReference virtualNicMoRef = configManager.getVirtualNicManager();
    VirtualNicManager vNicManager = (VirtualNicManager)this._vlsiClient.createStub(VirtualNicManager.class, virtualNicMoRef);
    VirtualNic[] result = null;
    try {
      VirtualNic[] candidateNics;
      String[] selectedNics;
      VirtualNicManager.NetConfig netConfig = vNicManager.queryNetConfig(VirtualNicManager.NicType.management
          .toString());
      if (netConfig == null) {
        candidateNics = null;
        selectedNics = null;
      } else {
        candidateNics = netConfig.getCandidateVnic();
        selectedNics = netConfig.getSelectedVnic();
      } 
      if (candidateNics == null || selectedNics == null) {
        result = null;
      } else {
        result = new VirtualNic[selectedNics.length];
        for (int i = 0; i < selectedNics.length; i++) {
          for (VirtualNic nic : candidateNics) {
            if (selectedNics[i].equals(nic.getKey())) {
              result[i] = nic;
              break;
            } 
          } 
          assert result[i] != null;
        } 
      } 
    } catch (HostConfigFault e) {
      String msg = "Failed querying network configuration of the host.";
      _log.error(msg, (Throwable)e);
    } 
    return result;
  }
  
  boolean usesServiceConsole(HostSystem host) {
    ConfigManager configManager = host.getConfigManager();
    ManagedObjectReference netSystemMoRef = configManager.getNetworkSystem();
    NetworkSystem netSystem = (NetworkSystem)this._vlsiClient.createStub(NetworkSystem.class, netSystemMoRef);
    NetCapabilities netCapabilities = netSystem.getCapabilities();
    return netCapabilities.isUsesServiceConsoleNic();
  }
  
  VirtualNic[] getServiceConsoleNics(HostSystem host) {
    ConfigInfo info = host.getConfig();
    NetworkInfo networkInfo = info.getNetwork();
    return networkInfo.getConsoleVnic();
  }
  
  private static String getIpAddress(VirtualNic hostNic) {
    IpConfig ipConfig = hostNic.getSpec().getIp();
    if (ipConfig == null)
      return null; 
    return ipConfig.getIpAddress();
  }
  
  private static IpConfig.IpV6Address[] getIpV6Addresses(VirtualNic hostNic) {
    IpConfig ipConfig = hostNic.getSpec().getIp();
    if (ipConfig == null)
      return null; 
    IpConfig.IpV6AddressConfiguration ipV6Config = ipConfig.getIpV6Config();
    if (ipV6Config == null)
      return null; 
    return ipV6Config.getIpV6Address();
  }
}

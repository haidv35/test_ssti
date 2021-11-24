package com.vmware.ph.phservice.common.vim.vc.util;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.vim.internal.vc.pc.VcPropertyCollectorReader;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorReader;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorUtil;
import com.vmware.vim.binding.vim.view.ContainerView;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.query.InvalidProperty;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VirtualMachineReader {
  private static final Log _log = LogFactory.getLog(VirtualMachineReader.class);
  
  public static List<ManagedObjectReference> getVmMoRefs(VcClient vcClient, int queryOffset, int quertLimit) {
    List<String> vmPropertyNames = Arrays.asList(new String[] { "config.name" });
    VmodlTypeMap vmodlTypeMap = vcClient.getVmodlContext().getVmodlTypeMap();
    VcPropertyCollectorReader pcReader = new VcPropertyCollectorReader(vcClient);
    ManagedObjectReference containerViewMoRef = pcReader.createContainerView();
    List<Pair<VmodlType, String>> traversalChain = Arrays.asList((Pair<VmodlType, String>[])new Pair[] { new Pair<>(vmodlTypeMap.getVmodlType(ContainerView.class), "view") });
    List<ManagedObjectReference> vmMoRefs = Collections.emptyList();
    PropertyCollector.FilterSpec vmFilterSpec = PropertyCollectorUtil.createTraversableFilterSpec(vmodlTypeMap
        .getVmodlType("VirtualMachine"), vmPropertyNames, containerViewMoRef, traversalChain, vmodlTypeMap);
    try {
      List<PropertyCollectorReader.PcResourceItem> results = null;
      try {
        results = pcReader.retrieveContent(vmFilterSpec, vmPropertyNames, queryOffset, quertLimit);
      } catch (InvalidProperty e) {
        if (_log.isDebugEnabled())
          _log.debug("Failed to read VirtualMachines: ", (Throwable)e); 
      } 
      if (results != null) {
        vmMoRefs = new ArrayList<>(results.size());
        for (PropertyCollectorReader.PcResourceItem pcResourceItem : results) {
          ManagedObjectReference moRef = (ManagedObjectReference)pcResourceItem.getPropertyValues().get(0);
          vmMoRefs.add(moRef);
        } 
      } 
    } finally {
      pcReader.destroyContainerView(containerViewMoRef);
    } 
    return vmMoRefs;
  }
}

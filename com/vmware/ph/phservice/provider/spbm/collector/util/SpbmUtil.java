package com.vmware.ph.phservice.provider.spbm.collector.util;

import com.vmware.vim.binding.pbm.ServerObjectRef;
import com.vmware.vim.binding.pbm.compliance.ComplianceResult;
import com.vmware.vim.binding.pbm.compliance.RollupComplianceResult;
import com.vmware.vim.binding.vim.vslm.ID;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.List;

public class SpbmUtil {
  private static final String VIRTUAL_MACHINE_SOR_TYPE = "virtualMachine";
  
  private static final String VIRTUAL_DISK_UUID_SOR_TYPE = "virtualDiskUUID";
  
  public static ServerObjectRef[] createSoRefsForVms(List<ManagedObjectReference> vmMoRefs) {
    ServerObjectRef[] serverObjectRefs = new ServerObjectRef[vmMoRefs.size()];
    int objCount = 0;
    for (ManagedObjectReference vmMoRef : vmMoRefs)
      serverObjectRefs[objCount++] = 
        createSoRef(vmMoRef.getValue(), "virtualMachine", vmMoRef.getServerGuid()); 
    return serverObjectRefs;
  }
  
  public static List<ServerObjectRef> createSorListForVm(String vcUuid, List<String> vmIds) {
    List<ServerObjectRef> soRefs = new ArrayList<>(vmIds.size());
    for (String vmId : vmIds)
      soRefs.add(createSoRef(vmId, "virtualMachine", vcUuid)); 
    return soRefs;
  }
  
  public static ServerObjectRef createSoRef(String value, String type, String vcUuid) {
    ServerObjectRef soRef = new ServerObjectRef();
    soRef.setKey(value);
    soRef.setObjectType(type);
    soRef.setServerUuid(vcUuid);
    return soRef;
  }
  
  public static ServerObjectRef[] createSoRefsForFcds(String vcUuid, ID[] vStorageObjectIds) {
    ServerObjectRef[] soRefs = new ServerObjectRef[vStorageObjectIds.length];
    for (int i = 0; i < vStorageObjectIds.length; i++)
      soRefs[i] = createSoRef(vStorageObjectIds[i].getId(), "virtualDiskUUID", vcUuid); 
    return soRefs;
  }
  
  public static List<ServerObjectRef> createSoRefsForFcds(String vcUuid, List<String> fcdIds) {
    List<ServerObjectRef> soRefs = new ArrayList<>(fcdIds.size());
    for (String fcdId : fcdIds)
      soRefs.add(createSoRef(fcdId, "virtualDiskUUID", vcUuid)); 
    return soRefs;
  }
  
  public static List<RollupComplianceResult> createRollupComplianceResultWithNullComplianceResult(List<ServerObjectRef> soRefs) {
    List<RollupComplianceResult> rollupComplianceResult = new ArrayList<>(soRefs.size());
    for (ServerObjectRef vmSoRef : soRefs) {
      RollupComplianceResult result = new RollupComplianceResult();
      result.setEntity(vmSoRef);
      result.setResult(null);
      rollupComplianceResult.add(result);
    } 
    return rollupComplianceResult;
  }
  
  public static List<ComplianceResult> createComplianceResultWithNullComplianceStatus(List<ServerObjectRef> soRefs) {
    List<ComplianceResult> complianceResult = new ArrayList<>(soRefs.size());
    for (ServerObjectRef soRef : soRefs) {
      ComplianceResult result = new ComplianceResult();
      result.setEntity(soRef);
      result.setComplianceStatus(null);
      complianceResult.add(result);
    } 
    return complianceResult;
  }
}

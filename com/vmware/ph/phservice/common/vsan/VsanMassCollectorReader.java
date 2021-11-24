package com.vmware.ph.phservice.common.vsan;

import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import com.vmware.vim.vsan.binding.vim.VsanMassCollector;
import com.vmware.vim.vsan.binding.vim.VsanMassCollectorObjectCollectionEnum;
import com.vmware.vim.vsan.binding.vim.VsanMassCollectorSpec;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VsanMassCollectorReader {
  public static final ManagedObjectReference VSAN_MASS_COLLECTOR_MO_REF = new ManagedObjectReference("VsanMassCollector", "vsan-mass-collector");
  
  private static final String VSAN_CLUSTER_UUID_PROPERTY = "configurationEx.vsanConfigInfo.defaultConfig.uuid";
  
  private static final Log _log = LogFactory.getLog(VsanMassCollectorReader.class);
  
  private final VmomiClient _vsanHealthClient;
  
  public VsanMassCollectorReader(VmomiClient vsanHealthClient) {
    this._vsanHealthClient = vsanHealthClient;
  }
  
  public ManagedObjectReference getMoRef() throws Exception {
    VsanMassCollector vsanMassCollector = this._vsanHealthClient.<VsanMassCollector>createStub(VSAN_MASS_COLLECTOR_MO_REF);
    return vsanMassCollector._getRef();
  }
  
  public PropertyCollector.ObjectContent[] retrieveProperties(VsanMassCollectorSpec[] querySpecs) throws Exception {
    VsanMassCollector vsanMassCollector = this._vsanHealthClient.<VsanMassCollector>createStub(VSAN_MASS_COLLECTOR_MO_REF);
    _log.info("[VsanMassCollector] Calling retrieveProperties on the VsanMassCollector with spec: " + 
        
        Arrays.toString((Object[])querySpecs));
    PropertyCollector.ObjectContent[] vsanData = vsanMassCollector.vsanRetrieveProperties(querySpecs);
    return vsanData;
  }
  
  public String retrievePropertiesJson(VsanMassCollectorSpec[] querySpecs) throws Exception {
    VsanMassCollector vsanMassCollector = this._vsanHealthClient.<VsanMassCollector>createStub(VSAN_MASS_COLLECTOR_MO_REF);
    _log.info("[VsanMassCollector] Calling retrievePropertiesJson on the VsanMassCollector with spec: " + 
        
        Arrays.toString((Object[])querySpecs));
    String vsanData = vsanMassCollector.vsanRetrievePropertiesJson(querySpecs);
    return vsanData;
  }
  
  public String getClusterUuid(ManagedObjectReference clusterMoRef) throws Exception {
    VsanMassCollectorSpec querySpec = new VsanMassCollectorSpec(new ManagedObjectReference[] { clusterMoRef }, VsanMassCollectorObjectCollectionEnum.ALL_VSAN_ENABLED_CLUSTERS.name(), new String[] { "configurationEx.vsanConfigInfo.defaultConfig.uuid" }, null, null);
    PropertyCollector.ObjectContent[] properties = retrieveProperties(new VsanMassCollectorSpec[] { querySpec });
    String clusterUuid = null;
    if (ArrayUtils.isNotEmpty((Object[])properties) && (properties[0]).missingSet == null)
      clusterUuid = (String)(properties[0]).propSet[0].getVal(); 
    return clusterUuid;
  }
  
  public Map<String, ManagedObjectReference> getClusterUuidToMoRefs(List<ManagedObjectReference> clusterMoRefs) throws Exception {
    VsanMassCollectorSpec querySpec = new VsanMassCollectorSpec(clusterMoRefs.<ManagedObjectReference>toArray(new ManagedObjectReference[clusterMoRefs.size()]), VsanMassCollectorObjectCollectionEnum.ALL_VSAN_ENABLED_CLUSTERS.name(), new String[] { "configurationEx.vsanConfigInfo.defaultConfig.uuid" }, null, null);
    PropertyCollector.ObjectContent[] properties = retrieveProperties(new VsanMassCollectorSpec[] { querySpec });
    Map<String, ManagedObjectReference> clusterUuidToMoRef = new LinkedHashMap<>();
    if (ArrayUtils.isNotEmpty((Object[])properties))
      for (int i = 0; i < properties.length; i++) {
        PropertyCollector.ObjectContent objectContent = properties[i];
        if (objectContent.missingSet == null) {
          String clusterUuid = (String)objectContent.propSet[0].getVal();
          String clusterMoRefValue = objectContent.obj.getValue();
          clusterMoRefs.stream()
            .filter(moRef -> moRef.getValue().equals(clusterMoRefValue))
            .findFirst()
            .ifPresent(moRef -> (ManagedObjectReference)clusterUuidToMoRef.put(clusterUuid, moRef));
        } 
      }  
    return clusterUuidToMoRef;
  }
}

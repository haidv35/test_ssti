package com.vmware.ph.phservice.cloud.dataapp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.analytics.vapi.DataAppAgentTypes;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentCreateSpec;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataAppAgentRequestDeserializer {
  private static final Log _log = LogFactory.getLog(DataAppAgentRequestDeserializer.class);
  
  public static DataAppAgentTypes.CollectRequestSpec deserializeCollectRequestSpec(String collectRequestSpecJson) {
    DataAppAgentTypes.CollectRequestSpec collectRequestSpec = new DataAppAgentTypes.CollectRequestSpec();
    try {
      collectRequestSpec = (DataAppAgentTypes.CollectRequestSpec)(new ObjectMapper()).readValue(collectRequestSpecJson, DataAppAgentTypes.CollectRequestSpec.class);
    } catch (Exception e) {
      _log.warn("collectRequestSpecJson could not be deserialized successfully.", e);
    } 
    return collectRequestSpec;
  }
  
  public static String deserializeJsonLdContextData(String jsonLdContextData) {
    if (jsonLdContextData != null && jsonLdContextData.startsWith("\"")) {
      jsonLdContextData = jsonLdContextData.substring(1, jsonLdContextData.length() - 1);
      jsonLdContextData = jsonLdContextData.replace("\\\"", "\"");
    } 
    return jsonLdContextData;
  }
  
  public static DataAppAgentCreateSpec deserializeCreateSpec(String createSpecJson) throws IOException {
    if (StringUtils.isBlank(createSpecJson))
      return null; 
    ObjectMapper objectMapper = new ObjectMapper();
    DataAppAgentTypes.CreateSpec createSpecDto = (DataAppAgentTypes.CreateSpec)objectMapper.readValue(createSpecJson, DataAppAgentTypes.CreateSpec.class);
    DataAppAgentTypes.ManifestSpec manifestSpecDto = createSpecDto.getManifestSpec();
    ManifestSpec manifestSpec = createManifestSpec(manifestSpecDto);
    DataAppAgentCreateSpec agentCreateSpec = new DataAppAgentCreateSpec(manifestSpec, createSpecDto.getObjectType(), createSpecDto.getCollectionTriggerDataNeeded(), createSpecDto.getDeploymentDataNeeded(), createSpecDto.getResultNeeded(), createSpecDto.getSignalCollectionCompleted(), createSpecDto.getLocalManifestPath(), createSpecDto.getLocalPayloadPath(), createSpecDto.getLocalObfuscationMapPath());
    return agentCreateSpec;
  }
  
  private static ManifestSpec createManifestSpec(DataAppAgentTypes.ManifestSpec manifestSpecDto) {
    if (manifestSpecDto == null)
      return null; 
    ManifestSpec manifestSpec = new ManifestSpec(manifestSpecDto.getResourceId(), manifestSpecDto.getDataType(), manifestSpecDto.getObjectId(), manifestSpecDto.getVersionDataType(), manifestSpecDto.getVersionObjectId());
    return manifestSpec;
  }
}

package com.vmware.ph.phservice.cloud.dataapp.internal.repository;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.repository.AgentObfuscationRepository;
import com.vmware.ph.phservice.common.internal.ConfigurationService;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class PropertyControlledAgentObfuscationRepositoryWrapper implements AgentObfuscationRepository {
  private final ConfigurationService _configurationService;
  
  private final String _propNameForObfuscationsFolderPath;
  
  public PropertyControlledAgentObfuscationRepositoryWrapper(ConfigurationService configurationService, String propNameForObfuscationsFolderPath) {
    this._configurationService = configurationService;
    this._propNameForObfuscationsFolderPath = propNameForObfuscationsFolderPath;
  }
  
  public Map<String, String> readObfuscationMap(DataAppAgentId agentId, String objectId) {
    AgentObfuscationRepository obfuscationRepository = getAgentObfuscationRepository();
    if (obfuscationRepository != null)
      return obfuscationRepository.readObfuscationMap(agentId, objectId); 
    return Collections.emptyMap();
  }
  
  public void writeObfuscationMap(DataAppAgentId agentId, String objectId, Map<String, String> obfuscationMap) {
    AgentObfuscationRepository obfuscationRepository = getAgentObfuscationRepository();
    if (obfuscationRepository != null)
      obfuscationRepository.writeObfuscationMap(agentId, objectId, obfuscationMap); 
  }
  
  private AgentObfuscationRepository getAgentObfuscationRepository() {
    String targetObfuscationsFolderPath = this._configurationService.getProperty(this._propNameForObfuscationsFolderPath);
    AgentObfuscationRepository obfuscationRepository = null;
    if (!StringUtils.isBlank(targetObfuscationsFolderPath))
      obfuscationRepository = new FileSystemAgentObfuscationRepository(Paths.get(targetObfuscationsFolderPath, new String[0])); 
    return obfuscationRepository;
  }
}

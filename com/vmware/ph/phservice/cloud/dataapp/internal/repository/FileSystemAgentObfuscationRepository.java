package com.vmware.ph.phservice.cloud.dataapp.internal.repository;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.repository.AgentObfuscationRepository;
import com.vmware.ph.phservice.common.internal.file.FileUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

public class FileSystemAgentObfuscationRepository implements AgentObfuscationRepository {
  private static final Log _log = LogFactory.getLog(FileSystemAgentObfuscationRepository.class);
  
  private static final String OBFUSCATION_FILE_SUFFIX = "_obfuscationTableForHuman";
  
  private final Path _obfuscationsFolderPath;
  
  public FileSystemAgentObfuscationRepository(Path obfuscationsFolderPath) {
    this._obfuscationsFolderPath = obfuscationsFolderPath;
  }
  
  public Map<String, String> readObfuscationMap(DataAppAgentId agentId, String objectId) {
    Map<String, String> obfuscationMap = new HashMap<>();
    File obfuscationFile = getObfuscationFile(agentId, objectId);
    try {
      JSONObject obfuscationMapJson = new JSONObject(FileUtil.readGzipFileToString(obfuscationFile));
      for (String obfuscationKey : obfuscationMapJson.keySet())
        obfuscationMap.put(obfuscationKey, obfuscationMapJson
            
            .getString(obfuscationKey)); 
    } catch (IOException|org.json.JSONException e) {
      _log.warn("Unable to read obfuscation map for agent: " + agentId + ", due to: " + e
          .getMessage());
    } 
    return obfuscationMap;
  }
  
  public void writeObfuscationMap(DataAppAgentId agentId, String objectId, Map<String, String> obfuscationMap) {
    File obfuscationFile = getObfuscationFile(agentId, objectId);
    JSONObject obfuscationMapJson = new JSONObject(obfuscationMap);
    try {
      FileUtil.writeStringToGzipFile(obfuscationFile, obfuscationMapJson
          
          .toString());
    } catch (IOException e) {
      _log.warn("Unable to write obfuscation map for agent: " + agentId + ", due to: " + e
          .getMessage());
    } 
  }
  
  private File getObfuscationFile(DataAppAgentId agentId, String objectId) {
    String obfuscationFileName = getObfuscationFileName(agentId, objectId);
    File obfuscationFile = new File(this._obfuscationsFolderPath.toFile(), obfuscationFileName);
    return obfuscationFile;
  }
  
  private static String getObfuscationFileName(DataAppAgentId agentId, String objectId) {
    StringBuilder obfuscationFileNameBuilder = new StringBuilder();
    if (!StringUtils.isBlank(objectId))
      obfuscationFileNameBuilder.append(objectId); 
    String pluginType = agentId.getPluginType();
    if (!StringUtils.isBlank(pluginType)) {
      if (obfuscationFileNameBuilder.length() > 0)
        obfuscationFileNameBuilder.append("-"); 
      obfuscationFileNameBuilder.append(pluginType);
    } 
    obfuscationFileNameBuilder.append("_obfuscationTableForHuman");
    obfuscationFileNameBuilder.append(".json.gz");
    String obfuscationFileName = obfuscationFileNameBuilder.toString();
    obfuscationFileName = FileUtil.NOT_ALLOWED_FILENAME_CHARACTERS_PATTERN.matcher(obfuscationFileName).replaceAll("_");
    return obfuscationFileName;
  }
}

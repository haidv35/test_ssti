package com.vmware.ph.phservice.common.cis.appliance;

import com.vmware.ph.phservice.common.internal.file.FileUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultDeploymentNodeTypeReader implements DeploymentNodeTypeReader {
  private static final Log _log = LogFactory.getLog(DefaultDeploymentNodeTypeReader.class);
  
  private final File _deploymentNodeTypeFile;
  
  private final File _vmcGatewayDeploymentNodeTypeFile;
  
  private final String _vmcGatewayDeploymentNodeTypeEntry;
  
  private final Object _lock = new Object();
  
  private volatile DeploymentNodeTypeReader.DeploymentNodeType _deploymentNodeType = DeploymentNodeTypeReader.DeploymentNodeType.NONE;
  
  public DefaultDeploymentNodeTypeReader(Path deploymentNodeTypeFilePath, Path vmcGatewayDeploymentNodeTypeFilePath, String vmcGatewayDeploymentNodeTypeEntry) {
    this._deploymentNodeTypeFile = deploymentNodeTypeFilePath.toFile();
    this._vmcGatewayDeploymentNodeTypeFile = vmcGatewayDeploymentNodeTypeFilePath.toFile();
    this._vmcGatewayDeploymentNodeTypeEntry = vmcGatewayDeploymentNodeTypeEntry;
  }
  
  public DeploymentNodeTypeReader.DeploymentNodeType getDeploymentNodeType() {
    if (this._deploymentNodeType.equals(DeploymentNodeTypeReader.DeploymentNodeType.NONE))
      synchronized (this._lock) {
        if (this._deploymentNodeType.equals(DeploymentNodeTypeReader.DeploymentNodeType.NONE))
          this._deploymentNodeType = discoverDeploymentNodeType(this._deploymentNodeTypeFile, this._vmcGatewayDeploymentNodeTypeFile, this._vmcGatewayDeploymentNodeTypeEntry); 
      }  
    return this._deploymentNodeType;
  }
  
  private static DeploymentNodeTypeReader.DeploymentNodeType discoverDeploymentNodeType(File deploymentNodeTypeFile, File vmcGatewayDeploymentNodeTypeFile, String vmcGatewayDeploymentNodeTypeEntry) {
    DeploymentNodeTypeReader.DeploymentNodeType deploymentNodeType = DeploymentNodeTypeReader.DeploymentNodeType.NONE;
    try {
      deploymentNodeType = tryDiscoverVmcGatewayDeploymentNodeTypeFromFile(vmcGatewayDeploymentNodeTypeFile, vmcGatewayDeploymentNodeTypeEntry);
      if (DeploymentNodeTypeReader.DeploymentNodeType.NONE.equals(deploymentNodeType))
        deploymentNodeType = tryDiscoverDeploymentNodeTypeFromFile(deploymentNodeTypeFile); 
    } catch (IOException e) {
      if (_log.isDebugEnabled())
        _log.debug("Failed to read the deploment node type.", e); 
    } 
    if (_log.isInfoEnabled())
      _log.info(String.format("Node type is '%s'.", new Object[] { deploymentNodeType })); 
    return deploymentNodeType;
  }
  
  private static DeploymentNodeTypeReader.DeploymentNodeType tryDiscoverVmcGatewayDeploymentNodeTypeFromFile(File vmcGatewayDeploymentNodeTypeFile, String vmcGatewayDeploymentNodeTypeEntry) throws IOException {
    boolean isDeployementNodeTypeVmcGateway = FileUtil.isStringInFile(vmcGatewayDeploymentNodeTypeFile
        .toPath(), vmcGatewayDeploymentNodeTypeEntry);
    if (isDeployementNodeTypeVmcGateway)
      return DeploymentNodeTypeReader.DeploymentNodeType.VMC_GATEWAY; 
    return DeploymentNodeTypeReader.DeploymentNodeType.NONE;
  }
  
  private static DeploymentNodeTypeReader.DeploymentNodeType tryDiscoverDeploymentNodeTypeFromFile(File deploymentNodeTypeFile) throws IOException {
    String deploymentNodeTypeValue = FileUtil.readFileToString(deploymentNodeTypeFile);
    DeploymentNodeTypeReader.DeploymentNodeType deploymentNodeType = convertStringToDeploymentNodeType(deploymentNodeTypeValue);
    return deploymentNodeType;
  }
  
  private static DeploymentNodeTypeReader.DeploymentNodeType convertStringToDeploymentNodeType(String deploymentNodeTypeValue) {
    DeploymentNodeTypeReader.DeploymentNodeType depolymentNodeType = DeploymentNodeTypeReader.DeploymentNodeType.NONE;
    if (deploymentNodeTypeValue != null && !deploymentNodeTypeValue.trim().isEmpty()) {
      try {
        String deploymentNodeTypeEnumConstantName = deploymentNodeTypeValue.trim().toUpperCase(Locale.US);
        depolymentNodeType = DeploymentNodeTypeReader.DeploymentNodeType.valueOf(deploymentNodeTypeEnumConstantName);
      } catch (IllegalArgumentException e) {
        if (_log.isInfoEnabled())
          _log.info(
              String.format("Unknown deployment node type '%s'.", new Object[] { deploymentNodeTypeValue }), e); 
      } 
    } else if (_log.isInfoEnabled()) {
      _log.info("Invalid deployment node type value of 'null'.");
    } 
    return depolymentNodeType;
  }
}

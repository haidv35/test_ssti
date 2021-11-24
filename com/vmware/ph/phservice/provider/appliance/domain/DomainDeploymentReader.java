package com.vmware.ph.phservice.provider.appliance.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;

public class DomainDeploymentReader {
  private final String _deploymentDomainId;
  
  private final DeploymentInfo _deploymentInfo;
  
  public DomainDeploymentReader(String domainId, DeploymentInfo deploymentInfo) {
    this._deploymentDomainId = domainId;
    this._deploymentInfo = deploymentInfo;
  }
  
  public DeploymentDomain getDeploymentDomain() {
    Map<String, List<String>> siteNameToPscNodeIds = getSiteNameToPscNodeIds(this._deploymentInfo);
    String id = this._deploymentDomainId;
    int siteCount = siteNameToPscNodeIds.size();
    int pscNodeCount = (this._deploymentInfo.getPscNodes()).length;
    return new DeploymentDomain(id, siteCount, pscNodeCount);
  }
  
  public Map<String, DeploymentPscNode> getDeploymentPscNodes() {
    Map<String, List<String>> siteNameToPscNodeIds = getSiteNameToPscNodeIds(this._deploymentInfo);
    DeploymentInfo.ServiceInfo[] pscNodeServiceInfos = this._deploymentInfo.getPscNodes();
    Map<String, DeploymentPscNode> idToDeploymentPscNode = new LinkedHashMap<>();
    for (DeploymentInfo.ServiceInfo serviceInfo : pscNodeServiceInfos) {
      String id = getPscNodeIdFromServiceInfo(serviceInfo);
      String siteName = getSiteNameFromServiceInfo(serviceInfo);
      String siteId = generateSiteId(siteNameToPscNodeIds.get(siteName));
      String domainId = this._deploymentDomainId;
      boolean isEmbeddedDeployment = isEmbeddedDeployment(this._deploymentInfo.getHostName(), serviceInfo);
      idToDeploymentPscNode.put(id, new DeploymentPscNode(id, siteId, domainId, isEmbeddedDeployment, serviceInfo));
    } 
    return idToDeploymentPscNode;
  }
  
  public Map<String, DeploymentSite> getDeploymentSites() {
    Map<String, List<String>> siteNameToPscNodeIds = getSiteNameToPscNodeIds(this._deploymentInfo);
    Map<String, DeploymentSite> idToDeploymentSite = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : siteNameToPscNodeIds.entrySet()) {
      String id = generateSiteId(entry.getValue());
      String domainId = this._deploymentDomainId;
      int nodeCount = ((List)entry.getValue()).size();
      idToDeploymentSite.put(id, new DeploymentSite(id, domainId, nodeCount));
    } 
    return idToDeploymentSite;
  }
  
  private static Map<String, List<String>> getSiteNameToPscNodeIds(DeploymentInfo deploymentInfoMo) {
    Map<String, List<String>> siteNameToPscNodeIds = new HashMap<>();
    DeploymentInfo.ServiceInfo[] pscNodeServiceInfos = deploymentInfoMo.getPscNodes();
    for (DeploymentInfo.ServiceInfo pscNodeServiceInfo : pscNodeServiceInfos) {
      String siteName = getSiteNameFromServiceInfo(pscNodeServiceInfo);
      String pscNodeId = getPscNodeIdFromServiceInfo(pscNodeServiceInfo);
      List<String> pscNodesInSite = siteNameToPscNodeIds.get(siteName);
      if (pscNodesInSite == null) {
        pscNodesInSite = new ArrayList<>();
        siteNameToPscNodeIds.put(siteName, pscNodesInSite);
      } 
      pscNodesInSite.add(pscNodeId);
    } 
    return siteNameToPscNodeIds;
  }
  
  private static String getSiteNameFromServiceInfo(DeploymentInfo.ServiceInfo serviceInfo) {
    return serviceInfo.getServiceId().split(":")[0];
  }
  
  private static String getPscNodeIdFromServiceInfo(DeploymentInfo.ServiceInfo serviceInfo) {
    return serviceInfo.getServiceId().split(":")[1];
  }
  
  private static boolean isEmbeddedDeployment(String deploymentHostName, DeploymentInfo.ServiceInfo serviceInfo) {
    return deploymentHostName.equalsIgnoreCase(serviceInfo.getHostId());
  }
  
  private static String generateSiteId(List<String> pscNodeIds) {
    StringBuilder sb = new StringBuilder();
    for (String pscNodeId : pscNodeIds)
      sb.append(pscNodeId); 
    return DigestUtils.shaHex(sb.toString());
  }
}

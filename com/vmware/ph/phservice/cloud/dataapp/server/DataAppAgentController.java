package com.vmware.ph.phservice.cloud.dataapp.server;

import com.vmware.analytics.vapi.DataAppAgentTypes;
import com.vmware.ph.phservice.cloud.dataapp.AgentStatus;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgent;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentCreateSpec;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.AsyncCollectorDataAppAgent;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.CollectorDataAppAgent;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import com.vmware.ph.phservice.common.cdf.internal.dataapp.PluginResultUtil;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DataAppAgentController {
  private static final Log _log = LogFactory.getLog(DataAppAgentController.class);
  
  private static final String DATAAPP_AGENT_PATH = "/dataapp/agent";
  
  private static final String LOCAL_MANIFEST_DEFAULT_PATH = "/etc/vmware-analytics/local_payload.manifest";
  
  private static final String LOCAL_AGENT_COLLECTOR_ID = "local_payload";
  
  private static final String LOCAL_AGENT_COLLECTOR_INSTANCE_ID = "local-payload-audit-00000000-0000-0000-0000-000000000000";
  
  private static final String LOCAL_AGENT_DEPLOYMENT_SECRET = "2609f2dc4ad44bade362250581dfb0c7c314a036322175c9d2f3d6f2159034a5";
  
  private static final String LOCAL_AGENT_PLUGIN_TYPE = "audit";
  
  private final DataAppAgentManager _dataAppAgentService;
  
  private String _pathToLocalManifest = "/etc/vmware-analytics/local_payload.manifest";
  
  private Future<String> _localPayloadCollectionTaskFuture = null;
  
  private long _localPayloadRetentionPeriodMillis = 0L;
  
  public DataAppAgentController(DataAppAgentManager dataAppAgentService) {
    this._dataAppAgentService = dataAppAgentService;
  }
  
  @RequestMapping(value = {"/dataapp/agent"}, method = {RequestMethod.POST})
  public Callable<ResponseEntity<Void>> create(@RequestParam("_c") String collectorId, @RequestParam("_i") String collectorInstanceId, @RequestHeader("X-Deployment-Secret") String deploymentSecret, @RequestHeader(value = "X-Plugin-Type", required = false) String defaultPluginType, @RequestBody(required = false) String createSpecJson) {
    return () -> {
        try {
          DataAppAgentId agentId = new DataAppAgentId(collectorId, collectorInstanceId, deploymentSecret, defaultPluginType);
          DataAppAgentCreateSpec agentCreateSpec = DataAppAgentRequestDeserializer.deserializeCreateSpec(createSpecJson);
          this._dataAppAgentService.createAgent(agentId, agentCreateSpec);
          _log.info(String.format("Successfully created data app agent for collector [%s] with instance [%s] and add spec [%s].", new Object[] { agentId.getCollectorId(), agentId.getCollectorInstanceId(), agentCreateSpec }));
          return new ResponseEntity(HttpStatus.CREATED);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.IncorrectFormat e) {
          _log.warn(String.format("Incorrect format for collectorId [%s] or collectorInstanceId [%s].", new Object[] { collectorId, collectorInstanceId }));
          return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.AlreadyExists e) {
          _log.warn(String.format("Data app agent for collector [%s] with instance [%s] already exists.", new Object[] { collectorId, collectorInstanceId }));
          return new ResponseEntity(HttpStatus.CONFLICT);
        } catch (IOException e) {
          _log.warn(String.format("Invalid data app agent create spec: %s.", new Object[] { createSpecJson }));
          return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } 
      };
  }
  
  @RequestMapping(value = {"/dataapp/agent"}, method = {RequestMethod.POST}, params = {"action=execute"})
  public Callable<ResponseEntity<String>> execute(@RequestParam("action") String action, @RequestParam("_c") String collectorId, @RequestParam("_i") String collectorInstanceId, @RequestHeader("X-Deployment-Secret") String deploymentSecret, @RequestHeader(value = "X-Plugin-Type", required = false) String pluginType, @RequestHeader(value = "X-Data-Type", required = false) String manifestDataType, @RequestHeader(value = "X-Object-Id", required = false) String objectId, @RequestHeader(value = "X-Use-Cache", required = false, defaultValue = "false") boolean useCache, @RequestBody(required = false) String jsonLdContextData) {
    String validJsonLdContextData = DataAppAgentRequestDeserializer.deserializeJsonLdContextData(jsonLdContextData);
    return () -> {
        DataAppAgentId agentId = null;
        try {
          agentId = new DataAppAgentId(collectorId, collectorInstanceId, deploymentSecret, pluginType);
          ExceptionsContextManager.createCurrentContext();
          DataAppAgent agent = this._dataAppAgentService.getAgent(agentId);
          PluginResult result = agent.execute(objectId, validJsonLdContextData, useCache);
          String resultJson = PluginResultUtil.convertPluginResultsToJson((result != null) ? Collections.<PluginResult>singletonList(result) : null);
          return new ResponseEntity(resultJson, HttpStatus.OK);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.IncorrectFormat e) {
          _log.warn(String.format("Incorrect format for collectorId [%s] or collectorInstanceId [%s].", new Object[] { collectorId, collectorInstanceId }));
          return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.NotFound e) {
          _log.warn(String.format("Data app agent for collector [%s] with instance [%s] does not exist.", new Object[] { collectorId, collectorInstanceId }));
          return new ResponseEntity("Did not find agent matching the provided parameters.", HttpStatus.NOT_FOUND);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.NotRunning e) {
          _log.warn(String.format("Data app agent for collector [%s] with instance [%s] is not running.", new Object[] { collectorId, collectorInstanceId }));
          return new ResponseEntity("Did not find agent matching the provided parameters.", HttpStatus.NOT_FOUND);
        } finally {
          if (agentId != null)
            ExceptionsContextManager.flushCurrentContext(agentId); 
        } 
      };
  }
  
  @RequestMapping(value = {"/dataapp/agent"}, method = {RequestMethod.PUT}, params = {"action=audit"})
  public Callable<ResponseEntity<DataAppAgentTypes.AuditResult>> audit() {
    return () -> {
        try {
          DataAppAgentId localDataAppAgentId = new DataAppAgentId("local_payload", "local-payload-audit-00000000-0000-0000-0000-000000000000", "2609f2dc4ad44bade362250581dfb0c7c314a036322175c9d2f3d6f2159034a5", "audit");
          AsyncCollectorDataAppAgent agent = (AsyncCollectorDataAppAgent)this._dataAppAgentService.getAgent(localDataAppAgentId);
          AgentStatus agentStatus = agent.getAgentStatus();
          if (this._localPayloadCollectionTaskFuture != null && this._localPayloadCollectionTaskFuture.isDone() && !shouldRetainLocalPayload(agentStatus.getLastCollectedTime()))
            this._localPayloadCollectionTaskFuture = null; 
          if (this._localPayloadCollectionTaskFuture == null) {
            String localManifestContent = new String(Files.readAllBytes(Paths.get(this._pathToLocalManifest, new String[0])), StandardCharsets.UTF_8);
            this._localPayloadCollectionTaskFuture = agent.collectAsync(localManifestContent, null, null);
            DataAppAgentTypes.AuditResult auditResult = buildAuditResult(DataAppAgentTypes.AuditStatus.STARTED, "{}");
            return new ResponseEntity(auditResult, HttpStatus.CREATED);
          } 
          if (!this._localPayloadCollectionTaskFuture.isDone()) {
            DataAppAgentTypes.AuditResult auditResult = buildAuditResult(DataAppAgentTypes.AuditStatus.PROCESSING, "{}");
            return new ResponseEntity(auditResult, HttpStatus.OK);
          } 
          String resultString = this._localPayloadCollectionTaskFuture.get();
          DataAppAgentTypes.AuditResult result = buildAuditResult(DataAppAgentTypes.AuditStatus.DONE, resultString);
          return new ResponseEntity(result, HttpStatus.OK);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.NotFound e) {
          _log.warn(String.format("Data app agent for collector [%s] with instance [%s] does not exist.", new Object[] { "local_payload", "local-payload-audit-00000000-0000-0000-0000-000000000000" }));
          DataAppAgentTypes.AuditResult result = buildAuditResult(DataAppAgentTypes.AuditStatus.ERROR, "Did not find agent matching the provided parameters.");
          return new ResponseEntity(result, HttpStatus.NOT_FOUND);
        } 
      };
  }
  
  @RequestMapping(value = {"/dataapp/agent"}, method = {RequestMethod.POST}, params = {"action=exportObfuscationMap"})
  public Callable<ResponseEntity<String>> exportObfuscationMap(@RequestParam("action") String action, @RequestParam("_c") String collectorId, @RequestParam("_i") String collectorInstanceId, @RequestHeader("X-Deployment-Secret") String deploymentSecret, @RequestHeader("X-Plugin-Type") String pluginType, @RequestHeader(value = "X-Object-Id", required = false) String objectId) {
    return () -> {
        DataAppAgent agent = null;
        try {
          DataAppAgentId agentId = new DataAppAgentId(collectorId, collectorInstanceId, deploymentSecret, pluginType);
          agent = this._dataAppAgentService.getAgent(agentId);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.IncorrectFormat e) {
          _log.warn(String.format("Incorrect format for collectorId [%s] or collectorInstanceId [%s].", new Object[] { collectorId, collectorInstanceId }));
          return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.NotFound|com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.NotRunning e) {
          return new ResponseEntity("Did not find agent matching the provided parameters.", HttpStatus.NOT_FOUND);
        } 
        Map<String, String> obuscationMap = null;
        if (agent != null && agent instanceof CollectorDataAppAgent)
          obuscationMap = ((CollectorDataAppAgent)agent).exportObfuscationMap(objectId); 
        String obuscationMapAsString = DataAppAgentResponseSerializer.serializeObfuscationMap(obuscationMap);
        return new ResponseEntity(obuscationMapAsString, HttpStatus.OK);
      };
  }
  
  @RequestMapping(value = {"/dataapp/agent"}, method = {RequestMethod.GET})
  public Callable<ResponseEntity<String>> get(@RequestParam("_c") String collectorId, @RequestParam("_i") String collectorInstanceId, @RequestHeader("X-Deployment-Secret") String deploymentSecret, @RequestHeader(value = "X-Plugin-Type", required = false) String pluginType) {
    return () -> {
        DataAppAgent agent = null;
        try {
          DataAppAgentId agentId = new DataAppAgentId(collectorId, collectorInstanceId, deploymentSecret, pluginType);
          agent = this._dataAppAgentService.getAgent(agentId);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.IncorrectFormat e) {
          _log.warn(String.format("Incorrect format for collectorId [%s] or collectorInstanceId [%s].", new Object[] { collectorId, collectorInstanceId }));
          return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.NotFound e) {
          return new ResponseEntity("Did not find agent matching the provided parameters.", HttpStatus.NOT_FOUND);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.NotRunning notRunning) {}
        AgentStatus agentStatus = null;
        if (agent != null) {
          agentStatus = agent.getAgentStatus();
        } else {
          agentStatus = new AgentStatus();
        } 
        String diagnosticData = DataAppAgentResponseSerializer.serializeDiagnosticData(agentStatus);
        return new ResponseEntity(diagnosticData, HttpStatus.OK);
      };
  }
  
  @RequestMapping(value = {"/dataapp/agent"}, method = {RequestMethod.DELETE})
  public Callable<ResponseEntity<Void>> delete(@RequestParam("_c") String collectorId, @RequestParam("_i") String collectorInstanceId, @RequestHeader("X-Deployment-Secret") String deploymentSecret, @RequestHeader(value = "X-Plugin-Type", required = false) String pluginType) {
    return () -> {
        try {
          DataAppAgentId agentId = new DataAppAgentId(collectorId, collectorInstanceId, deploymentSecret, pluginType);
          this._dataAppAgentService.destroyAgent(agentId);
          return new ResponseEntity(HttpStatus.OK);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.IncorrectFormat e) {
          _log.warn(String.format("Incorrect format for collectorId [%s] or collectorInstanceId [%s].", new Object[] { collectorId, collectorInstanceId }));
          return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.NotFound e) {
          return new ResponseEntity(HttpStatus.NOT_FOUND);
        } 
      };
  }
  
  public void setPathToLocalManifest(String pathToLocalManifest) {
    this._pathToLocalManifest = pathToLocalManifest;
  }
  
  public void setLocalPayloadRetentionPeriodMillis(long localPayloadRetentionPeriodMillis) {
    this._localPayloadRetentionPeriodMillis = localPayloadRetentionPeriodMillis;
  }
  
  private DataAppAgentTypes.AuditResult buildAuditResult(DataAppAgentTypes.AuditStatus started, String resultString) {
    DataAppAgentTypes.AuditResult auditResult = new DataAppAgentTypes.AuditResult();
    auditResult.setStatus(started);
    auditResult.setResult(resultString);
    return auditResult;
  }
  
  private boolean shouldRetainLocalPayload(long lastUploadTime) {
    long lastCollectedTime = lastUploadTime;
    if (lastUploadTime == 0L)
      lastCollectedTime = Instant.now().toEpochMilli(); 
    ZoneId UTC = ZoneId.of("UTC");
    LocalDateTime nowUTC = LocalDateTime.now(UTC);
    LocalDateTime retainPeriodEnd = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(lastCollectedTime + this._localPayloadRetentionPeriodMillis), UTC);
    return !nowUTC.isAfter(retainPeriodEnd);
  }
}

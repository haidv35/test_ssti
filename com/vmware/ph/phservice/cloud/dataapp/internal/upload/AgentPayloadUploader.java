package com.vmware.ph.phservice.cloud.dataapp.internal.upload;

import com.vmware.ph.client.api.commondataformat.PayloadEnvelope;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.CollectionTriggerType;
import com.vmware.ph.phservice.common.cdf.PayloadUploader;
import com.vmware.ph.phservice.common.cdf.internal.jsonld20.JSONObjectUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AgentPayloadUploader implements PayloadUploader {
  private static final Log _log = LogFactory.getLog(AgentPayloadUploader.class);
  
  static final String TRIGGER_TYPE_PACKAGE_TYPE_NAME = "collection.trigger.type";
  
  static final String DEPLOYMENT_SECRET_TYPE_NAME = "pa__deployment_secret";
  
  static final String DEPLOYMENT_SECRET_KEY = "secret";
  
  private final AgentPayloadUploadStrategy _payloadUploadStrategy;
  
  private final String _objectId;
  
  private final PluginTypeContext _pluginContext;
  
  private final String _deploymentSecret;
  
  private final CollectionTriggerType _collectionTriggerType;
  
  private final boolean _isDeploymentDataNeeded;
  
  private final boolean _shouldIncludeCollectionMetadata;
  
  private final ExecutorService _executorService;
  
  private static final List<String> COLLECTION_METADATA_TYPES = Arrays.asList(new String[] { "collection", "collection_summary", "pa__collection_event" });
  
  public AgentPayloadUploader(AgentPayloadUploadStrategy payloadUploadStrategy, String objectId, PluginTypeContext pluginContext, String deploymentSecret, CollectionTriggerType collectionTriggerType, boolean isDeploymentDataNeeded, ExecutorService executorService) {
    this(payloadUploadStrategy, objectId, pluginContext, deploymentSecret, collectionTriggerType, isDeploymentDataNeeded, true, executorService);
  }
  
  public AgentPayloadUploader(AgentPayloadUploadStrategy payloadUploadStrategy, String objectId, PluginTypeContext pluginContext, String deploymentSecret, CollectionTriggerType collectionTriggerType, boolean isDeploymentDataNeeded, boolean shouldIncludeCollectionMetadata, ExecutorService executorService) {
    this._payloadUploadStrategy = payloadUploadStrategy;
    this._objectId = objectId;
    this._pluginContext = pluginContext;
    this._deploymentSecret = deploymentSecret;
    this._collectionTriggerType = collectionTriggerType;
    this._isDeploymentDataNeeded = isDeploymentDataNeeded;
    this._shouldIncludeCollectionMetadata = shouldIncludeCollectionMetadata;
    this._executorService = executorService;
  }
  
  public boolean isEnabled() {
    return (this._payloadUploadStrategy != null);
  }
  
  public Future<?> uploadPayload(final Payload payload, final PayloadEnvelope envelope, String uploadId) {
    Future<?> result = this._executorService.submit(new Callable<Void>() {
          public Void call() throws PayloadUploader.PayloadUploadException {
            try {
              AgentPayloadUploader.this.uploadPayloadBlocking(payload, envelope);
              return null;
            } catch (AgentPayloadUploadException e) {
              throw new PayloadUploader.PayloadUploadException(e);
            } 
          }
        });
    return result;
  }
  
  private void uploadPayloadBlocking(Payload payload, PayloadEnvelope envelope) throws AgentPayloadUploadException {
    if (payload != null) {
      String agentCollectorId = envelope.getCollector().getCollectorId();
      String agentCollectorInstanceId = envelope.getCollector().getCollectorInstanceId();
      String agentCollectionId = payload.getCollectionId();
      List<JsonLd> jsonLdsForUpload = getJsonLdsForUpload(agentCollectorInstanceId, payload.getJsons());
      if (jsonLdsForUpload.isEmpty()) {
        _log.info("No Json-Lds to upload. Skipping upload operation.");
      } else {
        uploadJsonLds(agentCollectorId, agentCollectorInstanceId, agentCollectionId, jsonLdsForUpload);
      } 
    } 
  }
  
  private void uploadJsonLds(String agentCollectorId, String agentCollectorInstanceId, String agentCollectionId, Collection<JsonLd> jsonLds) throws AgentPayloadUploadException {
    _log.info(
        String.format("Uploading %d JsonLds, collectorId=%s, instanceId=%s, collectionId=%s.", new Object[] { Integer.valueOf(jsonLds.size()), agentCollectorId, agentCollectorInstanceId, agentCollectionId }));
    String jsonString = JSONObjectUtil.buildJsonArrayStringFromJsonLds(jsonLds);
    for (JsonLd jsonLd : jsonLds)
      jsonLd.setJsonString(null); 
    AgentJsonUploadRequest uploadRequest = new AgentJsonUploadRequest(agentCollectorId, agentCollectorInstanceId, agentCollectionId, this._deploymentSecret, this._objectId, this._pluginContext, jsonString);
    this._payloadUploadStrategy.upload(uploadRequest);
  }
  
  private List<JsonLd> getJsonLdsForUpload(String agentCollectorInstanceId, Collection<JsonLd> jsonLds) {
    if (jsonLds == null || jsonLds.isEmpty())
      return Collections.emptyList(); 
    List<JsonLd> jsonLdsForUpload = new ArrayList<>(jsonLds);
    if (!this._shouldIncludeCollectionMetadata) {
      _log.debug("Removing collection metadata resources.");
      jsonLdsForUpload = filterOutCollectionMetadataResources(jsonLds);
    } 
    if (this._collectionTriggerType != null) {
      _log.debug("Adding collection trigger type Json-Ld resource.");
      try {
        jsonLdsForUpload.add(constructCollectionTriggerTypeJsonLd());
      } catch (IOException e) {
        _log.error("Failed to create collection trigger type resource.", e);
      } 
    } 
    if (this._isDeploymentDataNeeded) {
      _log.debug("Adding deployment data Json-Ld resource.");
      try {
        jsonLdsForUpload.add(constructDeploymentSecretJsonLd(agentCollectorInstanceId));
      } catch (IOException e) {
        _log.error("Failed to create deployment data resource.", e);
      } 
    } 
    return jsonLdsForUpload;
  }
  
  private List<JsonLd> filterOutCollectionMetadataResources(Collection<JsonLd> jsonLds) {
    return (List<JsonLd>)jsonLds.stream()
      .filter(jsonLd -> {
          boolean isCollectionMetadata;
          try {
            String jsonLdType = jsonLd.getFieldValueAsString("@type");
            isCollectionMetadata = COLLECTION_METADATA_TYPES.contains(jsonLdType);
          } catch (IOException e) {
            _log.debug("No @type field in Json-Ld " + jsonLd + ". Resource is not considered metadata.");
            isCollectionMetadata = false;
          } 
          return !isCollectionMetadata;
        }).collect(Collectors.toList());
  }
  
  private JsonLd constructCollectionTriggerTypeJsonLd() throws IOException {
    return (new JsonLd.Builder())
      .withId(this._collectionTriggerType.name())
      .withType("collection.trigger.type")
      .build();
  }
  
  private JsonLd constructDeploymentSecretJsonLd(String agentCollectorInstanceId) throws IOException {
    return (new JsonLd.Builder())
      .withId(agentCollectorInstanceId)
      .withType("pa__deployment_secret")
      .withProperty("secret", this._deploymentSecret)
      .build();
  }
}

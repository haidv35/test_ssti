package com.vmware.ph.phservice.collector.internal.cdf;

import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

public class PayloadUtil {
  public static final String PERF_DATA_RESOURCE_TYPE = "pa__collection_event";
  
  private static final String OPERATION_NAME_PROPERTY_NAME = "name";
  
  private static final String ELAPSED_MILLIS_PROPERTY_NAME = "elapsed_ms";
  
  private static final String COLLECTION_ID_PROPERTY_NAME = "collection_id";
  
  private static final String PAGE_PROPERTY_NAME = "page";
  
  public static Iterable<CollectedPayload> buildPerfData(long elapsedNano, String operationName, String collectionId) throws IOException {
    JsonLd perfData = buildPerfData(elapsedNano, operationName, null, collectionId);
    Payload.Builder payload = (new Payload.Builder()).add(perfData);
    CollectedPayload cp = (new CollectedPayload.Builder()).setPayload(payload).build();
    return Collections.singleton(cp);
  }
  
  public static JsonLd buildPerfData(long elapsedNano, String operationName, Integer page, String collectionId) throws IOException {
    String uuid = UUID.randomUUID().toString();
    long elapsedMillis = TimeUnit.MILLISECONDS.convert(elapsedNano, TimeUnit.NANOSECONDS);
    JsonLd.Builder jsonLdBuilder = (new JsonLd.Builder()).withId(uuid).withType("pa__collection_event").withProperty("name", operationName).withProperty("elapsed_ms", Long.valueOf(elapsedMillis)).withProperty("collection_id", collectionId);
    if (page != null)
      jsonLdBuilder.withProperty("page", page); 
    return jsonLdBuilder.build();
  }
  
  public static JsonLd buildUpdatedPerfData(JsonLd perfData, long extraElapsedNano) {
    JSONObject perfDataJsonObject = perfData.getJson();
    long extraElapsedMillis = TimeUnit.MILLISECONDS.convert(extraElapsedNano, TimeUnit.NANOSECONDS);
    long oldElapsedMillis = perfDataJsonObject.getLong("elapsed_ms");
    perfDataJsonObject.put("elapsed_ms", oldElapsedMillis + extraElapsedMillis);
    return new JsonLd(perfDataJsonObject.toString());
  }
}

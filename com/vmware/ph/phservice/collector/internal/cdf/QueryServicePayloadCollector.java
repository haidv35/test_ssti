package com.vmware.ph.phservice.collector.internal.cdf;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.vmware.cis.data.api.QueryService;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.NamedQueryResultSetMapping;
import com.vmware.ph.phservice.collector.internal.data.NamedQuery;
import com.vmware.ph.phservice.collector.internal.data.NamedQueryResultSet;
import com.vmware.ph.phservice.collector.internal.data.QueryServiceCollector;
import com.vmware.ph.phservice.collector.internal.manifest.Manifest;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationRule;
import com.vmware.ph.phservice.common.internal.obfuscation.Obfuscator;
import com.vmware.ph.phservice.provider.common.internal.Context;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryServicePayloadCollector {
  public static final String QUERY_NAME_CONTEXT_KEY = "queryName";
  
  private static final Logger _logger = LoggerFactory.getLogger(QueryServicePayloadCollector.class);
  
  private final QueryServiceCollector<CollectedPayload> _queryServiceCollector;
  
  private final Obfuscator _obfuscator;
  
  public QueryServicePayloadCollector(Obfuscator obfuscator) {
    this(new QueryServiceCollector<>(), obfuscator);
  }
  
  public QueryServicePayloadCollector(QueryServiceCollector<CollectedPayload> queryServiceCollector, Obfuscator obfuscator) {
    this._queryServiceCollector = queryServiceCollector;
    this._obfuscator = obfuscator;
  }
  
  public Iterable<CollectedPayload> collect(Manifest manifest, QueryService queryService, Context context, int pageSize) {
    long startTimeNano = System.nanoTime();
    NamedQuery[] queries = manifest.getQueries();
    NamedQueryResultSetMapping<Payload> payloadMapping = manifest.getMapping();
    NamedQueryResultSetToCollectedPayloadMapping collectedPayloadMapping = new NamedQueryResultSetToCollectedPayloadMapping(payloadMapping);
    Context modifiedContext = new Context(context);
    Iterable<CollectedPayload> result = this._queryServiceCollector.collect(queries, collectedPayloadMapping, queryService, modifiedContext, pageSize);
    if (this._obfuscator != null) {
      List<ObfuscationRule> obfuscationRules = manifest.getObfuscationRules();
      ObfuscationFunction obfuscationFunction = new ObfuscationFunction(this._obfuscator, obfuscationRules);
      result = Iterables.transform(result, obfuscationFunction);
    } 
    Iterable<CollectedPayload> perfData = buildCollectionPerfData(startTimeNano, context.getCollectionId());
    result = Iterables.concat(result, perfData);
    return result;
  }
  
  private Iterable<CollectedPayload> buildCollectionPerfData(long startTimeNano, String collectionId) {
    long endTimeNano = System.nanoTime();
    String collectorName = getClass().getSimpleName();
    Iterable<CollectedPayload> perfData = Collections.emptyList();
    try {
      perfData = PayloadUtil.buildPerfData(endTimeNano - startTimeNano, "QueryServiceCdfCollector", collectionId);
    } catch (IOException e) {
      _logger.error("Failed to build perf data resource. No perf data will be available for collector {}.", collectorName);
    } 
    return perfData;
  }
  
  static class NamedQueryResultSetToCollectedPayloadMapping implements NamedQueryResultSetMapping<CollectedPayload> {
    private final NamedQueryResultSetMapping<Payload> _resultSetToPayloadMapping;
    
    public NamedQueryResultSetToCollectedPayloadMapping(NamedQueryResultSetMapping<Payload> resultSetToPayloadMapping) {
      this._resultSetToPayloadMapping = resultSetToPayloadMapping;
    }
    
    public CollectedPayload map(NamedQueryResultSet namedQueryResultSet, Context context) {
      Context modifiedContext = new Context(context);
      modifiedContext.put("queryName", namedQueryResultSet
          
          .getQueryName());
      Payload payload = this._resultSetToPayloadMapping.map(namedQueryResultSet, modifiedContext);
      long startTimeNanos = namedQueryResultSet.getStartTimeNanos();
      long elapsedNano = System.nanoTime() - startTimeNanos;
      String queryName = namedQueryResultSet.getQueryName();
      int currentPageIndex = namedQueryResultSet.getCurrPageIndex();
      Payload.Builder mergedPayloadBuilder = new Payload.Builder();
      mergedPayloadBuilder.add(payload);
      try {
        JsonLd perfData = PayloadUtil.buildPerfData(elapsedNano, queryName, 

            
            Integer.valueOf(currentPageIndex), context
            .getCollectionId());
        mergedPayloadBuilder.add(perfData);
      } catch (IOException e) {
        QueryServicePayloadCollector._logger.error("Failed to build perf data resource. No perf data will be available for query {}.", queryName);
      } 
      CollectedPayload.Builder collectedPayloadBuilder = (new CollectedPayload.Builder()).setPayload(mergedPayloadBuilder);
      collectedPayloadBuilder.setLastInBatch((namedQueryResultSet
          .getCurrentQueryPageSize() != null || namedQueryResultSet
          .getNextQueryPageSize() != null));
      CollectedPayload collectedPayload = collectedPayloadBuilder.build();
      return collectedPayload;
    }
    
    public boolean isQuerySupported(String queryName) {
      return this._resultSetToPayloadMapping.isQuerySupported(queryName);
    }
  }
  
  static class ObfuscationFunction implements Function<CollectedPayload, CollectedPayload> {
    private final Obfuscator _obfuscator;
    
    private final List<ObfuscationRule> _obfuscationRules;
    
    private Function<Long, Long> _obfuscationElapsedNanoComputer;
    
    public ObfuscationFunction(Obfuscator obfuscator, List<ObfuscationRule> obfuscationRules) {
      this._obfuscator = obfuscator;
      this._obfuscationRules = obfuscationRules;
    }
    
    ObfuscationFunction(Obfuscator obfuscator, List<ObfuscationRule> obfuscationRules, Function<Long, Long> obfuscationElapsedNanoComputer) {
      this._obfuscator = obfuscator;
      this._obfuscationRules = obfuscationRules;
      this._obfuscationElapsedNanoComputer = obfuscationElapsedNanoComputer;
    }
    
    public CollectedPayload apply(CollectedPayload inputCollectedPayload) {
      Payload inputPayload = inputCollectedPayload.getPayload();
      obfuscatePayload(inputPayload);
      return inputCollectedPayload;
    }
    
    void obfuscatePayload(Payload payload) {
      ListIterator<JsonLd> jsonLdIterator = payload.getJsons().listIterator();
      long startObfuscationTimeNanos = System.nanoTime();
      while (jsonLdIterator.hasNext()) {
        JsonLd updatedJsonLd, jsonLd = jsonLdIterator.next();
        if (jsonLdIterator.hasNext()) {
          updatedJsonLd = (JsonLd)this._obfuscator.obfuscate(jsonLd, this._obfuscationRules);
        } else {
          long obfuscationElapsedNano;
          if (this._obfuscationElapsedNanoComputer == null) {
            obfuscationElapsedNano = System.nanoTime() - startObfuscationTimeNanos;
          } else {
            obfuscationElapsedNano = ((Long)this._obfuscationElapsedNanoComputer.apply(Long.valueOf(startObfuscationTimeNanos))).longValue();
          } 
          updatedJsonLd = PayloadUtil.buildUpdatedPerfData(jsonLd, obfuscationElapsedNano);
        } 
        jsonLdIterator.set(updatedJsonLd);
      } 
    }
  }
}

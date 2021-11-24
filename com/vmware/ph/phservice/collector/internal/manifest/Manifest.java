package com.vmware.ph.phservice.collector.internal.manifest;

import com.vmware.cis.data.api.Query;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.phservice.collector.internal.NamedQueryResultSetMapping;
import com.vmware.ph.phservice.collector.internal.data.NamedQuery;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationRule;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import java.util.List;

public class Manifest {
  private final NamedQuery[] _namedQueries;
  
  private final NamedQueryResultSetMapping<Payload> _mapping;
  
  private final List<ObfuscationRule> _obfuscationRules;
  
  private final int _recommendedPageSize;
  
  private Manifest(NamedQuery[] namedQueries, NamedQueryResultSetMapping<Payload> mapping, List<ObfuscationRule> obfuscationRules, int recommendedPageSize) {
    this._namedQueries = namedQueries;
    this._mapping = mapping;
    this._obfuscationRules = obfuscationRules;
    this._recommendedPageSize = recommendedPageSize;
  }
  
  public NamedQuery[] getQueries() {
    return this._namedQueries;
  }
  
  public NamedQueryResultSetMapping<Payload> getMapping() {
    return this._mapping;
  }
  
  public List<ObfuscationRule> getObfuscationRules() {
    return this._obfuscationRules;
  }
  
  public int getRecommendedPageSize() {
    return this._recommendedPageSize;
  }
  
  public static class Builder {
    private final NamedQuery[] _namedQueries;
    
    private NamedQueryResultSetMapping<Payload> _mapping;
    
    private List<ObfuscationRule> _obfuscationRules;
    
    private Object _contextData;
    
    private int _recommendedPageSize;
    
    public Builder(NamedQuery[] namedQueries) {
      this._namedQueries = namedQueries;
    }
    
    public static Builder forManifest(Manifest manifest) {
      return (new Builder(manifest.getQueries()))
        .withMapping(manifest.getMapping())
        .withObfuscationRules(manifest.getObfuscationRules())
        .withRecommendedPageSize(manifest.getRecommendedPageSize());
    }
    
    public static Builder forManifestWithNewQueries(Manifest manifest, NamedQuery[] namedQueries) {
      return (new Builder(namedQueries))
        .withMapping(manifest.getMapping())
        .withObfuscationRules(manifest.getObfuscationRules())
        .withRecommendedPageSize(manifest.getRecommendedPageSize());
    }
    
    public static Builder forQueries(NamedQuery[] namedQueries) {
      return new Builder(namedQueries);
    }
    
    public Builder withMapping(NamedQueryResultSetMapping<Payload> mapping) {
      this._mapping = mapping;
      return this;
    }
    
    public Builder withObfuscationRules(List<ObfuscationRule> obfuscationRules) {
      this._obfuscationRules = obfuscationRules;
      return this;
    }
    
    public Builder withRecommendedPageSize(int recommendedPageSize) {
      this._recommendedPageSize = recommendedPageSize;
      return this;
    }
    
    public Builder withContext(Object contextData) {
      this._contextData = contextData;
      return this;
    }
    
    public Manifest build() {
      NamedQuery[] namedQueriesWithContext = addContextToNamedQueries(this._namedQueries, this._contextData);
      Manifest manifest = new Manifest(namedQueriesWithContext, this._mapping, this._obfuscationRules, this._recommendedPageSize);
      return manifest;
    }
    
    private static NamedQuery[] addContextToNamedQueries(NamedQuery[] namedQueries, Object contextData) {
      if (contextData == null)
        return namedQueries; 
      NamedQuery[] namedQueriesWithContext = new NamedQuery[namedQueries.length];
      for (int i = 0; i < namedQueries.length; i++)
        namedQueriesWithContext[i] = 
          applyContextToNamedQuery(namedQueries[i], contextData, "@type"); 
      return namedQueriesWithContext;
    }
    
    private static NamedQuery applyContextToNamedQuery(NamedQuery namedQuery, Object contextData, String contextPropertyName) {
      Query query = namedQuery.getQuery();
      Query queryWithContext = QueryContextUtil.addContextToQueryFilter(query, contextData);
      NamedQuery namedQueryWithContext = null;
      if (!query.equals(queryWithContext)) {
        namedQueryWithContext = new NamedQuery(queryWithContext, namedQuery.getName(), namedQuery.getCpuThreshold(), namedQuery.getMemoryThreshold());
      } else {
        namedQueryWithContext = namedQuery;
      } 
      return namedQueryWithContext;
    }
  }
}

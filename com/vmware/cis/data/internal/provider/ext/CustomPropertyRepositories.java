package com.vmware.cis.data.internal.provider.ext;

import com.vmware.cis.data.internal.provider.ext.alias.AliasPropertyRepository;
import com.vmware.cis.data.internal.provider.ext.derived.DerivedPropertyRepository;
import com.vmware.cis.data.internal.provider.ext.predicate.PredicatePropertyRepository;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyRepository;
import java.util.Collection;

public class CustomPropertyRepositories {
  private final AliasPropertyRepository _aliasPropertyRepository;
  
  private final DerivedPropertyRepository _derivedPropertyRepository;
  
  private final PredicatePropertyRepository _predicatePropertyRepository;
  
  private final RelatedPropertyRepository _relatedPropertyRepository;
  
  public CustomPropertyRepositories(Collection<Class<?>> queryModels) {
    assert queryModels != null;
    this._aliasPropertyRepository = new AliasPropertyRepository(queryModels);
    this._derivedPropertyRepository = new DerivedPropertyRepository(queryModels);
    this._predicatePropertyRepository = new PredicatePropertyRepository(queryModels);
    this._relatedPropertyRepository = new RelatedPropertyRepository(queryModels);
  }
  
  public AliasPropertyRepository getAliasPropertyRepository() {
    return this._aliasPropertyRepository;
  }
  
  public DerivedPropertyRepository getDerivedPropertyRepository() {
    return this._derivedPropertyRepository;
  }
  
  public PredicatePropertyRepository getPredicatePropertyRepository() {
    return this._predicatePropertyRepository;
  }
  
  public RelatedPropertyRepository getRelatedPropertyRepository() {
    return this._relatedPropertyRepository;
  }
}

package com.vmware.cis.data.internal.provider;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;

public final class QueryClauseAnalyzer {
  private final ProviderBySchemaLookup _providerLookup;
  
  public QueryClauseAnalyzer(ProviderBySchemaLookup providerLookup) {
    assert providerLookup != null;
    this._providerLookup = providerLookup;
  }
  
  public DataProvider getSortProvider(List<SortCriterion> sortCriteria) {
    assert sortCriteria != null;
    assert !sortCriteria.isEmpty();
    Set<String> sortProperties = gatherPropertiesFromSort(sortCriteria);
    if (sortProperties.isEmpty())
      return null; 
    DataProvider provider = this._providerLookup.getProviderForProperties(sortProperties);
    if (provider == null)
      throw new UnsupportedOperationException("Could not find a single data provider for sorting criteria: " + sortCriteria); 
    return provider;
  }
  
  public DataProvider getQueryProvider(Query query) {
    assert query != null;
    Set<String> properties = new HashSet<>();
    properties.addAll(gatherPropertiesFromFilter(query.getFilter()));
    Set<String> sortProperties = gatherPropertiesFromSort(query.getSortCriteria());
    if (!properties.isEmpty()) {
      properties.addAll(sortProperties);
      return this._providerLookup.getProviderForProperties(properties);
    } 
    DataProvider fromProvider = this._providerLookup.getProviderForModels(query.getResourceModels());
    if (fromProvider == null || sortProperties.isEmpty())
      return fromProvider; 
    DataProvider sortProvider = this._providerLookup.getProviderForProperties(sortProperties);
    if (sortProvider == fromProvider)
      return fromProvider; 
    return null;
  }
  
  public Map<DataProvider, List<String>> getPropertiesByProvider(Collection<String> properties) {
    assert properties != null;
    assert !properties.isEmpty();
    Map<DataProvider, List<String>> propertiesByProvider = new HashMap<>();
    boolean addType = false;
    for (String property : properties) {
      if (PropertyUtil.isType(property)) {
        addType = true;
        continue;
      } 
      DataProvider provider = this._providerLookup.getProviderForProperty(property);
      addPropertyToProvider(propertiesByProvider, provider, property);
    } 
    if (addType)
      for (List<String> providerProperties : propertiesByProvider.values())
        providerProperties.add("@type");  
    return propertiesByProvider;
  }
  
  public Map<DataProvider, List<PropertyPredicate>> getPredicatesByProvider(Query query) {
    Filter filter = query.getFilter();
    if (filter == null)
      return Collections.emptyMap(); 
    Map<DataProvider, List<PropertyPredicate>> criteriaByProvider = new HashMap<>();
    List<PropertyPredicate> specialPredicates = new ArrayList<>();
    for (PropertyPredicate predicate : filter.getCriteria()) {
      if (isSpecialProperty(predicate.getProperty())) {
        specialPredicates.add(predicate);
        continue;
      } 
      DataProvider provider = this._providerLookup.getProviderForProperty(predicate
          .getProperty());
      List<PropertyPredicate> providerCriteria = criteriaByProvider.get(provider);
      if (providerCriteria == null) {
        providerCriteria = new ArrayList<>();
        criteriaByProvider.put(provider, providerCriteria);
      } 
      providerCriteria.add(predicate);
    } 
    if (criteriaByProvider.isEmpty())
      for (String model : query.getResourceModels()) {
        DataProvider dataProvider = this._providerLookup.getProviderForModel(model);
        criteriaByProvider.put(dataProvider, new ArrayList<>());
      }  
    addSpecialPropertyPredicateIfRequested(criteriaByProvider, specialPredicates);
    return criteriaByProvider;
  }
  
  public List<String> filterModelsForProvider(Collection<String> models, DataProvider provider) {
    Validate.notEmpty(models);
    List<String> filteredModels = new ArrayList<>();
    for (String model : models) {
      DataProvider currentProvider = this._providerLookup.getProviderForModel(model);
      if (currentProvider == provider)
        filteredModels.add(model); 
    } 
    return filteredModels;
  }
  
  private void addSpecialPropertyPredicateIfRequested(Map<DataProvider, List<PropertyPredicate>> criteriaByProvider, List<PropertyPredicate> specialPredicates) {
    if (specialPredicates == null || specialPredicates.isEmpty())
      return; 
    if (criteriaByProvider.isEmpty())
      throw new UnsupportedOperationException("Filtering only by model key or type is not supported"); 
    for (List<PropertyPredicate> criteria : criteriaByProvider.values())
      criteria.addAll(specialPredicates); 
  }
  
  public static String getQueryResourceModel(Query query) {
    assert query.getResourceModels().size() <= 1;
    return query.getResourceModels().iterator().next();
  }
  
  public static List<String> gatherPropertiesForSingleResourceModel(Query query) {
    if (query == null)
      return Collections.emptyList(); 
    List<String> properties = query.getProperties();
    Set<String> queryProperties = new HashSet<>(properties.size());
    for (String property : properties) {
      if (!isSpecialProperty(property))
        queryProperties.add(property); 
    } 
    return new ArrayList<>(queryProperties);
  }
  
  public static Set<String> qualifyPropertiesForResourceModel(Collection<String> properties, String resourceModel) {
    Set<String> qualifiedProperties = new HashSet<>(properties.size());
    for (String property : properties)
      qualifiedProperties.add(
          QualifiedProperty.forModelAndSimpleProperty(resourceModel, property)
          .toString()); 
    return new HashSet<>(qualifiedProperties);
  }
  
  public static Set<String> gatherPropertiesFromFilter(Filter filter) {
    if (filter == null)
      return Collections.emptySet(); 
    List<PropertyPredicate> predicates = filter.getCriteria();
    Set<String> filterProperties = new HashSet<>(predicates.size());
    for (PropertyPredicate predicate : predicates) {
      if (!isSpecialProperty(predicate.getProperty()))
        filterProperties.add(predicate.getProperty()); 
    } 
    return filterProperties;
  }
  
  public static Set<String> gatherPropertiesFromSort(List<SortCriterion> sortCriteria) {
    if (sortCriteria == null)
      return Collections.emptySet(); 
    Set<String> sortProperties = new HashSet<>(sortCriteria.size());
    for (SortCriterion sortCriterion : sortCriteria) {
      if (!isSpecialProperty(sortCriterion.getProperty()))
        sortProperties.add(sortCriterion.getProperty()); 
    } 
    return sortProperties;
  }
  
  public static boolean isSpecialProperty(String property) {
    return (PropertyUtil.isSpecialProperty(property) || 
      PropertyUtil.isInstanceUuid(property));
  }
  
  private void addPropertyToProvider(Map<DataProvider, List<String>> propertiesByProvider, DataProvider provider, String property) {
    List<String> providerProperties = propertiesByProvider.get(provider);
    if (providerProperties == null) {
      providerProperties = new ArrayList<>();
      propertiesByProvider.put(provider, providerProperties);
    } 
    providerProperties.add(property);
  }
}

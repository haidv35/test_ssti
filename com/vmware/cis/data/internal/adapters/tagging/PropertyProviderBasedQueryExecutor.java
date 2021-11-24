package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.merge.DefaultItemComparator;
import com.vmware.cis.data.internal.provider.util.filter.FilterEvaluator;
import com.vmware.cis.data.internal.provider.util.property.PropertyByName;
import com.vmware.cis.data.internal.util.PropertyUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PropertyProviderBasedQueryExecutor {
  private final PropertyProvider _data;
  
  private final FilteringPropertyProvider _search;
  
  public PropertyProviderBasedQueryExecutor(PropertyProvider data) {
    this(data, new FilteringPropertyProvider() {
          public List<?> getKeys(PropertyPredicate predicate) {
            return null;
          }
        });
  }
  
  public PropertyProviderBasedQueryExecutor(PropertyProvider data, FilteringPropertyProvider search) {
    assert data != null;
    assert search != null;
    this._data = data;
    this._search = search;
  }
  
  public ResultSet executeQuery(Query query) {
    ResultSet.Builder resultBuilder;
    assert query != null;
    List<?> filteredKeys = filter(query.getFilter());
    if (query.getLimit() != 0) {
      List<?> sortedKeys = sort(filteredKeys, query.getSortCriteria());
      List<?> pagedKeys = page(sortedKeys, query.getOffset(), query
          .getLimit());
      resultBuilder = ResultSet.Builder.properties(query.getProperties());
      List<List<Object>> items = getPropertyValues(pagedKeys, query.getProperties());
      assert pagedKeys.size() == items.size();
      Iterator<List<Object>> itemIterator = items.iterator();
      for (Object key : pagedKeys) {
        List<Object> propertyValues = itemIterator.next();
        resultBuilder.item(key, propertyValues);
      } 
    } else {
      resultBuilder = ResultSet.Builder.properties(new String[0]);
    } 
    return resultBuilder
      .totalCount(query.getWithTotalCount() ? Integer.valueOf(filteredKeys.size()) : null)
      .build();
  }
  
  private List<?> filter(Filter filter) {
    List<?> keys = filterByNative(filter);
    if (keys != null)
      return keys; 
    List<String> propertiesInInterpretableFilter = getPropsAndKey(filter);
    List<List<Object>> allItems = this._data.list(propertiesInInterpretableFilter);
    return filterByInterpreted(filter, propertiesInInterpretableFilter, allItems);
  }
  
  private List<?> filterByNative(Filter filter) {
    if (filter == null)
      return null; 
    if (filter.getCriteria().size() > 1 && filter.getOperator().equals(LogicalOperator.OR))
      return null; 
    if (filter.getCriteria().size() == 1) {
      PropertyPredicate predicate = filter.getCriteria().get(0);
      return filterByNative(predicate);
    } 
    return filterByNativeInConjunction(filter.getCriteria());
  }
  
  private List<?> filterByNativeInConjunction(List<PropertyPredicate> predicates) {
    assert predicates != null;
    List<?> keys = null;
    List<PropertyPredicate> remainingPredicates = new ArrayList<>(predicates.size());
    for (PropertyPredicate predicate : predicates) {
      if (keys != null) {
        remainingPredicates.add(predicate);
        continue;
      } 
      keys = filterByNative(predicate);
      if (keys == null)
        remainingPredicates.add(predicate); 
    } 
    if (keys == null)
      return null; 
    Filter filter = new Filter(remainingPredicates, LogicalOperator.AND);
    List<String> propertiesInInterpretableFilter = getPropsAndKey(filter);
    List<List<Object>> allItems = this._data.get(keys, propertiesInInterpretableFilter);
    return filterByInterpreted(filter, propertiesInInterpretableFilter, allItems);
  }
  
  private List<?> filterByNative(PropertyPredicate predicate) {
    assert predicate != null;
    if (!"@modelKey".equals(predicate.getProperty()) || 
      !operatorIsEquality(predicate.getOperator()))
      return this._search.getKeys(predicate); 
    Collection<Object> comparableCollection = getComparableCollection(predicate);
    List<?> modelKeys = new ArrayList(comparableCollection);
    return getModelKeys(this._data.get(modelKeys, PropertyUtil.PROPERTY_LIST_MODEL_KEY), PropertyUtil.PROPERTY_LIST_MODEL_KEY);
  }
  
  private List<?> filterByInterpreted(Filter filter, List<String> propertiesInFilter, List<List<Object>> items) {
    assert propertiesInFilter != null;
    assert items != null;
    if (filter == null)
      return getModelKeys(items, propertiesInFilter); 
    if (items.isEmpty())
      return Collections.emptyList(); 
    List<List<Object>> filtered = new ArrayList<>(items.size());
    final Map<String, Integer> indexByProperty = new HashMap<>(propertiesInFilter.size());
    int i = 0;
    for (String property : propertiesInFilter)
      indexByProperty.put(property, Integer.valueOf(i++)); 
    for (List<Object> item : items) {
      PropertyByName valueByName = new PropertyByName() {
          public Object getValue(String propertyName) {
            Integer index = (Integer)indexByProperty.get(propertyName);
            if (index == null)
              return null; 
            return item.get(index.intValue());
          }
        };
      if (FilterEvaluator.eval(filter, valueByName))
        filtered.add(item); 
    } 
    return getModelKeys(filtered, propertiesInFilter);
  }
  
  private static Collection<Object> getComparableCollection(PropertyPredicate predicate) {
    assert predicate != null;
    if (PropertyPredicate.ComparisonOperator.IN.equals(predicate.getOperator())) {
      assert predicate.getComparableValue() instanceof Collection;
      Collection<Object> comparableCollection = (Collection<Object>)predicate.getComparableValue();
      return comparableCollection;
    } 
    return Collections.singletonList(predicate.getComparableValue());
  }
  
  private static List<String> getPropsAndKey(Filter filter) {
    if (filter == null)
      return Collections.singletonList("@modelKey"); 
    Set<String> properties = new LinkedHashSet<>(filter.getCriteria().size() + 1);
    properties.add("@modelKey");
    for (PropertyPredicate predicate : filter.getCriteria())
      properties.add(predicate.getProperty()); 
    return new ArrayList<>(properties);
  }
  
  private static List<?> getModelKeys(List<List<Object>> items, List<String> properties) {
    int modelKeyIndex = properties.indexOf("@modelKey");
    assert modelKeyIndex >= 0;
    List<Object> modelKeys = new ArrayList(items.size());
    for (List<Object> item : items) {
      Object modelKey = item.get(modelKeyIndex);
      modelKeys.add(modelKey);
    } 
    return modelKeys;
  }
  
  private static List<?> getModelKeysOfItems(List<ResourceItem> items, List<String> properties) {
    int modelKeyIndex = properties.indexOf("@modelKey");
    assert modelKeyIndex >= 0;
    List<Object> modelKeys = new ArrayList(items.size());
    for (ResourceItem item : items) {
      Object modelKey = item.getPropertyValues().get(modelKeyIndex);
      modelKeys.add(modelKey);
    } 
    return modelKeys;
  }
  
  private List<?> sort(List<?> ids, List<SortCriterion> sortCriteria) {
    assert ids != null;
    assert sortCriteria != null;
    if (ids.isEmpty() || sortCriteria.isEmpty())
      return ids; 
    List<SortCriterion> typeFreeSortCriteria = removeTypeFromSort(sortCriteria);
    List<String> propertiesInSort = getPropsAndKey(typeFreeSortCriteria);
    List<List<Object>> items = this._data.get(ids, propertiesInSort);
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(propertiesInSort);
    for (int index = 0; index < ids.size(); index++) {
      Object id = ids.get(index);
      List<Object> propertyValues = items.get(index);
      resultBuilder.item(id, propertyValues);
    } 
    ResultSet resultSet = resultBuilder.build();
    List<ResourceItem> resourceItems = new ArrayList<>(resultSet.getItems());
    Collections.sort(resourceItems, new DefaultItemComparator(propertiesInSort, typeFreeSortCriteria));
    return getModelKeysOfItems(resourceItems, propertiesInSort);
  }
  
  private static List<SortCriterion> removeTypeFromSort(List<SortCriterion> sortCriteria) {
    List<SortCriterion> newSortCriteria = new ArrayList<>(sortCriteria.size());
    for (SortCriterion sortCriterion : sortCriteria) {
      if (PropertyUtil.isType(sortCriterion.getProperty()))
        continue; 
      newSortCriteria.add(sortCriterion);
    } 
    return newSortCriteria;
  }
  
  private static List<String> getPropsAndKey(List<SortCriterion> sortCriteria) {
    assert sortCriteria != null;
    Set<String> properties = new LinkedHashSet<>(sortCriteria.size() + 1);
    properties.add("@modelKey");
    for (SortCriterion sortCriterion : sortCriteria)
      properties.add(sortCriterion.getProperty()); 
    return new ArrayList<>(properties);
  }
  
  private static <T> List<T> page(List<T> list, int offset, int limit) {
    assert list != null;
    assert offset >= 0;
    if (list.isEmpty())
      return list; 
    if (limit == 0)
      return Collections.emptyList(); 
    if (offset >= list.size())
      return Collections.emptyList(); 
    if (limit < 0)
      return list.subList(offset, list.size()); 
    int lastIndex = offset + limit;
    if (lastIndex < 0 || lastIndex >= list.size())
      return list.subList(offset, list.size()); 
    return list.subList(offset, lastIndex);
  }
  
  private List<List<Object>> getPropertyValues(List<?> modelKeys, List<String> properties) {
    assert modelKeys != null;
    assert properties != null;
    if (modelKeys.isEmpty())
      return Collections.emptyList(); 
    if (properties.isEmpty())
      return Collections.nCopies(modelKeys.size(), Collections.emptyList()); 
    return this._data.get(modelKeys, properties);
  }
  
  private static boolean operatorIsEquality(PropertyPredicate.ComparisonOperator operator) {
    return (operator == PropertyPredicate.ComparisonOperator.EQUAL || operator == PropertyPredicate.ComparisonOperator.IN);
  }
}

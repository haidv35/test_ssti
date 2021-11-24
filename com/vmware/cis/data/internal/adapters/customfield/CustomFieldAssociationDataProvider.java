package com.vmware.cis.data.internal.adapters.customfield;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.util.filter.OperatorLikeEvaluator;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CustomFieldAssociationDataProvider implements DataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(CustomFieldAssociationDataProvider.class);
  
  private static final boolean EXACTLY = true;
  
  private static final boolean CONTAINS = false;
  
  private static final String CUSTOM_FIELD_ASSOCIATION_KEY = "NOT_USED";
  
  private final CustomFieldRepository _customFields;
  
  public CustomFieldAssociationDataProvider(Client vlsiClient) {
    this(new VimCustomFieldsManagerRepository(vlsiClient));
  }
  
  CustomFieldAssociationDataProvider(CustomFieldRepository customFields) {
    Validate.notNull(customFields);
    this._customFields = customFields;
  }
  
  public ResultSet executeQuery(Query query) {
    validateQuery(query);
    Filter filter = query.getFilter();
    Collection<ManagedObjectReference> entities = getEntitiesWithCustomFieldAndValue(filter);
    return createResultSet(entities);
  }
  
  public QuerySchema getSchema() {
    Map<String, QuerySchema.PropertyInfo> props = new LinkedHashMap<>();
    props.put("CustomFieldAssociation/name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    props.put("CustomFieldAssociation/value", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    props.put("CustomFieldAssociation/entity", QuerySchema.PropertyInfo.forNonFilterableProperty());
    return QuerySchema.forProperties(props);
  }
  
  public String toString() {
    return "CustomFieldAssociationDataProviderConnection(" + this._customFields + ")";
  }
  
  private Collection<ManagedObjectReference> getEntitiesWithCustomFieldAndValue(Filter filter) {
    assert filter != null;
    PropertyPredicate namePredicate = getCaseInsensitivePredicate(filter, "CustomFieldAssociation/name");
    PropertyPredicate valuePredicate = getCaseInsensitivePredicate(filter, "CustomFieldAssociation/value");
    PropertyPredicate.ComparisonOperator nameOp = namePredicate.getOperator();
    PropertyPredicate.ComparisonOperator valueOp = valuePredicate.getOperator();
    String name = getComparableString(namePredicate);
    String value = getComparableString(valuePredicate);
    if (_logger.isDebugEnabled())
      _logger.debug("Searching entities by custom field name {} '{}' and custom field value {} '{}", new Object[] { nameOp, name, valueOp, value }); 
    if (PropertyPredicate.ComparisonOperator.EQUAL.equals(nameOp) && PropertyPredicate.ComparisonOperator.EQUAL.equals(valueOp))
      return nameEqualAndValueEqual(name, value); 
    if (PropertyPredicate.ComparisonOperator.EQUAL.equals(nameOp) && PropertyPredicate.ComparisonOperator.NOT_EQUAL.equals(valueOp))
      return nameEqualAndValueNotEqual(name, value); 
    if (PropertyPredicate.ComparisonOperator.EQUAL.equals(nameOp) && PropertyPredicate.ComparisonOperator.LIKE.equals(valueOp))
      return nameEqualAndValueLike(name, value); 
    if (PropertyPredicate.ComparisonOperator.NOT_EQUAL.equals(nameOp) && PropertyPredicate.ComparisonOperator.EQUAL.equals(valueOp))
      return nameNotEqualAndValueEqual(name, value); 
    if (PropertyPredicate.ComparisonOperator.NOT_EQUAL.equals(nameOp) && PropertyPredicate.ComparisonOperator.NOT_EQUAL.equals(valueOp))
      return nameNotEqualAndValueNotEqual(name, value); 
    if (PropertyPredicate.ComparisonOperator.NOT_EQUAL.equals(nameOp) && PropertyPredicate.ComparisonOperator.LIKE.equals(valueOp))
      return nameNotEqualAndValueLike(name, value); 
    if (PropertyPredicate.ComparisonOperator.LIKE.equals(nameOp) && PropertyPredicate.ComparisonOperator.EQUAL.equals(valueOp))
      return nameLikeAndValueEqual(name, value); 
    if (PropertyPredicate.ComparisonOperator.LIKE.equals(nameOp) && PropertyPredicate.ComparisonOperator.NOT_EQUAL.equals(valueOp))
      return nameLikeAndValueNotEqual(name, value); 
    if (PropertyPredicate.ComparisonOperator.LIKE.equals(nameOp) && PropertyPredicate.ComparisonOperator.LIKE.equals(valueOp))
      return nameLikeAndValueLike(name, value); 
    throw new IllegalArgumentException("Filter uses unsupported comparison operator: " + filter);
  }
  
  private Collection<ManagedObjectReference> nameEqualAndValueEqual(String name, String value) {
    return getEntities(name, value, true);
  }
  
  private Collection<ManagedObjectReference> nameEqualAndValueNotEqual(String name, String value) {
    Collection<ManagedObjectReference> entities = getEntities(name, (String)null, true);
    return filterEntities(entities, equalsIgnoreCase(name), 
        notEqualsIgnoreCase(value));
  }
  
  private Collection<ManagedObjectReference> nameEqualAndValueLike(String name, String value) {
    Collection<ManagedObjectReference> entities = getEntities(name, (String)null, true);
    return filterEntities(entities, equalsIgnoreCase(name), 
        likeIgnoreCase(value));
  }
  
  private Collection<ManagedObjectReference> nameNotEqualAndValueEqual(String name, String value) {
    ImmutableList immutableList = FluentIterable.from(getCustomFieldNames()).filter(notEqualsIgnoreCase(name)).toList();
    return getEntities((Collection<String>)immutableList, value, true);
  }
  
  private Collection<ManagedObjectReference> nameNotEqualAndValueNotEqual(String name, String value) {
    return getEntities(notEqualsIgnoreCase(name), notEqualsIgnoreCase(value));
  }
  
  private Collection<ManagedObjectReference> nameNotEqualAndValueLike(String name, String value) {
    return getEntities(notEqualsIgnoreCase(name), likeIgnoreCase(value));
  }
  
  private Collection<ManagedObjectReference> nameLikeAndValueEqual(String name, String value) {
    ImmutableList immutableList = FluentIterable.from(getCustomFieldNames()).filter(likeIgnoreCase(name)).toList();
    return getEntities((Collection<String>)immutableList, value, true);
  }
  
  private Collection<ManagedObjectReference> nameLikeAndValueNotEqual(String name, String value) {
    return getEntities(likeIgnoreCase(name), notEqualsIgnoreCase(value));
  }
  
  private Collection<ManagedObjectReference> nameLikeAndValueLike(String name, String value) {
    OperatorLikeEvaluator.StringMatchingInfo nameMatchInfo = OperatorLikeEvaluator.analyzeTemplate(name);
    OperatorLikeEvaluator.StringMatchingInfo valueMatchInfo = OperatorLikeEvaluator.analyzeTemplate(value);
    OperatorLikeEvaluator.StringMatchingMode nameMode = nameMatchInfo.getMode();
    OperatorLikeEvaluator.StringMatchingMode valueMode = valueMatchInfo.getMode();
    String nameSearchText = nameMatchInfo.getSearchText();
    String valueSearchText = valueMatchInfo.getSearchText();
    if (OperatorLikeEvaluator.StringMatchingMode.Exact.equals(nameMode) && OperatorLikeEvaluator.StringMatchingMode.Exact.equals(valueMode))
      return getEntities(nameSearchText, valueSearchText, true); 
    if (OperatorLikeEvaluator.StringMatchingMode.Exact.equals(nameMode))
      return nameEqualAndValueLike(nameSearchText, value); 
    if (OperatorLikeEvaluator.StringMatchingMode.Exact.equals(valueMode))
      return nameLikeAndValueEqual(name, valueSearchText); 
    if (OperatorLikeEvaluator.StringMatchingMode.Contains.equals(nameMode) && OperatorLikeEvaluator.StringMatchingMode.Contains.equals(valueMode))
      return getEntities(nameSearchText, valueSearchText, false); 
    Collection<ManagedObjectReference> entities = getEntities(nameSearchText, valueSearchText, false);
    return filterEntities(entities, likeIgnoreCase(name), 
        likeIgnoreCase(value));
  }
  
  private Collection<ManagedObjectReference> getEntities(Predicate<String> namePredicate, Predicate<String> valuePredicate) {
    assert namePredicate != null;
    assert valuePredicate != null;
    ImmutableList immutableList = FluentIterable.from(getCustomFieldNames()).filter(namePredicate).toList();
    Collection<ManagedObjectReference> entities = getEntities((Collection<String>)immutableList, (String)null, true);
    return filterEntities(entities, namePredicate, valuePredicate);
  }
  
  private Collection<ManagedObjectReference> getEntities(String name, String value, boolean exactMatch) {
    return getEntities(Collections.singleton(name), value, exactMatch);
  }
  
  private Collection<ManagedObjectReference> getEntities(Collection<String> names, String value, boolean exactMatch) {
    return this._customFields.getEntities(names, value, exactMatch);
  }
  
  private Collection<ManagedObjectReference> filterEntities(Collection<ManagedObjectReference> entities, Predicate<String> namePredicate, Predicate<String> valuePredicate) {
    return this._customFields.filterEntities(entities, namePredicate, valuePredicate);
  }
  
  private Collection<String> getCustomFieldNames() {
    return this._customFields.getCustomFieldNames();
  }
  
  private static void validateQuery(Query query) {
    Validate.notNull(query);
    for (String property : query.getProperties()) {
      if (!"CustomFieldAssociation/entity".equals(property))
        throw new IllegalArgumentException("Unsupported property requested: " + property); 
    } 
    if (query.getWithTotalCount())
      throw new IllegalArgumentException("Total count is not supported"); 
    for (String model : query.getResourceModels()) {
      if (!"CustomFieldAssociation".equals(model))
        throw new IllegalArgumentException("Unsupported resource model: " + model); 
    } 
    if (query.getFilter() == null)
      throw new IllegalArgumentException("Filter is required"); 
    if (query.getFilter().getCriteria().size() != 2)
      throw new IllegalArgumentException("Filter must be a conjunction with two predicates: " + query
          
          .getFilter()); 
    if (!LogicalOperator.AND.equals(query.getFilter().getOperator()))
      throw new IllegalArgumentException("Only conjunction filters are supported"); 
    if (!query.getSortCriteria().isEmpty())
      throw new IllegalArgumentException("Ordering is not supported"); 
  }
  
  private static PropertyPredicate getCaseInsensitivePredicate(Filter filter, String property) {
    assert filter != null;
    assert filter.getCriteria().size() > 0;
    PropertyPredicate wantedPredicate = null;
    int wantedPredicateCount = 0;
    for (PropertyPredicate predicate : filter.getCriteria()) {
      if (property.equals(predicate.getProperty())) {
        wantedPredicate = predicate;
        wantedPredicateCount++;
      } 
    } 
    if (wantedPredicateCount > 1)
      throw new IllegalArgumentException(String.format("Filter has multiple predicates on property '%s': %s", new Object[] { property, filter })); 
    if (wantedPredicate == null)
      throw new IllegalArgumentException(String.format("Filter must have predicate on '%s': %s", new Object[] { property, filter })); 
    return wantedPredicate;
  }
  
  private static String getComparableString(PropertyPredicate predicate) {
    assert predicate != null;
    Object comparableValue = predicate.getComparableValue();
    if (comparableValue instanceof String)
      return (String)comparableValue; 
    throw new IllegalArgumentException(
        String.format("Comparable value for property '%s' must be java.lang.String and not %s", new Object[] { predicate.getProperty(), (comparableValue != null) ? comparableValue
            .getClass() : "null" }));
  }
  
  private static ResultSet createResultSet(Collection<ManagedObjectReference> entitiesWithCustomFields) {
    assert entitiesWithCustomFields != null;
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(new String[] { "CustomFieldAssociation/entity" });
    for (ManagedObjectReference ref : entitiesWithCustomFields) {
      resultBuilder.item("NOT_USED", new Object[] { ref });
    } 
    return resultBuilder.build();
  }
  
  private static Predicate<String> equalsIgnoreCase(final String comparable) {
    Validate.notNull(comparable);
    return new Predicate<String>() {
        public boolean apply(String input) {
          if (input == null)
            return false; 
          return comparable.equalsIgnoreCase(input);
        }
        
        public String toString() {
          return "Predicate:EqualsIgnoreCase";
        }
      };
  }
  
  private static Predicate<String> notEqualsIgnoreCase(final String comparable) {
    Validate.notNull(comparable);
    return new Predicate<String>() {
        public boolean apply(String input) {
          if (input == null)
            return true; 
          return !comparable.equalsIgnoreCase(input);
        }
        
        public String toString() {
          return "Predicate:NotEqualsIgnoreCase";
        }
      };
  }
  
  private static Predicate<String> startsWithIgnoreCase(String comparable) {
    Validate.notNull(comparable);
    final String lowerCaseComparable = comparable.toLowerCase();
    return new Predicate<String>() {
        public boolean apply(String input) {
          if (input == null)
            return false; 
          return input.toLowerCase().startsWith(lowerCaseComparable);
        }
        
        public String toString() {
          return "Predicate:StartsWithIgnoreCase";
        }
      };
  }
  
  private static Predicate<String> endsWithIgnoreCase(String comparable) {
    Validate.notNull(comparable);
    final String lowerCaseComparable = comparable.toLowerCase();
    return new Predicate<String>() {
        public boolean apply(String input) {
          if (input == null)
            return false; 
          return input.toLowerCase().endsWith(lowerCaseComparable);
        }
        
        public String toString() {
          return "Predicate:EndsWithIgnoreCase";
        }
      };
  }
  
  private static Predicate<String> containsIgnoreCase(String comparable) {
    Validate.notNull(comparable);
    final String lowerCaseComparable = comparable.toLowerCase();
    return new Predicate<String>() {
        public boolean apply(String input) {
          if (input == null)
            return false; 
          return input.toLowerCase().contains(lowerCaseComparable);
        }
        
        public String toString() {
          return "Predicate:ContainsIgnoreCase";
        }
      };
  }
  
  private static Predicate<String> likeIgnoreCase(String comparable) {
    Validate.notNull(comparable);
    OperatorLikeEvaluator.StringMatchingInfo matchInfo = OperatorLikeEvaluator.analyzeTemplate(comparable);
    OperatorLikeEvaluator.StringMatchingMode mode = matchInfo.getMode();
    String searchText = matchInfo.getSearchText();
    switch (mode) {
      case StartsWith:
        return startsWithIgnoreCase(searchText);
      case EndsWith:
        return endsWithIgnoreCase(searchText);
      case Contains:
        return containsIgnoreCase(searchText);
      case Exact:
        return equalsIgnoreCase(searchText);
    } 
    throw new IllegalStateException(String.format("Unknown string matching mode '%s' in search phrase '%s'", new Object[] { mode, comparable }));
  }
}

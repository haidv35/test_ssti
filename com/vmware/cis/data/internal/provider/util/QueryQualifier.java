package com.vmware.cis.data.internal.provider.util;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.internal.util.UnqualifiedProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryQualifier {
  private static final String POSSIBLE_DP_FAILURE_MESSAGE = "It is possible that the property is misspelled or a connection to a certain Data Provider has failed. Please check the logs for other errors.";
  
  private final QuerySchema _querySchema;
  
  private final QueryQualifierAmbiguityResolver _ambiguityResolver;
  
  public QueryQualifier(QuerySchema querySchema, QueryQualifierAmbiguityResolver ambiguityResolver) {
    this._querySchema = querySchema;
    this._ambiguityResolver = ambiguityResolver;
  }
  
  public QualifierContext qualifyQuery(Query query) {
    assert query != null;
    List<String> properties = qualifySelectClause(query);
    Map<String, String> propertiesMapping = createMapping(query.getProperties(), properties);
    Filter filter = null;
    List<PropertyPredicate> propertyPredicates = qualifyWhereClause(query);
    if (!propertyPredicates.isEmpty())
      filter = new Filter(propertyPredicates, query.getFilter().getOperator()); 
    List<SortCriterion> sortCriteria = qualifyOrderByClause(query);
    Collection<String> resourceModels = query.getResourceModels();
    Query.Builder builder = QueryCopy.copyAndSelect(query, properties).from(resourceModels).where(filter).orderBy(sortCriteria);
    return new QualifierContext(builder.build(), propertiesMapping);
  }
  
  public ResultSet unqualifyResultSet(ResultSet resultSet, QualifierContext context) {
    assert resultSet != null;
    assert context != null;
    List<String> properties = new ArrayList<>(resultSet.getProperties().size());
    for (String qualifiedProperty : resultSet.getProperties()) {
      String originalProperty = (String)context._originalByQualifiedProperties.get(qualifiedProperty);
      if (originalProperty == null)
        originalProperty = qualifiedProperty; 
      properties.add(originalProperty);
    } 
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(properties);
    for (ResourceItem item : resultSet.getItems())
      resultBuilder.item(item.getKey(), item.getPropertyValues()); 
    return resultBuilder.totalCount(resultSet.getTotalCount()).build();
  }
  
  private List<String> qualifySelectClause(Query query) {
    List<String> properties = new ArrayList<>(query.getProperties().size());
    for (String property : query.getProperties()) {
      String qualifiedProperty = qualifyProperty(property, query.getResourceModels());
      properties.add(qualifiedProperty);
    } 
    return properties;
  }
  
  private List<PropertyPredicate> qualifyWhereClause(Query query) {
    if (query.getFilter() == null)
      return new ArrayList<>(); 
    List<PropertyPredicate> propertyPredicates = new ArrayList<>(query.getFilter().getCriteria().size());
    for (PropertyPredicate pp : query.getFilter().getCriteria()) {
      String qualifiedProperty = qualifyProperty(pp
          .getProperty(), query.getResourceModels());
      PropertyPredicate propertyPredicate = new PropertyPredicate(qualifiedProperty, pp.getOperator(), pp.getComparableValue(), pp.isIgnoreCase());
      propertyPredicates.add(propertyPredicate);
    } 
    return propertyPredicates;
  }
  
  private List<SortCriterion> qualifyOrderByClause(Query query) {
    List<SortCriterion> sortCriteria = new ArrayList<>(query.getSortCriteria().size());
    for (SortCriterion sc : query.getSortCriteria()) {
      String qualifiedProperty = qualifyProperty(sc
          .getProperty(), query.getResourceModels());
      SortCriterion sortCriterion = new SortCriterion(qualifiedProperty, sc.getSortDirection(), sc.isIgnoreCase());
      sortCriteria.add(sortCriterion);
    } 
    return sortCriteria;
  }
  
  public static Collection<String> getFromClause(List<String> properties, Filter filter, List<SortCriterion> sortCriteria) {
    Set<String> models = new LinkedHashSet<>();
    for (String property : properties)
      addModel(property, models); 
    if (filter != null)
      for (PropertyPredicate predicate : filter.getCriteria())
        addModel(predicate.getProperty(), models);  
    if (sortCriteria != null)
      for (SortCriterion sortCriterion : sortCriteria)
        addModel(sortCriterion.getProperty(), models);  
    return new ArrayList<>(models);
  }
  
  public static void addModel(String property, Set<String> models) {
    if (PropertyUtil.isSpecialProperty(property))
      return; 
    models.add(QualifiedProperty.forQualifiedName(property).getResourceModel());
  }
  
  private String qualifyProperty(String property, Collection<String> models) {
    assert models != null;
    if (PropertyUtil.isSpecialProperty(property))
      return property; 
    if (QualifiedProperty.isSyntacticallyQualified(property)) {
      QualifiedProperty qp = QualifiedProperty.forQualifiedName(property);
      if (modelExists(qp.getResourceModel())) {
        if (propertyExists(qp.getResourceModel(), qp.getSimpleProperty()))
          return property; 
        throw new IllegalArgumentException("Property '" + qp.getSimpleProperty() + "' is not registered for model '" + qp
            .getResourceModel() + "'. " + "It is possible that the property is misspelled or a connection to a certain Data Provider has failed. Please check the logs for other errors.");
      } 
      if (!qp.isVmodl1())
        throw new IllegalArgumentException("Resource model " + qp
            .getResourceModel() + " is not registered"); 
    } 
    QualifiedProperty qualifiedProperty = null;
    for (String model : models) {
      QualifiedProperty currentProperty = QualifiedProperty.forModelAndSimpleProperty(model, property);
      if (!propertyExists(model, property))
        continue; 
      if (qualifiedProperty == null) {
        qualifiedProperty = currentProperty;
        continue;
      } 
      String model1 = qualifiedProperty.getResourceModel();
      String model2 = currentProperty.getResourceModel();
      String newModel = this._ambiguityResolver.pickModel(model1, model2, property);
      if (newModel != null) {
        qualifiedProperty = QualifiedProperty.forModelAndSimpleProperty(newModel, property);
        continue;
      } 
      throw new IllegalArgumentException(
          String.format("Unqualified property '%s' is ambiguous - it appears both in  model '%s' and model '%s'", new Object[] { property, model1, model2 }));
    } 
    if (qualifiedProperty == null)
      throw new IllegalArgumentException("Could not qualify property: '" + property + "'. " + "It is possible that the property is misspelled or a connection to a certain Data Provider has failed. Please check the logs for other errors."); 
    return qualifiedProperty.toString();
  }
  
  private boolean propertyExists(String model, String property) {
    QuerySchema.ModelInfo modelInfo = this._querySchema.getModels().get(model);
    if (modelInfo == null)
      return false; 
    QuerySchema.PropertyInfo propertyInfo = modelInfo.getProperties().get(
        UnqualifiedProperty.getRootProperty(property));
    if (propertyInfo == null)
      return false; 
    return true;
  }
  
  private boolean modelExists(String model) {
    QuerySchema.ModelInfo modelInfo = this._querySchema.getModels().get(model);
    return (modelInfo != null);
  }
  
  private Map<String, String> createMapping(List<String> originalProperties, List<String> qualifiedProperties) {
    assert originalProperties != null;
    assert qualifiedProperties != null;
    assert originalProperties.size() == qualifiedProperties.size();
    Map<String, String> propertiesMapping = new HashMap<>();
    Iterator<String> iterator = originalProperties.iterator();
    for (String qualifiedProperty : qualifiedProperties) {
      String originalProperty = iterator.next();
      String storedProperty = propertiesMapping.put(qualifiedProperty, originalProperty);
      if (storedProperty == null)
        continue; 
      if (storedProperty.equals(originalProperty))
        throw new IllegalArgumentException(String.format("Property '%s' specified multiple times in SELECT", new Object[] { originalProperty })); 
      throw new IllegalArgumentException(String.format("Both property '%s' and %s are qualified as %s", new Object[] { storedProperty, originalProperty, qualifiedProperty }));
    } 
    return propertiesMapping;
  }
  
  public static interface QueryQualifierAmbiguityResolver {
    String pickModel(String param1String1, String param1String2, String param1String3);
  }
  
  public class QualifierContext {
    private final Map<String, String> _originalByQualifiedProperties;
    
    private final Query _qualifiedQuery;
    
    QualifierContext(Query qualifiedQuery, Map<String, String> originalByQualifiedProperties) {
      assert qualifiedQuery != null;
      assert originalByQualifiedProperties != null;
      this._qualifiedQuery = qualifiedQuery;
      this._originalByQualifiedProperties = originalByQualifiedProperties;
    }
    
    public Query getQualifiedQuery() {
      return this._qualifiedQuery;
    }
  }
}

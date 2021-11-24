package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.provider.DataProvider;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RelatedLengthProviderConnection implements DataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(RelatedLengthProviderConnection.class);
  
  private static final String SUFFIX_LENGTH = "/length";
  
  private final DataProvider _connection;
  
  private final RelatedPropertyLookup _relatedPropertyLookup;
  
  public RelatedLengthProviderConnection(DataProvider connection, RelatedPropertyLookup relatedPropertyLookup) {
    assert connection != null;
    assert relatedPropertyLookup != null;
    this._connection = connection;
    this._relatedPropertyLookup = relatedPropertyLookup;
  }
  
  public ResultSet executeQuery(Query query) {
    Validate.notNull(query);
    List<String> selectProperties = query.getProperties();
    List<Integer> relatedLengthPositions = new ArrayList<>();
    List<String> modifiedSelectProperties = modifySelectProperties(selectProperties, relatedLengthPositions);
    if (relatedLengthPositions.isEmpty())
      return this._connection.executeQuery(query); 
    Query modifiedQuery = QueryCopy.copyAndSelect(query, modifiedSelectProperties).build();
    ResultSet result = this._connection.executeQuery(modifiedQuery);
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(selectProperties);
    modifyResourceItems(resultBuilder, result.getItems(), relatedLengthPositions);
    return resultBuilder.totalCount(result.getTotalCount()).build();
  }
  
  private List<String> modifySelectProperties(List<String> selectProperties, List<Integer> relatedLengthPositions) {
    List<String> convertedProperties = new ArrayList<>(selectProperties.size());
    for (int index = 0; index < selectProperties.size(); index++) {
      String selectedProperty = selectProperties.get(index);
      if (!selectedProperty.endsWith("/length")) {
        convertedProperties.add(selectedProperty);
      } else {
        String plainProperty = StringUtils.removeEnd(selectedProperty, "/length");
        RelatedPropertyDescriptor descriptor = this._relatedPropertyLookup.getRelatedPropertyDescriptor(plainProperty);
        if (descriptor == null) {
          convertedProperties.add(selectedProperty);
        } else {
          if (!descriptor.getType().isArray())
            throw new IllegalArgumentException("Cannot request length over related property which is defined as non-array:" + selectedProperty); 
          convertedProperties.add(plainProperty);
          relatedLengthPositions.add(Integer.valueOf(index));
          _logger.warn("Counting of the values for property '{}' will be taken on client side.", selectedProperty);
        } 
      } 
    } 
    return convertedProperties;
  }
  
  private void modifyResourceItems(ResultSet.Builder resultBuilder, List<ResourceItem> items, List<Integer> relatedLengthPositions) {
    for (ResourceItem item : items) {
      List<Object> modifiedValues = new ArrayList(item.getPropertyValues());
      for (Integer relatedLengthPosition : relatedLengthPositions) {
        Object value = modifiedValues.get(relatedLengthPosition.intValue());
        if (value == null)
          continue; 
        int length = Array.getLength(value);
        modifiedValues.set(relatedLengthPosition.intValue(), Integer.valueOf(length));
      } 
      resultBuilder.item(item.getKey(), modifiedValues);
    } 
  }
  
  public QuerySchema getSchema() {
    return this._connection.getSchema();
  }
  
  public String toString() {
    return this._connection.toString();
  }
}

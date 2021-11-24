package com.vmware.cis.data.internal.provider.ext.predicate;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import java.util.ArrayList;
import java.util.List;

final class FilterAlgebra {
  public static Filter negate(Filter filter) {
    assert filter != null;
    List<PropertyPredicate> negatedPredicates = new ArrayList<>(filter.getCriteria().size());
    for (PropertyPredicate predicate : filter.getCriteria()) {
      PropertyPredicate negatedPredicate = PredicateAlgebra.negate(predicate);
      negatedPredicates.add(negatedPredicate);
    } 
    return new Filter(negatedPredicates, negate(filter.getOperator()));
  }
  
  private static LogicalOperator negate(LogicalOperator logicalOperator) {
    assert logicalOperator != null;
    switch (logicalOperator) {
      case AND:
        return LogicalOperator.OR;
      case OR:
        return LogicalOperator.AND;
    } 
    throw new IllegalArgumentException(String.format("Unsupported logical operator '%s'", new Object[] { logicalOperator }));
  }
}

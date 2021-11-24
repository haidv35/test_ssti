package com.vmware.cis.data.internal.provider.join;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.ResultSetAnalyzer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class LeftJoinOperator implements JoinOperator {
  public ResultSet join(Collection<ResultSet> results) {
    throw new UnsupportedOperationException("LEFT JOIN for unordered result sets is not supported");
  }
  
  public ResultSet joinOrderedResult(ResultSet left, ResultSet rightOrdered) {
    assert left != null;
    assert rightOrdered != null;
    List<Object> rightOrderedKeys = ResultSetAnalyzer.gatherModelKeysOrdered(rightOrdered);
    Set<Object> leftUnorderedKeys = ResultSetAnalyzer.gatherModelKeys(left);
    rightOrderedKeys.retainAll(leftUnorderedKeys);
    if (!rightOrderedKeys.isEmpty()) {
      leftUnorderedKeys.removeAll(rightOrderedKeys);
      rightOrderedKeys.addAll(leftUnorderedKeys);
    } 
    Collection<Object> modelKeys = rightOrderedKeys.isEmpty() ? leftUnorderedKeys : rightOrderedKeys;
    List<ResultSet> results = Arrays.asList(new ResultSet[] { left, rightOrdered });
    ResultSet joinResult = RelationalAlgebra.joinAndSelect(results, modelKeys);
    return joinResult;
  }
}

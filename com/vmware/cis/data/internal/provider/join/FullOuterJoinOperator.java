package com.vmware.cis.data.internal.provider.join;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.ResultSetAnalyzer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

final class FullOuterJoinOperator implements JoinOperator {
  public ResultSet join(Collection<ResultSet> results) {
    assert results != null;
    assert !results.isEmpty();
    Iterator<ResultSet> resultsIterator = results.iterator();
    if (results.size() == 1)
      return resultsIterator.next(); 
    ResultSet joinResult = RelationalAlgebra.joinAndSelect(results, null);
    return joinResult;
  }
  
  public ResultSet joinOrderedResult(ResultSet result, ResultSet orderedResult) {
    assert result != null;
    assert orderedResult != null;
    List<Object> orderedKeys = ResultSetAnalyzer.gatherModelKeysOrdered(orderedResult);
    Set<Object> unorderedKeys = ResultSetAnalyzer.gatherModelKeys(result);
    if (!orderedKeys.isEmpty()) {
      unorderedKeys.removeAll(orderedKeys);
      orderedKeys.addAll(unorderedKeys);
    } 
    Collection<Object> modelKeys = orderedKeys.isEmpty() ? unorderedKeys : orderedKeys;
    List<ResultSet> results = Arrays.asList(new ResultSet[] { result, orderedResult });
    ResultSet joinResult = RelationalAlgebra.joinAndSelect(results, modelKeys);
    return joinResult;
  }
}

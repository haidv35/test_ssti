package com.vmware.cis.data.internal.provider.join;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.ResultSetAnalyzer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class InnerJoinOperator implements JoinOperator {
  public ResultSet join(Collection<ResultSet> results) {
    assert results != null;
    assert !results.isEmpty();
    Iterator<ResultSet> resultsIterator = results.iterator();
    if (results.size() == 1)
      return resultsIterator.next(); 
    Set<Object> intersection = ResultSetAnalyzer.gatherModelKeys(resultsIterator.next());
    while (resultsIterator.hasNext()) {
      Set<Object> modelKeys = ResultSetAnalyzer.gatherModelKeys(resultsIterator.next());
      intersection.retainAll(modelKeys);
    } 
    ResultSet joinResult = RelationalAlgebra.joinAndSelect(results, intersection);
    return joinResult;
  }
  
  public ResultSet joinOrderedResult(ResultSet result, ResultSet orderedResult) {
    assert result != null;
    assert orderedResult != null;
    Set<Object> unorderedKeys = ResultSetAnalyzer.gatherModelKeys(result);
    List<Object> orderedKeys = ResultSetAnalyzer.gatherModelKeysOrdered(orderedResult);
    orderedKeys.retainAll(unorderedKeys);
    List<ResultSet> results = Arrays.asList(new ResultSet[] { result, orderedResult });
    ResultSet joinResult = RelationalAlgebra.joinAndSelect(results, orderedKeys);
    return joinResult;
  }
}

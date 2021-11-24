package com.vmware.cis.data.internal.provider.join;

import com.vmware.cis.data.api.ResultSet;
import java.util.Collection;

interface JoinOperator {
  ResultSet join(Collection<ResultSet> paramCollection);
  
  ResultSet joinOrderedResult(ResultSet paramResultSet1, ResultSet paramResultSet2);
}

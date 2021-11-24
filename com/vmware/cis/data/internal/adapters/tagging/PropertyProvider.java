package com.vmware.cis.data.internal.adapters.tagging;

import java.util.List;

public interface PropertyProvider {
  List<List<Object>> get(List<?> paramList, List<String> paramList1);
  
  List<List<Object>> list(List<String> paramList);
}

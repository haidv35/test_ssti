package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.api.PropertyPredicate;
import java.util.List;

public interface FilteringPropertyProvider {
  List<?> getKeys(PropertyPredicate paramPropertyPredicate);
}

package com.vmware.cis.data.internal.api.binding;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.internal.provider.ext.alias.AliasPropertyDescriptor;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyDescriptor;
import com.vmware.cis.data.provider.DataProvider;
import java.util.Collection;
import java.util.List;

interface QueryBindingField {
  List<String> getPropertiesToSelect();
  
  void set(DataProvider paramDataProvider, List<ResourceItem> paramList, List<Object> paramList1);
  
  Collection<RelatedPropertyDescriptor> getRelatedPropertyDescriptors();
  
  Collection<AliasPropertyDescriptor> getAliasPropertyDescriptors();
}

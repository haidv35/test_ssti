package com.vmware.cis.data.internal.adapters.customfield;

import com.google.common.base.Predicate;
import com.vmware.vim.binding.vim.CustomFieldsManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.Collection;

interface CustomFieldRepository {
  Collection<ManagedObjectReference> getEntities(Collection<String> paramCollection, String paramString, boolean paramBoolean);
  
  Collection<ManagedObjectReference> filterEntities(Collection<ManagedObjectReference> paramCollection, Predicate<String> paramPredicate1, Predicate<String> paramPredicate2);
  
  Collection<CustomFieldsManager.FieldDef> getCustomFieldDefs();
  
  Collection<String> getCustomFieldNames();
}

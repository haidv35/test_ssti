package com.vmware.cis.data.internal.provider.util.property;

import com.vmware.cis.data.api.ResourceItem;

public interface ResourceItemPropertyByName {
  Object getValue(String paramString, ResourceItem paramResourceItem);
}

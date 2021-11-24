package com.vmware.cis.data.internal.provider.ext.relationship.invert;

import com.vmware.cis.data.api.QuerySchema;
import java.util.Collection;

public interface RelationshipInversionLookup {
  Collection<String> invert(String paramString);
  
  QuerySchema addInvertibleRelationships(QuerySchema paramQuerySchema);
}

package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.Collection;
import org.apache.commons.lang.Validate;

final class InvSvcTaggingIdConverter {
  private static final String ID_PART_SEPARATOR = Character.toString(':');
  
  private static final String VMOMI_ID_PART_0 = "urn";
  
  private static final String VMOMI_ID_PART_1 = "vmomi";
  
  private static final String VMOMI_ID_PREFIX = "urn" + ID_PART_SEPARATOR + "vmomi" + ID_PART_SEPARATOR;
  
  public static String taggingMorToId(ManagedObjectReference mor) {
    Validate.notNull(mor);
    return VMOMI_ID_PREFIX + mor.getType() + ID_PART_SEPARATOR + mor
      .getValue() + ID_PART_SEPARATOR + mor.getServerGuid();
  }
  
  public static ManagedObjectReference taggingIdToMor(String id) {
    if (id == null)
      return null; 
    String[] parts = id.split(ID_PART_SEPARATOR);
    boolean validParts = (parts.length == 5 && "urn".equals(parts[0]) && "vmomi".equals(parts[1]) && !parts[2].isEmpty() && !parts[3].isEmpty() && !parts[4].isEmpty());
    if (!validParts)
      throw new IllegalArgumentException(String.format("Invalid tagging id: '%s'", new Object[] { id })); 
    String moType = parts[2];
    String moId = parts[3];
    String sguid = parts[4];
    return new ManagedObjectReference(moType, moId, sguid);
  }
  
  public static ManagedObjectReference[] taggingIdsToMorArray(Collection<String> ids) {
    if (ids == null)
      return null; 
    ManagedObjectReference[] mors = new ManagedObjectReference[ids.size()];
    int i = 0;
    for (String id : ids)
      mors[i++] = taggingIdToMor(id); 
    return mors;
  }
}

package com.vmware.ph.phservice.cloud.health.dataapp;

import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import org.json.JSONObject;

public class DataAppMoRefToObjectIdConverter {
  private final VmodlToJsonLdSerializer _vmodlToJsonLdSerializer;
  
  public DataAppMoRefToObjectIdConverter(VmodlToJsonLdSerializer vmodlToJsonLdSerializer) {
    this._vmodlToJsonLdSerializer = vmodlToJsonLdSerializer;
  }
  
  public String getObjectId(ManagedObjectReference moRef) {
    if (moRef == null)
      return null; 
    JSONObject jsonObject = (JSONObject)this._vmodlToJsonLdSerializer.serialize(moRef);
    String objectId = jsonObject.getString("@id");
    return objectId;
  }
}

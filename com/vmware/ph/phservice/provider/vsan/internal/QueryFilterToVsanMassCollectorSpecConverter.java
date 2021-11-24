package com.vmware.ph.phservice.provider.vsan.internal;

import com.vmware.cis.data.api.Query;
import com.vmware.ph.phservice.common.cdf.internal.jsonld20.JSONObjectUtil;
import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vsan.binding.vim.VsanMassCollectorPropertyParams;
import com.vmware.vim.vsan.binding.vim.VsanMassCollectorSpec;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;

public class QueryFilterToVsanMassCollectorSpecConverter {
  private final VmodlToJsonLdSerializer _serializer;
  
  public QueryFilterToVsanMassCollectorSpecConverter(VmodlToJsonLdSerializer serializer) {
    this._serializer = serializer;
  }
  
  public VsanMassCollectorSpec[] getVsanMassCollectorSpecs(Query query) {
    VsanMassCollectorSpec vsanMassCollectorSpec = getSpecFromQueryFilter(query, this._serializer);
    List<String> jsonLdSpecs = getJsonLdSpecsFromQueryFilter(query);
    validateSpecs(jsonLdSpecs, vsanMassCollectorSpec);
    VsanMassCollectorSpec[] vsanMassCollectorSpecs = null;
    if (!isListEmpty(jsonLdSpecs)) {
      vsanMassCollectorSpecs = parseJsonLdSpecs(jsonLdSpecs, this._serializer);
    } else {
      vsanMassCollectorSpecs = new VsanMassCollectorSpec[] { vsanMassCollectorSpec };
    } 
    return vsanMassCollectorSpecs;
  }
  
  private static void validateSpecs(List<String> jsonLdSpecs, VsanMassCollectorSpec vsanMassCollectorSpec) {
    if (isListEmpty(jsonLdSpecs) && vsanMassCollectorSpec == null)
      throw new IllegalArgumentException("Missing vSAN specs. Either specify JSON-Ld Specs or a Mass Collector Spec."); 
    if (!isListEmpty(jsonLdSpecs) && vsanMassCollectorSpec != null)
      throw new IllegalArgumentException("Cannot have both vSAN JSON-Ld Specs and a Mass Collector Spec in a single query."); 
  }
  
  private static VsanMassCollectorSpec getSpecFromQueryFilter(Query query, VmodlToJsonLdSerializer serializer) {
    String vsanMassCollectorObjectCollection = (String)QueryUtil.getFilterPropertyComparableValue(query, "vsanMassCollectorSpecs/objectCollection");
    List<ManagedObjectReference> vsanMassCollectorObjects = QueryUtil.getFilterPropertyComparableValues(query, "vsanMassCollectorSpecs/objects");
    List<String> vsanMassCollectorProperties = QueryUtil.getFilterPropertyComparableValues(query, "vsanMassCollectorSpecs/properties");
    List<String> vsanMassCollectorPropertiesParams = QueryUtil.getFilterPropertyComparableValues(query, "vsanMassCollectorSpecs/propertiesParams");
    VsanMassCollectorSpec vsanMassCollectorSpec = null;
    if (!isListEmpty(vsanMassCollectorProperties) && (
      !StringUtils.isBlank(vsanMassCollectorObjectCollection) || 
      !isListEmpty(vsanMassCollectorObjects))) {
      vsanMassCollectorSpec = new VsanMassCollectorSpec();
      vsanMassCollectorSpec.setObjectCollection(vsanMassCollectorObjectCollection);
      vsanMassCollectorSpec.setObjects(vsanMassCollectorObjects
          .<ManagedObjectReference>toArray(
            new ManagedObjectReference[vsanMassCollectorObjects.size()]));
      vsanMassCollectorSpec.setProperties(vsanMassCollectorProperties
          .<String>toArray(
            new String[vsanMassCollectorProperties.size()]));
      vsanMassCollectorSpec.setPropertiesParams(
          parsePropertiesParams(vsanMassCollectorPropertiesParams, serializer));
    } 
    return vsanMassCollectorSpec;
  }
  
  private static VsanMassCollectorPropertyParams[] parsePropertiesParams(List<String> jsonLdPropertiesParams, VmodlToJsonLdSerializer serializer) {
    JSONArray propertiesParamsAsJsonArray = JSONObjectUtil.convertJsonStringsToJsonArray(jsonLdPropertiesParams);
    Object deserializedVsanMassCollectorPropertiesParams = serializer.deserialize(propertiesParamsAsJsonArray);
    if (!(deserializedVsanMassCollectorPropertiesParams instanceof VsanMassCollectorPropertyParams[]))
      return null; 
    VsanMassCollectorPropertyParams[] vsanMassCollectorPropertiesParams = (VsanMassCollectorPropertyParams[])deserializedVsanMassCollectorPropertiesParams;
    return vsanMassCollectorPropertiesParams;
  }
  
  private static List<String> getJsonLdSpecsFromQueryFilter(Query query) {
    List<String> vsanMassCollectorQuerySpecs = QueryUtil.getFilterPropertyComparableValues(query, "vsanMassCollectorSpecs");
    return vsanMassCollectorQuerySpecs;
  }
  
  private static VsanMassCollectorSpec[] parseJsonLdSpecs(List<String> jsonLdSpecs, VmodlToJsonLdSerializer serializer) {
    if (isListEmpty(jsonLdSpecs))
      return null; 
    try {
      JSONArray jsonLdSpecsAsJsonArray = JSONObjectUtil.convertJsonStringsToJsonArray(jsonLdSpecs);
      Object deserializedSpecs = serializer.deserialize(jsonLdSpecsAsJsonArray);
      if (!(deserializedSpecs instanceof VsanMassCollectorSpec[]))
        return null; 
      VsanMassCollectorSpec[] vsanMassCollectorSpecs = (VsanMassCollectorSpec[])deserializedSpecs;
      return vsanMassCollectorSpecs;
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create VsanMassCollectorSpec-s for " + 
          
          ArrayUtils.toString(jsonLdSpecs), e);
    } 
  }
  
  private static boolean isListEmpty(List<?> list) {
    return (list == null || list.isEmpty());
  }
}

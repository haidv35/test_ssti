package com.vmware.ph.phservice.common.vmomi.pc;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.vim.binding.vmodl.DynamicProperty;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.query.InvalidProperty;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PropertyCollectorReader {
  private static final int BATCH_SIZE = 250;
  
  private static final PropertyCollector.RetrieveOptions RETRIEVE_OPTIONS = new PropertyCollector.RetrieveOptions();
  
  private final Builder<PropertyCollector> _pcBuilder;
  
  private final Builder<String> _serverGuidBuilder;
  
  private PropertyCollector _pc;
  
  private String _serverGuid;
  
  static {
    RETRIEVE_OPTIONS.maxObjects = Integer.valueOf(250);
  }
  
  public PropertyCollectorReader(Builder<PropertyCollector> pcBuilder, Builder<String> serverGuidBuilder) {
    this._pcBuilder = pcBuilder;
    this._serverGuidBuilder = serverGuidBuilder;
  }
  
  public List<PcResourceItem> retrieveContent(PropertyCollector.FilterSpec pcFilterSpec, List<String> pcPropertyNames, int offset, int limit) throws InvalidProperty {
    List<PcResourceItem> pcResult = retrieveContentInt(pcFilterSpec, pcPropertyNames, offset, limit, new PcRetrieveResultToPcResourceItemsConverter() {
          public List<PropertyCollectorReader.PcResourceItem> convert(PropertyCollector.RetrieveResult pcRetrieveResult, List<String> pcPropertyNames) {
            List<PropertyCollectorReader.PcResourceItem> result = PropertyCollectorReader.this.convertPcResultToPcResourceItems(pcRetrieveResult, pcPropertyNames);
            return result;
          }
        });
    return pcResult;
  }
  
  private List<PcResourceItem> retrieveContentInt(PropertyCollector.FilterSpec filterSpec, List<String> pcPropertyNames, int offset, int limit, PcRetrieveResultToPcResourceItemsConverter converter) throws InvalidProperty {
    if (this._pc == null)
      this._pc = (PropertyCollector)this._pcBuilder.build(); 
    ArrayList<PcResourceItem> pcResult = new ArrayList<>();
    if (filterSpec == null)
      return pcResult; 
    String lastResultToken = null;
    try {
      int itemsStartOffset = 0;
      PropertyCollector.RetrieveResult pcChunkRetrieveResult = this._pc.retrievePropertiesEx(new PropertyCollector.FilterSpec[] { filterSpec }, RETRIEVE_OPTIONS);
      while (pcChunkRetrieveResult != null) {
        lastResultToken = pcChunkRetrieveResult.getToken();
        List<PcResourceItem> pcChunkResourceItems = converter.convert(pcChunkRetrieveResult, pcPropertyNames);
        pcResult.addAll(
            PageUtil.pageWindowItems(pcChunkResourceItems, itemsStartOffset, offset, limit));
        if (limit > 0 && pcResult.size() >= limit) {
          pcChunkRetrieveResult = null;
          continue;
        } 
        if (lastResultToken == null) {
          pcChunkRetrieveResult = null;
          continue;
        } 
        itemsStartOffset += pcChunkResourceItems.size();
        pcChunkRetrieveResult = this._pc.continueRetrievePropertiesEx(lastResultToken);
      } 
    } finally {
      if (lastResultToken != null)
        this._pc.cancelRetrievePropertiesEx(lastResultToken); 
    } 
    return pcResult;
  }
  
  private List<PcResourceItem> convertPcResultToPcResourceItems(PropertyCollector.RetrieveResult pcResult, List<String> pcPropertyNames) {
    if (pcResult == null)
      return new ArrayList<>(0); 
    ArrayList<PcResourceItem> resourceItems = new ArrayList<>();
    for (PropertyCollector.ObjectContent pcResultObjectContent : pcResult.objects) {
      PcResourceItem resourceItem = createPcResourceItem(pcResultObjectContent, pcPropertyNames);
      if (resourceItem != null)
        resourceItems.add(resourceItem); 
    } 
    return resourceItems;
  }
  
  private PcResourceItem createPcResourceItem(PropertyCollector.ObjectContent pcObjectContent, List<String> pcPropertyNames) {
    List<Object> propertyValues = new ArrayList();
    ManagedObjectReference moRef = pcObjectContent.obj;
    if (moRef.getServerGuid() == null) {
      if (this._serverGuid == null)
        this._serverGuid = (String)this._serverGuidBuilder.build(); 
      moRef.setServerGuid(this._serverGuid);
    } 
    propertyValues.add(moRef);
    Map<String, Object> pcPropertyNameToValue = new LinkedHashMap<>();
    for (String pcPropertyName : pcPropertyNames)
      pcPropertyNameToValue.put(pcPropertyName, null); 
    DynamicProperty[] resultPropertySet = pcObjectContent.getPropSet();
    if (resultPropertySet != null)
      for (DynamicProperty dp : resultPropertySet)
        pcPropertyNameToValue.put(dp.getName(), dp.getVal());  
    propertyValues.addAll(pcPropertyNameToValue.values());
    PcResourceItem resourceItem = new PcResourceItem(propertyValues);
    return resourceItem;
  }
  
  public static class PcResourceItem {
    private final List<Object> _propertyValues;
    
    public PcResourceItem(List<Object> propertyValues) {
      this._propertyValues = propertyValues;
    }
    
    public List<Object> getPropertyValues() {
      return this._propertyValues;
    }
  }
  
  private static interface PcRetrieveResultToPcResourceItemsConverter {
    List<PropertyCollectorReader.PcResourceItem> convert(PropertyCollector.RetrieveResult param1RetrieveResult, List<String> param1List) throws InvalidProperty;
  }
}

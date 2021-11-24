package com.vmware.ph.phservice.provider.common.vmomi.pc;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.vmomi.pc.PcStartMoRefProvider;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorReader;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.query.InvalidProperty;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PcDataProviderImpl implements DataProvider {
  private static final Log _log = LogFactory.getLog(PcDataProviderImpl.class);
  
  private final PropertyCollectorReader _pcReader;
  
  private final PcStartMoRefProvider _pcStartMoRefProvider;
  
  private final VmodlTypeMap _vmodlTypeMap;
  
  private final PcSchemaConverter _pcSchemaConverter;
  
  private final DataProvider[] _conflictingDataProviders;
  
  private final QuerySchema _supportedQuerySchema;
  
  private QuerySchema _querySchema;
  
  public PcDataProviderImpl(PropertyCollectorReader pcReader, PcStartMoRefProvider pcStartMoRefProvider, VmodlTypeMap vmodlTypeMap, VmodlVersion vmodlVersion, PcSchemaConverter pcSchemaConverter) {
    this(pcReader, pcStartMoRefProvider, vmodlTypeMap, vmodlVersion, pcSchemaConverter, new DataProvider[0]);
  }
  
  public PcDataProviderImpl(PropertyCollectorReader pcReader, PcStartMoRefProvider pcStartMoRefProvider, VmodlTypeMap vmodlTypeMap, VmodlVersion vmodlVersion, PcSchemaConverter pcSchemaConverter, DataProvider... conflictingDataProviders) {
    this._pcReader = pcReader;
    this._pcStartMoRefProvider = pcStartMoRefProvider;
    this._vmodlTypeMap = vmodlTypeMap;
    this._pcSchemaConverter = pcSchemaConverter;
    this._supportedQuerySchema = this._pcSchemaConverter.convertSchema(vmodlVersion);
    this._conflictingDataProviders = conflictingDataProviders;
  }
  
  public ResultSet executeQuery(Query query) {
    Collection<String> queryResourceModelTypes = query.getResourceModels();
    List<String> queryPropertyNames = query.getProperties();
    String queryResourceModelType = queryResourceModelTypes.iterator().next();
    ResultSet resultSet = processQuery(queryResourceModelType, queryPropertyNames, query

        
        .getOffset(), query
        .getLimit());
    return resultSet;
  }
  
  public QuerySchema getSchema() {
    if (this._querySchema == null) {
      QuerySchema allQuerySchema = this._pcSchemaConverter.convertSchema(null);
      this._querySchema = QuerySchemaUtil.resolveConflict(allQuerySchema, this._conflictingDataProviders);
    } 
    return this._querySchema;
  }
  
  private ResultSet processQuery(String queryResourceModelType, List<String> queryPropertyNames, int offset, int limit) {
    List<String> supportedQueryPropertyNames = QuerySchemaUtil.getSupportedQueryPropertyNames(queryResourceModelType, queryPropertyNames, this._supportedQuerySchema);
    if (supportedQueryPropertyNames == null || supportedQueryPropertyNames.isEmpty())
      return ResultSet.Builder.properties(queryPropertyNames).build(); 
    VmodlType pcModelType = this._vmodlTypeMap.getVmodlType(queryResourceModelType);
    List<String> pcPropertyNames = PcDataProviderUtil.convertQueryPropertiesToPcProperties(supportedQueryPropertyNames);
    List<PropertyCollectorReader.PcResourceItem> pcResourceItems = callPc(pcModelType, pcPropertyNames, offset, limit);
    ResultSet resultSet = PcDataProviderUtil.convertPcResourceItemsToQueryResultSet(supportedQueryPropertyNames, pcResourceItems);
    return resultSet;
  }
  
  private List<PropertyCollectorReader.PcResourceItem> callPc(VmodlType pcModelType, List<String> pcPropertyNames, int offset, int limit) {
    List<PropertyCollectorReader.PcResourceItem> pcResourceItems = new ArrayList<>();
    List<Pair<VmodlType, String>> traversalChain = this._pcSchemaConverter.getRetrievalRulesChain(pcModelType);
    ManagedObjectReference startMoRef = getStartMoRef(pcModelType, traversalChain);
    PropertyCollector.FilterSpec pcFilterSpec = PropertyCollectorUtil.createTraversableFilterSpec(pcModelType, pcPropertyNames, startMoRef, traversalChain, this._vmodlTypeMap);
    try {
      pcResourceItems = this._pcReader.retrieveContent(pcFilterSpec, pcPropertyNames, offset, limit);
    } catch (InvalidProperty e) {
      if (_log.isWarnEnabled())
        _log.warn("Call to PropertyCollector returned InvalidProperty fault for property " + e
            
            .getName() + " :", (Throwable)e); 
    } finally {
      this._pcStartMoRefProvider.destroyStartMoRefIfNeeded(startMoRef);
    } 
    return pcResourceItems;
  }
  
  private ManagedObjectReference getStartMoRef(VmodlType pcModelType, List<Pair<VmodlType, String>> traversalChain) {
    VmodlType startVmodlType = null;
    if (!traversalChain.isEmpty()) {
      startVmodlType = (VmodlType)((Pair)traversalChain.get(0)).getFirst();
    } else {
      startVmodlType = pcModelType;
    } 
    ManagedObjectReference startMoRef = this._pcStartMoRefProvider.getStartMoRef(startVmodlType);
    return startMoRef;
  }
}

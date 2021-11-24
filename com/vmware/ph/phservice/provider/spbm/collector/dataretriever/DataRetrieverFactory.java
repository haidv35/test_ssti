package com.vmware.ph.phservice.provider.spbm.collector.dataretriever;

import com.vmware.cis.data.api.Query;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import com.vmware.vim.binding.pbm.capability.provider.CapabilityObjectSchema;
import com.vmware.vim.binding.pbm.compliance.ComplianceResult;
import com.vmware.vim.binding.pbm.compliance.RollupComplianceResult;
import com.vmware.vim.binding.pbm.profile.CapabilityBasedProfile;
import com.vmware.vim.binding.pbm.profile.DefaultProfileInfo;
import com.vmware.vim.binding.pbm.replication.QueryReplicationGroupResult;
import com.vmware.vim.binding.vmodl.DataObject;
import java.util.Arrays;
import java.util.List;

public class DataRetrieverFactory {
  public static final List<Class<? extends DataObject>> QUERY_SCHEMA_PBM_CLASSES_REFFERED_BY_CLASS_NAME = Arrays.asList((Class<? extends DataObject>[])new Class[] { CapabilityObjectSchema.class, CapabilityBasedProfile.class, ComplianceResult.class, RollupComplianceResult.class, QueryReplicationGroupResult.class, DefaultProfileInfo.class });
  
  public static DataRetriever getDataRetreiver(SpbmCollectorContext ctx, Query query) {
    String dataObjectName = query.getResourceModels().iterator().next();
    switch (dataObjectName) {
      case "CapabilityObjectSchema":
      case "PbmCapabilitySchema":
        return new CapabilityObjectSchemaRetriever(ctx);
      case "CapabilityBasedProfile":
      case "PbmCapabilityProfile":
        return new CapabilityBasedProfileRetriever(ctx);
      case "ComplianceResult":
      case "PbmComplianceResult":
        return new FcdComplianceResultRetriever(ctx, query.getOffset(), query.getLimit());
      case "StorageContainer":
        return new StorageContainerRetriever(ctx);
      case "RollupComplianceResult":
      case "PbmRollupComplianceResult":
        return new RollupComplianceResultRetriever(ctx, query.getOffset(), query.getLimit());
      case "QueryReplicationGroupResult":
      case "PbmQueryReplicationGroupResult":
        return new QueryReplicationGroupResultRetriever(ctx);
      case "PbmCapabilityMetadataPerCategory":
        return new CapabilityObjectMetadataPerCategoryRetriever(ctx);
      case "VasaProviderInfo":
        return new VasaProviderInfoRetriever(ctx);
      case "DefaultProfileInfo":
      case "PbmDefaultProfileInfo":
        return new DatastoreDrpRetriever(ctx);
      case "CustomFaultDomainInfo":
        return new CustomFaultDomainInfoRetriever(ctx);
    } 
    throw new UnsupportedOperationException("DataObject unknown to SPBM DataProvider: " + dataObjectName);
  }
}

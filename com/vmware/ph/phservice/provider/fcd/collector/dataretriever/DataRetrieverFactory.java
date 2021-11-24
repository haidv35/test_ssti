package com.vmware.ph.phservice.provider.fcd.collector.dataretriever;

import com.vmware.cis.data.api.Query;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.DataRetriever;

public class DataRetrieverFactory {
  public static DataRetriever getDataRetreiver(VcClient vcClient, Query query) {
    String dataObjectName = query.getResourceModels().iterator().next();
    switch (dataObjectName) {
      case "VStorageObject":
        return new VStorageObjectRetriever(vcClient, query.getOffset(), query.getLimit());
      case "VStorageObjectAssociations":
        return new VStorageObjectAssociationsRetriever(vcClient, query.getOffset(), query.getLimit());
      case "CustomVStorageObjectSnapshotInfo":
        return new CustomVStorageObjectSnapshotInfoRetriever(vcClient, query.getOffset(), query.getLimit());
    } 
    throw new UnsupportedOperationException("DataObject unknown to FCD DataProvider: " + dataObjectName);
  }
}

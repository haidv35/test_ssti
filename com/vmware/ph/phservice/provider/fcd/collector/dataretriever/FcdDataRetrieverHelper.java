package com.vmware.ph.phservice.provider.fcd.collector.dataretriever;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.util.DatastoreReader;
import com.vmware.vim.binding.vim.fault.InvalidDatastore;
import com.vmware.vim.binding.vim.vslm.ID;
import com.vmware.vim.binding.vim.vslm.vcenter.VStorageObjectManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FcdDataRetrieverHelper {
  private static final Log log = LogFactory.getLog(FcdDataRetrieverHelper.class);
  
  private final VcClient _vcClient;
  
  private final VStorageObjectManager _vStorageObjectManager;
  
  public FcdDataRetrieverHelper(VcClient vcClient) {
    this._vcClient = vcClient;
    this._vStorageObjectManager = (VStorageObjectManager)this._vcClient.createMo(this._vcClient.getServiceInstanceContent().getVStorageObjectManager());
  }
  
  public TreeMap<ID, ManagedObjectReference> getSortedFcdIdToDatastoreMap() {
    List<ManagedObjectReference> datastoreMors = DatastoreReader.getDatastoreMoRefs(this._vcClient);
    TreeMap<ID, ManagedObjectReference> fcdIdToDatastoreMap = new TreeMap<>(new Comparator<ID>() {
          public int compare(ID o1, ID o2) {
            return o1.getId().compareTo(o2.getId());
          }
        });
    for (ManagedObjectReference datastore : datastoreMors) {
      ID[] fcdIds = null;
      try {
        fcdIds = this._vStorageObjectManager.listVStorageObject(datastore);
        if (fcdIds != null)
          for (ID fcdId : fcdIds)
            fcdIdToDatastoreMap.put(fcdId, datastore);  
      } catch (InvalidDatastore e) {
        log.warn("Error while retrieving VStorageObject objects", (Throwable)e);
      } 
    } 
    return fcdIdToDatastoreMap;
  }
  
  public VStorageObjectManager getVStorageObjectManager() {
    return this._vStorageObjectManager;
  }
}

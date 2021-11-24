package com.vmware.ph.phservice.common.vmomi.internal.server;

import com.vmware.ph.phservice.common.vmomi.internal.VersionFinder;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import com.vmware.vim.vmomi.server.AdapterServer;
import com.vmware.vim.vmomi.server.BodyHandler;
import com.vmware.vim.vmomi.server.impl.BodyHandlerImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BodyHandlerFactory {
  private VersionFinder _versionFinder;
  
  private List<VmodlVersion> _versions;
  
  private AdapterServer _adapterServer;
  
  private VmodlContext _vmodlContext;
  
  public BodyHandlerFactory(VersionFinder versionFinder, List<VmodlVersion> versions, AdapterServer adapterServer, VmodlContext vmodlContext) {
    this._versionFinder = versionFinder;
    this._versions = versions;
    this._adapterServer = adapterServer;
    this._vmodlContext = vmodlContext;
  }
  
  public List<BodyHandler> getBodyHandlers() {
    Set<VmodlVersion> compatibleVersions = new HashSet<>();
    for (VmodlVersion version : this._versions)
      compatibleVersions.addAll(this._versionFinder.getCompatible(version)); 
    List<BodyHandler> result = new ArrayList<>();
    for (VmodlVersion version : compatibleVersions) {
      BodyHandlerImpl bodyHandlerImpl = new BodyHandlerImpl(version, this._adapterServer, this._vmodlContext);
      result.add(bodyHandlerImpl);
    } 
    return result;
  }
}

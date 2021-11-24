package com.vmware.ph.phservice.common.vmomi.internal;

import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.util.HashSet;
import java.util.Set;

public class VersionFinder {
  private final VmodlContext _vmodlContext;
  
  public VersionFinder(VmodlContext vmodlContext) {
    this._vmodlContext = vmodlContext;
  }
  
  public VmodlVersion findVersion(Class<?> versionType) {
    return this._vmodlContext.getVmodlVersionMap().getVersion(versionType);
  }
  
  public Set<VmodlVersion> getCompatible(VmodlVersion vmodlVersion) {
    return getCompatible(vmodlVersion, vmodlVersion.getNamespace());
  }
  
  private Set<VmodlVersion> getCompatible(VmodlVersion vmodlVersion, String versionNamespace) {
    Set<VmodlVersion> compatibleVersions = new HashSet<>();
    VmodlVersion internalVmodlVersion = null;
    if (!vmodlVersion.isInternal()) {
      String internalClassName = String.format("%1$s.internal.%2$s", new Object[] { vmodlVersion
            .getVersionClass().getPackage().getName(), vmodlVersion
            .getVersionClass().getSimpleName() });
      try {
        Class<?> internalVersionType = Class.forName(internalClassName);
        internalVmodlVersion = findVersion(internalVersionType);
      } catch (ClassNotFoundException classNotFoundException) {}
    } 
    if (vmodlVersion.getNamespace().equals(versionNamespace)) {
      compatibleVersions.add(vmodlVersion);
      if (internalVmodlVersion != null)
        compatibleVersions.add(internalVmodlVersion); 
      Set<VmodlVersion> vmodlParents = vmodlVersion.getParentVersion();
      for (VmodlVersion parent : vmodlParents)
        compatibleVersions.addAll(getCompatible(parent, versionNamespace)); 
    } 
    return compatibleVersions;
  }
}

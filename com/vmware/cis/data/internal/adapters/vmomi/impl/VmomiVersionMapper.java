package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.vim.binding.cis.data.provider.version.internal.version1;
import com.vmware.vim.binding.cis.data.provider.version.internal.version12;
import com.vmware.vim.binding.cis.data.provider.version.internal.version13;
import com.vmware.vim.binding.cis.data.provider.version.internal.versions;
import com.vmware.vim.binding.vim.version.internal.version11;
import com.vmware.vim.binding.vim.version.internal.version12;
import com.vmware.vim.binding.vim.version.internal.version13;
import com.vmware.vim.binding.vim.version.internal.versions;
import java.util.LinkedHashMap;
import java.util.Map;

public class VmomiVersionMapper {
  private static Map<Class<?>, Class<?>> publicDpVersionByVimVersion = new LinkedHashMap<>();
  
  private static Map<Class<?>, Class<?>> devDpVersionByVimVersion = new LinkedHashMap<>();
  
  private static Map<String, Class<?>> dpVersionByServiceVersion = new LinkedHashMap<>();
  
  static {
    publicDpVersionByVimVersion.put(version11.class, version1.class);
    publicDpVersionByVimVersion.put(version12.class, version12.class);
    publicDpVersionByVimVersion.put(version13.class, version13.class);
    devDpVersionByVimVersion.put(versions.VIM_VERSION_LTS, versions.CIS_DATA_PROVIDER_VERSION_LTS);
    devDpVersionByVimVersion.put(versions.VIM_VERSION_NEWEST, versions.CIS_DATA_PROVIDER_VERSION_NEWEST);
    dpVersionByServiceVersion.put("6.5", version11.class);
    dpVersionByServiceVersion.put("6.7", version12.class);
  }
  
  static Class<?> getVersion(Class<?> vcVersion, boolean useUnstable) {
    assert vcVersion != null;
    Class<?> version = publicDpVersionByVimVersion.get(vcVersion);
    if (version != null)
      return version; 
    if (useUnstable)
      return versions.CIS_DATA_PROVIDER_VERSION_NEWEST; 
    version = devDpVersionByVimVersion.get(vcVersion);
    if (version != null)
      return version; 
    throw new IllegalArgumentException("Version " + vcVersion + " not supported");
  }
  
  public static Class<?> getVcVersion(String serviceVersion) {
    return dpVersionByServiceVersion.get(serviceVersion);
  }
}

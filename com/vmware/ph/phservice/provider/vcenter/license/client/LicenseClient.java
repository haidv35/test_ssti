package com.vmware.ph.phservice.provider.vcenter.license.client;

import com.vmware.vim.binding.cis.license.management.AssetManagementService;
import com.vmware.vim.binding.cis.license.management.SystemManagementService;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.sso.client.SamlToken;
import java.io.Closeable;
import java.security.PrivateKey;

public interface LicenseClient extends Closeable {
  AssetManagementService getAssetManagementService();
  
  SystemManagementService getSystemManagementService();
  
  <T extends com.vmware.vim.binding.vmodl.ManagedObject> T getManagedObject(Class<T> paramClass, ManagedObjectReference paramManagedObjectReference);
  
  void login(SamlToken paramSamlToken, PrivateKey paramPrivateKey);
  
  void logout();
  
  void close();
}

package com.vmware.ph.phservice.common.cis.security;

import com.vmware.ph.phservice.common.security.KeyStoreProvider;
import java.security.KeyStore;
import java.security.KeyStoreException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VecsKeyStoreProvider implements KeyStoreProvider {
  private static final Log _log = LogFactory.getLog(VecsKeyStoreProvider.class);
  
  private static final String VECS_LOAD_PARAMETER_CLASSNAME = "com.vmware.provider.VecsLoadStoreParameter";
  
  private final String _storeName;
  
  public VecsKeyStoreProvider(String storeName) {
    this._storeName = storeName;
  }
  
  public KeyStore getKeyStore() {
    try {
      KeyStore keyStore = KeyStore.getInstance("VKS");
      KeyStore.LoadStoreParameter loadParam = Class.forName("com.vmware.provider.VecsLoadStoreParameter").getConstructor(new Class[] { String.class }).newInstance(new Object[] { this._storeName });
      keyStore.load(loadParam);
      return keyStore;
    } catch (KeyStoreException|java.security.NoSuchAlgorithmException|java.security.cert.CertificateException|java.io.IOException|InstantiationException|IllegalAccessException|IllegalArgumentException|java.lang.reflect.InvocationTargetException|NoSuchMethodException|SecurityException|ClassNotFoundException e) {
      _log.warn("Did not manage to load a VECS keystore. This might lead to unsuccessful HTTPS connections and not properly authenticated API calls.");
      return null;
    } 
  }
}

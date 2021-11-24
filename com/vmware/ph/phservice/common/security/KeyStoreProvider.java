package com.vmware.ph.phservice.common.security;

import java.security.KeyStore;

public interface KeyStoreProvider {
  KeyStore getKeyStore();
}

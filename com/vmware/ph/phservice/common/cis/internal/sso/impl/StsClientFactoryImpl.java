package com.vmware.ph.phservice.common.cis.internal.sso.impl;

import com.vmware.ph.phservice.common.cis.internal.sso.AdminClient;
import com.vmware.ph.phservice.common.cis.internal.sso.AdminClientFactory;
import com.vmware.ph.phservice.common.cis.internal.sso.SsoEndpoint;
import com.vmware.ph.phservice.common.cis.internal.sso.SsoEndpointProvider;
import com.vmware.ph.phservice.common.cis.internal.sso.StsClient;
import com.vmware.ph.phservice.common.cis.internal.sso.StsClientFactory;
import com.vmware.ph.phservice.common.internal.security.CertUtil;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import com.vmware.vim.vmomi.client.http.impl.AllowKnownThumbprintVerifier;
import java.net.URI;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;

public class StsClientFactoryImpl implements StsClientFactory {
  private final SsoEndpointProvider _ssoEndpointProvider;
  
  private final KeyStore _trustStore;
  
  private final ThumbprintVerifier _thumbprintVerifier;
  
  private final ExecutorService _threadPool;
  
  public StsClientFactoryImpl(SsoEndpointProvider ssoEndpointProvider, KeyStore trustStore, ThumbprintVerifier thumbprintVerifier, ExecutorService threadPool) {
    this._ssoEndpointProvider = ssoEndpointProvider;
    this._trustStore = trustStore;
    this._thumbprintVerifier = thumbprintVerifier;
    this._threadPool = threadPool;
  }
  
  public StsClient createStsClient() throws Exception {
    AllowKnownThumbprintVerifier allowKnownThumbprintVerifier;
    SsoEndpoint adminEndpoint = this._ssoEndpointProvider.getAdminEndpoint();
    SsoEndpoint stsEndpoint = this._ssoEndpointProvider.getStsEndpoint();
    String[] adminTrustedCertThumbprints = getTrustedCertsThumbprints(adminEndpoint);
    ThumbprintVerifier thumbprintVerifier = this._thumbprintVerifier;
    if (adminTrustedCertThumbprints != null)
      allowKnownThumbprintVerifier = new AllowKnownThumbprintVerifier(adminTrustedCertThumbprints); 
    AdminClientFactory adminClientFactory = new AdminClientFactoryImpl(adminEndpoint, this._trustStore, (ThumbprintVerifier)allowKnownThumbprintVerifier);
    X509Certificate[] signingCertificates = null;
    AdminClient adminClient = adminClientFactory.createAnonymousAdminClient();
    try {
      String[] signingCertificatesStringArray = adminClient.getConfigurationManagementService().getIssuersCertificates();
      signingCertificates = CertUtil.decodeCerts(signingCertificatesStringArray);
    } finally {
      adminClient.close();
    } 
    URI stsUri = stsEndpoint.getUrl();
    String[] stsTrustedCertThumbprints = getTrustedCertsThumbprints(stsEndpoint);
    StsClientImpl stsClientHelper = new StsClientImpl(stsUri.toURL(), this._trustStore, stsTrustedCertThumbprints[stsTrustedCertThumbprints.length - 1], signingCertificates);
    stsClientHelper.setExecutorService(this._threadPool);
    return stsClientHelper;
  }
  
  private String[] getTrustedCertsThumbprints(SsoEndpoint ssoEndpoint) throws NoSuchAlgorithmException, CertificateException {
    String[] sslTrust = ssoEndpoint.getSslTrust();
    if (sslTrust == null || sslTrust.length == 0)
      return null; 
    X509Certificate[] trustedCertificates = CertUtil.decodeCerts(ssoEndpoint.getSslTrust());
    String[] trustedCertThumbprints = CertUtil.getCertsThumbprints(trustedCertificates);
    return trustedCertThumbprints;
  }
}

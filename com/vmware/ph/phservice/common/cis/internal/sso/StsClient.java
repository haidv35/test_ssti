package com.vmware.ph.phservice.common.cis.internal.sso;

import com.vmware.vim.sso.client.SamlToken;
import com.vmware.vim.sso.client.exception.CertificateValidationException;
import com.vmware.vim.sso.client.exception.InvalidTokenException;
import com.vmware.vim.sso.client.exception.TokenRequestRejectedException;
import java.security.Key;
import java.security.cert.X509Certificate;

public interface StsClient {
  SamlToken acquireBearerToken(String paramString, char[] paramArrayOfchar, long paramLong) throws InvalidTokenException, TokenRequestRejectedException, CertificateValidationException;
  
  SamlToken acquireTokenByCertificate(X509Certificate paramX509Certificate, Key paramKey, long paramLong) throws InvalidTokenException, TokenRequestRejectedException, CertificateValidationException;
}

package com.vmware.cis.data.internal.util;

import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.vim.sso.client.ConfirmationType;
import com.vmware.vim.sso.client.SamlToken;
import java.security.PrivateKey;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

public final class FixedAuthenticationTokenSource implements AuthenticationTokenSource {
  private final SamlToken _token;
  
  private final PrivateKey _confirmationKey;
  
  public FixedAuthenticationTokenSource(SamlToken token, PrivateKey confirmationKey) {
    Validate.notNull(token, "Argument `token' is required.");
    if (token.getConfirmationType() == ConfirmationType.HOLDER_OF_KEY)
      Validate.notNull(confirmationKey, "Argument `confirmationKey' for holder-of-key tokens."); 
    this._token = token;
    this._confirmationKey = confirmationKey;
  }
  
  public SamlToken getAuthenticationToken() {
    return this._token;
  }
  
  public PrivateKey getConfirmationKey() {
    return this._confirmationKey;
  }
  
  public boolean equals(Object right) {
    boolean areEqual;
    if (!(right instanceof FixedAuthenticationTokenSource)) {
      areEqual = false;
    } else {
      FixedAuthenticationTokenSource other = (FixedAuthenticationTokenSource)right;
      return (this._token.equals(other._token) && 
        ObjectUtils.equals(this._confirmationKey, other._confirmationKey));
    } 
    return areEqual;
  }
  
  public int hashCode() {
    return (new HashCodeBuilder(11, 31))
      .append(this._token)
      .append(this._confirmationKey)
      .toHashCode();
  }
}

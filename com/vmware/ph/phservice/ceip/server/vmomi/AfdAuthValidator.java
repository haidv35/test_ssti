package com.vmware.ph.phservice.ceip.server.vmomi;

import com.vmware.af.VmAfClient;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.sharedcfg.server.auth.AuthValidator;
import com.vmware.ph.sso.StsTrustedCertificatesFactory;
import com.vmware.vim.vmomi.server.Activation;
import com.vmware.vim.vmomi.server.ActivationValidator;

public class AfdAuthValidator implements ActivationValidator {
  private final StsTrustedCertificatesFactory _stsTrustedCertificatesFactory;
  
  private final Builder<VmAfClient> _vmAfClientBuilder;
  
  private ActivationValidator _authValidator;
  
  public AfdAuthValidator(StsTrustedCertificatesFactory stsTrustedCertificatesFactory, Builder<VmAfClient> vmAfClientBuilder) {
    this._stsTrustedCertificatesFactory = stsTrustedCertificatesFactory;
    this._vmAfClientBuilder = vmAfClientBuilder;
  }
  
  public void validate(Activation activation, ActivationValidator.Future future) {
    initAuthValidator();
    this._authValidator.validate(activation, future);
  }
  
  private void initAuthValidator() {
    if (this._authValidator == null) {
      String domainName = getDomainName();
      this._authValidator = (ActivationValidator)new AuthValidator(this._stsTrustedCertificatesFactory, domainName);
    } 
  }
  
  private String getDomainName() {
    VmAfClient vmAfClient = (VmAfClient)this._vmAfClientBuilder.build();
    String domainName = vmAfClient.getDomainName();
    return domainName;
  }
}

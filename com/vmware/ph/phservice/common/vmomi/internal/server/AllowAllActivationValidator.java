package com.vmware.ph.phservice.common.vmomi.internal.server;

import com.vmware.vim.vmomi.server.Activation;
import com.vmware.vim.vmomi.server.ActivationValidator;

public class AllowAllActivationValidator implements ActivationValidator {
  public void validate(Activation activation, ActivationValidator.Future future) {
    future.setValid();
  }
}

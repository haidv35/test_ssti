package com.vmware.ph.phservice.common.vim.internal.vc;

import com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderException;
import com.vmware.vim.binding.vim.SessionManager;
import com.vmware.vim.binding.vim.UserSession;
import com.vmware.vim.binding.vim.fault.InvalidLocale;
import com.vmware.vim.binding.vim.fault.InvalidLogin;

public interface LoginHelper {
  UserSession login(SessionManager paramSessionManager) throws InvalidLogin, InvalidLocale, SsoTokenProviderException;
}

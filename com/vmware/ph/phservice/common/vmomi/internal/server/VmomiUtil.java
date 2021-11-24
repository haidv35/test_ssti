package com.vmware.ph.phservice.common.vmomi.internal.server;

import com.vmware.ph.phservice.common.internal.i18n.LocalizedMessageProvider;
import com.vmware.vim.binding.impl.vmodl.LocalizableMessageImpl;
import com.vmware.vim.binding.vmodl.LocalizableMessage;
import com.vmware.vim.binding.vmodl.RuntimeFault;
import com.vmware.vim.vmomi.core.RequestContext;
import com.vmware.vim.vmomi.server.Activation;
import com.vmware.vim.vmomi.server.common.Request;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmomiUtil {
  private static final Logger _logger = LoggerFactory.getLogger(VmomiUtil.class);
  
  public static final String ANALYTICS_GENERAL_RUNTIMEFAULT = "com.vmware.vsphere.analytics.general.runtimefault";
  
  public static String getCookieFromRequest(String sessionCookieKey, Activation activation) {
    String sessionCookie = null;
    if (activation != null && activation.getRequest() != null) {
      RequestContext requestContext = getRequestContext(activation);
      if (requestContext != null && requestContext.containsKey(sessionCookieKey))
        sessionCookie = (String)requestContext.get(sessionCookieKey); 
    } 
    return sessionCookie;
  }
  
  public static RequestContext getRequestContext(Activation activation) {
    Request request = activation.getRequest();
    return request.getRequestContext();
  }
  
  public static RuntimeFault generateRuntimeFault(LocalizedMessageProvider localizedMessageProvider, String key, Exception e) {
    String errorMessage = localizedMessageProvider.getMessage(key, Locale.ENGLISH);
    if (_logger.isDebugEnabled()) {
      _logger.debug(errorMessage, e);
    } else {
      _logger.error(errorMessage);
    } 
    String generalMessage = localizedMessageProvider.getMessage("com.vmware.vsphere.analytics.general.runtimefault", Locale.ENGLISH);
    RuntimeFault fault = new RuntimeFault();
    fault.setMessage(errorMessage);
    List<LocalizableMessage> localizableMessages = new ArrayList<>();
    localizableMessages.add(new LocalizableMessageImpl("com.vmware.vsphere.analytics.general.runtimefault", null, generalMessage));
    localizableMessages.add(new LocalizableMessageImpl(key, null, errorMessage));
    fault.setFaultMessage(localizableMessages.<LocalizableMessage>toArray(new LocalizableMessage[0]));
    if (e != null)
      fault.setStackTrace(e.getStackTrace()); 
    return fault;
  }
}

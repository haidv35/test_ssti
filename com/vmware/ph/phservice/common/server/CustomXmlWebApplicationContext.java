package com.vmware.ph.phservice.common.server;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class CustomXmlWebApplicationContext extends XmlWebApplicationContext implements ApplicationContextAware {
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    setParent(applicationContext);
  }
}

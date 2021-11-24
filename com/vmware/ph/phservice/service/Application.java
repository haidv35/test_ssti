package com.vmware.ph.phservice.service;

import com.vmware.vim.vmomi.server.http.Server;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {
  private final String[] _configLocations;
  
  private ClassPathXmlApplicationContext _context;
  
  private Server _server;
  
  public Application(String[] configLocations) {
    this._configLocations = configLocations;
  }
  
  public Application(String[] configLocations, String... additionalConfigLocations) {
    if (additionalConfigLocations != null) {
      List<String> locations = new ArrayList<>();
      locations.addAll(Arrays.asList(configLocations));
      locations.addAll(Arrays.asList(additionalConfigLocations));
      this._configLocations = locations.<String>toArray(new String[locations.size()]);
    } else {
      this._configLocations = configLocations;
    } 
  }
  
  public AbstractApplicationContext getContext() {
    if (this._context == null)
      return null; 
    return (AbstractApplicationContext)this._context;
  }
  
  public void start() {
    if (this._context == null)
      this._context = new ClassPathXmlApplicationContext(this._configLocations); 
    this._server = (Server)this._context.getBean("phWebServer");
  }
  
  public void await() {
    (new Thread() {
        public void run() {
          try {
            Application.this._server.join();
          } catch (Exception exception) {}
        }
      }).start();
  }
  
  public void stop() {
    if (this._context != null) {
      this._context.close();
      this._context = null;
    } 
  }
}

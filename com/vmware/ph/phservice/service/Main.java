package com.vmware.ph.phservice.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Main {
  private static final Log _log = LogFactory.getLog(Main.class);
  
  public static void main(String[] args) {
    (new VMonApplicationWrapper()).start(args);
  }
  
  private static class VMonApplicationWrapper {
    private Application _theApp;
    
    private VMonApplicationWrapper() {}
    
    public void start(String[] args) {
      if (Main._log.isInfoEnabled())
        Main._log.info("Starting service"); 
      try {
        if (this._theApp == null) {
          this._theApp = new Application(args);
          this._theApp.start();
          Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                  if (Main._log.isInfoEnabled())
                    Main._log.info("Stopping service"); 
                  if (Main.VMonApplicationWrapper.this._theApp != null)
                    Main.VMonApplicationWrapper.this._theApp.stop(); 
                  if (Main._log.isInfoEnabled())
                    Main._log.info("Service successfully stopped"); 
                }
              });
        } 
        if (Main._log.isInfoEnabled())
          Main._log.info("Service successfully started"); 
        this._theApp.await();
      } catch (Throwable t) {
        if (Main._log.isFatalEnabled())
          Main._log.fatal("Error starting service: ", t); 
      } 
    }
  }
}

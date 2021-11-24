package com.vmware.ph.phservice.common.server;

import com.vmware.ph.phservice.common.threadstate.ThreadActiveStateManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HealthStatusServlet extends HttpServlet {
  private static final String HEALTH_STATUS_RESPONSE = "<healthStatus schemaVersion=\"1.0\" xmlns=\"http://www.vmware.com/cis/cm/common/jaxb/healthstatus\">\n   <status>%s</status>\n</healthStatus>";
  
  private static final long serialVersionUID = 1L;
  
  private static final Log _log = LogFactory.getLog(HealthStatusServlet.class);
  
  private final ThreadActiveStateManager _threadActiveStateManager;
  
  private enum HealthStatusEnum {
    GREEN("GREEN"),
    RED("RED");
    
    private String status;
    
    HealthStatusEnum(String status) {
      this.status = status;
    }
    
    public String getStatus() {
      return this.status;
    }
  }
  
  public HealthStatusServlet(ThreadActiveStateManager threadActiveStateManager) {
    this._threadActiveStateManager = threadActiveStateManager;
  }
  
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    response.setHeader("Content-Type", "application/xml; charset=utf-8");
    try {
      ServletOutputStream servletOutputStream = response.getOutputStream();
      HealthStatusEnum healthStatus = HealthStatusEnum.GREEN;
      if (this._threadActiveStateManager.hasInactiveThreads())
        healthStatus = HealthStatusEnum.RED; 
      String healthStatusResponse = String.format("<healthStatus schemaVersion=\"1.0\" xmlns=\"http://www.vmware.com/cis/cm/common/jaxb/healthstatus\">\n   <status>%s</status>\n</healthStatus>", new Object[] { healthStatus.getStatus() });
      servletOutputStream.write(healthStatusResponse.getBytes("UTF-8"));
      servletOutputStream.flush();
    } catch (Exception e) {
      _log.warn("Unable to set health status: ", e);
    } 
  }
}

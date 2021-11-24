package com.vmware.ph.phservice.common.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

public class FileServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  private static final String CONTENT_TYPE = "application/jar";
  
  private static final Log _log = LogFactory.getLog(FileServlet.class);
  
  private final File _file;
  
  public FileServlet(Resource resource) throws IOException {
    this._file = new File(resource.getFile().getAbsolutePath());
  }
  
  protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    try {
      if (_log.isDebugEnabled())
        _log.debug(String.format("doGet: Entering (%s)", new Object[] { httpServletRequest
                .getRequestURI() })); 
      httpServletResponse.setContentType("application/jar");
      httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + this._file
          
          .getName() + "\"");
      httpServletResponse.setHeader("Content-Length", 
          
          String.valueOf(this._file.length()));
      ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
      InputStream inputStream = null;
      try {
        inputStream = new FileInputStream(this._file);
        IOUtils.copy(inputStream, (OutputStream)servletOutputStream);
      } finally {
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly((OutputStream)servletOutputStream);
      } 
      if (_log.isDebugEnabled())
        _log.debug(String.format("doGet: Leaving (%s)", new Object[] { httpServletRequest
                .getRequestURI() })); 
    } catch (Throwable t) {
      if (_log.isErrorEnabled())
        _log.error("doGet: Failed to stream data", t); 
    } 
  }
}

package com.vmware.ph.phservice.common.server;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NoOpServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    respondWith404(req, resp);
  }
  
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    respondWith404(req, resp);
  }
  
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    respondWith404(req, resp);
  }
  
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    respondWith404(req, resp);
  }
  
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    respondWith404(req, resp);
  }
  
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    respondWith404(req, resp);
  }
  
  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    respondWith404(req, resp);
  }
  
  private void respondWith404(HttpServletRequest req, HttpServletResponse resp) {
    resp.setStatus(404);
  }
}

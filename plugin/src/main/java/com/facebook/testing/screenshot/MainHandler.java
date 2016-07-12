package com.facebook.testing.screenshot;

import org.eclipse.jetty.server.*;
import java.util.*;
import java.io.IOException;
import org.eclipse.jetty.server.handler.AbstractHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class MainHandler extends AbstractHandler {
  @Override
  public void handle(
      String target,
      Request baseRequest,
      HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    String basePath = baseRequest.getPathInfo();
    if (basePath != null && basePath.equals("/404")) {
      return;
    }

    response.setContentType("html/text");
    response.setStatus(200);
    response.getWriter().write("<html><body>OK</body></html>");
    response.flushBuffer();
    baseRequest.setHandled(true);
  }
}

package com.facebook.testing.screenshot;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class MainHandler extends AbstractHandler {
  private File mRoot;

  public MainHandler(File root) {
    mRoot = root;
  }

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

    response.setContentType("text/html");
    response.setStatus(200);

    PrintWriter writer = new PrintWriter(response.getWriter(), true);
    writer.println("<!DOCTYPE html>");
    writer.println("<html>");
    writer.println("<body>");
    writer.println("OK");
    writer.println("</body>");
    writer.println("</html>");
    response.flushBuffer();
    baseRequest.setHandled(true);
  }
}

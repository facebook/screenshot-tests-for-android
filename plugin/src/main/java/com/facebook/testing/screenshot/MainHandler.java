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
  private File mMetadataFile;

  public MainHandler(File metadataFile) {
    mMetadataFile = metadataFile;
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

    response.setContentType("html/text");
    response.setStatus(200);

    Writer writer = response.getWriter();
    writer.write("<html><body>OK</body></html>");
    response.flushBuffer();
    baseRequest.setHandled(true);
  }
}

package com.facebook.testing.screenshot;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.*;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.eclipse.jetty.server.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
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
    writer.println("<!DOCTYPE html>");
    writer.write("<head>");
    writer.write("<script src='https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js'></script>");
    writer.write("<script src='https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.3/jquery-ui.min.js'></script>");
    writer.write("<script src='default.js'></script>");
    writer.write("<link rel='stylesheet' href='https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.3/themes/smoothness/jquery-ui.css' />");
    writer.write("<link rel='stylesheet' href='default.css'></head>");

    writer.println("<html>");

    printDetails(new File(mRoot, "metadata.xml"), writer);

    writer.println("<body>");

    writer.println("</body>");
    writer.println("</html>");
    response.flushBuffer();
    baseRequest.setHandled(true);
  }

  private void printDetails(File metadata, PrintWriter writer) {
    SAXParserFactory factory = SAXParserFactory.newInstance();

    try {
      SAXParser parser = factory.newSAXParser();
      parser.parse(new FileInputStream(metadata), new DefaultHandler() {
          @Override
          public void startElement(String uri, String localName, String qName, Attributes attr) {
            writer.println("foo");
          }
        });
    } catch (ParserConfigurationException|IOException|SAXException e) {
      throw new RuntimeException(e);
    }
  }
}

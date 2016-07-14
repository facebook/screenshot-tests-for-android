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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;


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


    if (basePath != null && basePath.equals("/default.css")) {
      writeResource("default.css", "text/css", response);
      return;
    }

    response.setContentType("text/html");
    response.setStatus(200);
    PrintWriter writer = new PrintWriter(response.getWriter(), true);
    writer.println(String.valueOf(basePath));

    writer.println("<!DOCTYPE html>");
    writer.println("<!DOCTYPE html>");
    writer.write("<head>");
    writer.write("<script src='https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js'></script>");
    writer.write("<script src='https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.3/jquery-ui.min.js'></script>");
    writer.write("<script src='/default.js'></script>");
    writer.write("<link rel='stylesheet' href='https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.3/themes/smoothness/jquery-ui.css' />");
    writer.write("<link rel='stylesheet' href='/default.css'></head>");

    writer.println("<html>");

    printDetails(new File(mRoot, "metadata.xml"), writer);

    writer.println("<body>");

    writer.println("</body>");
    writer.println("</html>");
    response.flushBuffer();
    baseRequest.setHandled(true);
  }

  private void writeResource(String name, String mime, HttpServletResponse response) throws IOException, ServletException {
    response.setStatus(200);
    response.setContentType(mime);
    try (InputStream stream = getClass().getResourceAsStream(name)) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      String line;
      PrintWriter writer = new PrintWriter(response.getWriter(), true);
      while ((line = reader.readLine()) != null) {
        writer.println(line);
      }
    }
  }

  static class Element {
    String name;
  }

  private void printDetails(File metadata, final PrintWriter writer) {
    if (!metadata.exists()) {
      writer.println("could not find metadata file");
      return;
    }
    boolean alternate = false;
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = dbFactory.newDocumentBuilder();
      Document doc = builder.parse(metadata);

      NodeList list = doc.getElementsByTagName("screenshot");

      for (int i = 0; i < list.getLength(); i++) {
        Node node = list.item(i);
        alternate = !alternate;
        writer.println(String.format("<div class='screenshot %s'>", alternate ? "alternate" : ""));
        writer.println("<div class='screenshot_name'>");

        writeExtra(node ,writer);
        writer.println(getChildNode(node, "name").getTextContent());
        writer.println("</div>");

        Node description = getChildNode(node, "description");
        if (description != null) {
          writer.println("<div class='screenshot_description'>");
          writer.println(description.getTextContent());
          writer.println("</div>");
        }


        if (getChildNode(node, "error") != null) {
          writer.println("<div class='screenshot_error'>" + getChildNode(node, "error").getTextContent() + "</div>");
        } else {
          writeImage(node, writer);
        }

        writer.println("</div>");
      }

      writer.println("END");
    } catch (SAXException|ParserConfigurationException|IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeExtra(Node node, PrintWriter writer) {
    Node extras = getChildNode(node, "extras");
    if (extras == null) {
      return;
    }

    String str = "";
    for (int i = 0; i < extras.getChildNodes().getLength(); i++) {
      writer.println("extras unsupported");
    }
  }

  private void writeImage(Node node, PrintWriter writer) {
    writer.println("<table class='img-wrapper'>");
    for (int y = 0; y < Integer.parseInt(getChildNode(node, "tile_height").getTextContent());
         y++) {
      writer.println("<tr>");
      for (int x = 0; x < Integer.parseInt(getChildNode(node, "tile_width").getTextContent());
           x++) {
        writer.println("<td>");
        String fileName = getTileName(getChildNode(node, "name").getTextContent(), x, y);
        writer.println("<img src='/s/" + fileName + ".png' />");
        writer.println("</td>");
      }
      writer.println("</tr>");
    }
    writer.println("</table>");
  }

  private String getTileName(String name, int x, int y) {
    if (x == 0 && y == 0) {
      return name;
    }
    return name + "_" + x + "_" + y;
  }

  private Node getChildNode(Node node, String name) {
    NodeList list = node.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      if (list.item(i).getNodeName().equals(name)) {
        return list.item(i);
      }
    }

    return null;
  }
}

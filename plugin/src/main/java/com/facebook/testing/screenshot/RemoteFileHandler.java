// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import org.eclipse.jetty.server.handler.*;
import com.android.ddmlib.IDevice;
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

public class RemoteFileHandler extends AbstractHandler {
  private IDevice mDevice;
  private String mPath;

  public RemoteFileHandler(IDevice device, String path) {
    mDevice = device;
    mPath = path;
  }

  @Override
  public void handle(
      String target,
      Request baseRequest,
      HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

  }
}

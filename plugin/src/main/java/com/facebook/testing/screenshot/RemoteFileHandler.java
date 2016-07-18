// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import org.eclipse.jetty.server.handler.*;
import com.android.ddmlib.*;
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
    String remoteFile = getRemoteFile(target);
    try {
      File tempFile = File.createTempFile("screenshot", "png");
      mDevice.pullFile(remoteFile, tempFile.getAbsolutePath());

      OutputStream os = response.getOutputStream();
      try (FileInputStream is = new FileInputStream(tempFile)) {
        int b;
        while ((b = is.read()) != -1) {
          os.write(b);
        }
      }
      os.flush();
    } catch (AdbCommandRejectedException|TimeoutException|SyncException e) {
      throw new IOException(e);
    }
  }

  private String getRemoteFile(String target) {
    return mPath + target;
  }
}

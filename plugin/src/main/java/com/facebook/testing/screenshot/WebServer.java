// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import java.io.File;
import java.util.*;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class WebServer {
  private int mPort;
  private Server mServer;
  private File mRoot;

  public WebServer(int port) {
    mPort = port;
  }

  public void setRoot(File file) {
    mRoot = file;
  }

  public void start() {
    mServer = new Server(mPort);

    Handler rootHandler = new MainHandler(mRoot);
    ContextHandler rootContextHandler = new ContextHandler("/");
    rootContextHandler.setHandler(rootHandler);

    ResourceHandler resourceHandler = new ResourceHandler();

    if (mRoot != null) {
      resourceHandler.setDirectoriesListed(true);
      resourceHandler.setResourceBase(mRoot.getAbsolutePath());
    }

    Handler[] handlers = new Handler[] {
      rootContextHandler,
      resourceHandler,
    };

    ContextHandlerCollection contexts = new ContextHandlerCollection();
    contexts.setHandlers(handlers);
    mServer.setHandler(contexts);
    try {
      mServer.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    if (mServer != null) {
      try {
        mServer.stop();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void main(String[] args) {
    WebServer server = new WebServer(10001);
    server.setRoot(new File(args[0]));
    server.start();
  }
}

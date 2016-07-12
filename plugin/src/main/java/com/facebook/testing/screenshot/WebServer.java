// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import java.io.File;
import java.util.*;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class WebServer {
  private int mPort;
  private Server mServer;
  private File mMetatdataFile;

  public WebServer(int port) {
    mPort = port;
  }

  public void setMetadataFile(File file) {
    mMetatdataFile = file;
  }

  public void start() {
    mServer = new Server(mPort);

    Handler rootHandler = new MainHandler();
    ContextHandler rootContextHandler = new ContextHandler("/");
    rootContextHandler.setHandler(rootHandler);

    Handler[] handlers = new Handler[] {
      rootContextHandler
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

}

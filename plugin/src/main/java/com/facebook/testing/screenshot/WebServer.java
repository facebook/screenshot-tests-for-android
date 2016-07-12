// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import org.eclipse.jetty.server.*;

public class WebServer {
  private int mPort;
  private Server mServer;

  public WebServer(int port) {
    mPort = port;
  }

  public void start() {
    mServer = new Server(mPort);
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

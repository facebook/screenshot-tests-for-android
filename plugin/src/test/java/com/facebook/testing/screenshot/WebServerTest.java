// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import org.junit.Before;
import static org.junit.Assert.*;

public class WebServerTest {
  int PORT = 10021;

  private WebServer mWebServer;

  @Before
  public void before() throws Throwable {
    mWebServer = new WebServer(PORT);
  }
}

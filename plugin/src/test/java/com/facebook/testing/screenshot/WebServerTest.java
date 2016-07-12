// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.net.URL;
import java.io.*;
import static org.hamcrest.Matchers.*;

public class WebServerTest {
  int PORT = 10021;

  private WebServer mWebServer;

  @Before
  public void before() throws Throwable {
    mWebServer = new WebServer(PORT);
    mWebServer.start();
  }

  @After
  public void after() throws Throwable {
    mWebServer.stop();
  }

  @Test
  public void testAccessEndpoint() throws Throwable {
    URL url = new URL("http://localhost:" + PORT + "/404");
    try (InputStream connection = url.openStream()) {
      fail("expected exception");
    } catch (FileNotFoundException e) {
      // at least we're connected
    }
  }

  @Test
  public void testAssertMainHandler() throws Throwable {
    URL url = new URL("http://localhost:" + PORT + "/");
    try (InputStream connection = url.openStream()) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection));
      assertThat(reader.readLine(), containsString("html"));
    }
  }
}

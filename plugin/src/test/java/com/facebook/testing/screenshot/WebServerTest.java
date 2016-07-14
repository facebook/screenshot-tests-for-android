// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import java.io.*;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class WebServerTest {
  int PORT = 10021;

  private WebServer mWebServer;

  @Rule
  public TemporaryFolder mTempDir = new TemporaryFolder();

  @Before
  public void before() throws Throwable {
    mWebServer = new WebServer(PORT);
    mWebServer.start();

    File file = mTempDir.newFile("metadata.xml");
    Files.write("<screenshots></screenshots>", file, Charsets.UTF_8);
    mWebServer.setRoot(mTempDir.getRoot());
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

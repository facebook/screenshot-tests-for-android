// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import java.io.*;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.*;
import org.junit.Rule;
import org.junit.rules.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MainHandlerTest {
  @Rule
  public TemporaryFolder mTempDir = new TemporaryFolder();

  String oneScreenshot =
      "  <screenshot>\n" +
      "    <description />\n" +
      "    " +
      " <name>com.foo.ScriptsFixtureTest_testGetTextViewScreenshot</name>\n" +
      "    <test_class>\n" +
      "    " +
      " com.facebook.testing.screenshot.ScriptsFixtureTest</test_class>\n" +
      "    " +
      " <test_name>testGetTextViewScreenshot</test_name>\n" +
      "    <tile_width>1</tile_width>\n" +
      "    <tile_height>1</tile_height>\n" +
      "    " +
      " <relative_file_name>com.foo.ScriptsFixtureTest_testGetTextViewScreenshot.png</relative_file_name>\n" +
      "    " +
      " <view_hierarchy>one_dump.xml</view_hierarchy>\n" +
      "  </screenshot>\n";


  @Test
  public void testBasics() throws Throwable {
    File file = writeMetadata("<screenshots></screenshots>");
    MainHandler handler = new MainHandler(file);

    String output = getOutput(handler);
    assertThat(output, containsString("<html>"));
    assertThat(output, containsString("</html>"));
  }

  @Test
  public void testSingleScreenshot() throws Throwable {
  }

  private File writeMetadata(String contents) throws IOException {
    File file = mTempDir.newFile("metadata.xml");
    Files.write(contents, file, Charsets.UTF_8);
    return file;
  }

  public String getOutput(MainHandler handler) throws Exception {
    Request request = mock(Request.class);
    StringWriter stringWriter = new StringWriter();
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter, true));

    handler.handle(
        "",
        request,
        servletRequest,
        response);

    return stringWriter.toString();
  }
}

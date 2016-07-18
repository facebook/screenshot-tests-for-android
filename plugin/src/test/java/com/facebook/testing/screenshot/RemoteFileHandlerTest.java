// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.io.ByteArrayInputStream;

import com.android.ddmlib.IDevice;
import org.eclipse.jetty.server.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RemoteFileHandlerTest {
  private IDevice device;
  private RemoteFileHandler handler;
  private Request mRequest;
  private HttpServletRequest mServletRequest;
  private HttpServletResponse mServletResponse;
  private ByteArrayOutputStream mRealStream = new ByteArrayOutputStream();
  private ServletOutputStreamWrapper mStream = new ServletOutputStreamWrapper(mRealStream);

  @Before
  public void before() throws Throwable {
    device = mock(IDevice.class);
    handler = new RemoteFileHandler(device, "/sdcard/foo");
    mRequest = mock(Request.class);
    mServletRequest = mock(HttpServletRequest.class);
    mServletResponse = mock(HttpServletResponse.class);
    when(mServletResponse.getOutputStream()).thenReturn(mStream);
  }

  @Test
  public void testPullsFile() throws Throwable {
    handler.handle("/bleh.png", mRequest, mServletRequest, mServletResponse);
    verify(device).pullFile(eq("/sdcard/foo/bleh.png"), any(String.class));
  }

  @Test
  public void testRetrievesRightContent() throws Throwable {
    doAnswer(
        new Answer() {
          @Override
          public Object answer(InvocationOnMock invocation) {
            String output = (String) invocation.getArguments()[1];
            try (FileOutputStream os = new FileOutputStream(output)) {
              OutputStreamWriter writer = new OutputStreamWriter(os);
              writer.write("foo");
              writer.flush();
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
            return null;
          }
        }).when(device).pullFile(any(String.class), any(String.class));

    handler.handle("/bleh.png", mRequest, mServletRequest, mServletResponse);
    assertEquals("foo", readStream());
  }

  private String readStream() throws IOException {
    byte[] bytes = mRealStream.toByteArray();
    ByteArrayInputStream is = new ByteArrayInputStream(bytes);

    StringBuffer ret = new StringBuffer();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      String line;
      while ((line = reader.readLine()) != null) {
        ret.append(line);
      }
    }

    return ret.toString();
  }
}

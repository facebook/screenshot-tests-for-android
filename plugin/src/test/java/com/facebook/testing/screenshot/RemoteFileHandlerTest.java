// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.fail;
import com.android.ddmlib.IDevice;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.*;

public class RemoteFileHandlerTest {
  private IDevice device;
  private RemoteFileHandler handler;
  private Request mRequest;
  private HttpServletRequest mServletRequest;
  private HttpServletResponse mServletResponse;

  @Before
  public void before() throws Throwable {
    device = mock(IDevice.class);
    handler = new RemoteFileHandler(device, "/sdcard/foo");
    mRequest = mock(Request.class);
    mServletRequest = mock(HttpServletRequest.class);
    mServletResponse = mock(HttpServletResponse.class);
  }

  @Test
  public void testPullsFile() throws Throwable {
    handler.handle("/bleh.png", mRequest, mServletRequest, mServletResponse);
    verify(device).pullFile(eq("/sdcard/foo/bleh.png"), any(String.class));
  }
}

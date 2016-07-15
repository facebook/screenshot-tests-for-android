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

public class RemoteFileHandlerTest {
  private IDevice device;
  private RemoteFileHandler handler;

  @Before
  public void before() throws Throwable {
    device = mock(IDevice.class);
    handler = new RemoteFileHandler(device, "/sdcard/foo/");
  }

  @Test
  public void testHandleRequest() throws Throwable {

  }
}

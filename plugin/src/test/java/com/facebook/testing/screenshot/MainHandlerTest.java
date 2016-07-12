// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import java.io.*;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.*;
import org.junit.Rule;
import org.junit.rules.*;
import static org.junit.Assert.*;

public class MainHandlerTest {
  @Rule
  public TemporaryFolder mTempDir = new TemporaryFolder();

  @Test
  public void testBasics() throws Throwable {
    File file = mTempDir.newFile("metadata.xml");
    Files.write("<screenshots></screenshots>", file, Charsets.UTF_8);
    MainHandler handler = new MainHandler(file);
  }

}

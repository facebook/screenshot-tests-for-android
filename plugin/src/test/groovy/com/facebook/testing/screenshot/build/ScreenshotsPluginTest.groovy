package com.facebook.testing.screenshot.build;

import org.junit.*;
import static org.junit.Assert.*;

class ScreenshotsPluginTest {
  def plugin;

  @Before
  public void setup() {
    plugin = new ScreenshotsPlugin()
  }

  @Test
  public void testOne() {
    assertEquals(true, true)
  }

  @Test
  public void testConstructor() {
  }
}

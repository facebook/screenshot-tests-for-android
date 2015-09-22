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
    assertNotNull(plugin)
  }

  @Test
  public void testConstructor() {
  }
}

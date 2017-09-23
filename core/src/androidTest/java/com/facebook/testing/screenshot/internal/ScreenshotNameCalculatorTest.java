package com.facebook.testing.screenshot.internal;

import com.facebook.testing.screenshot.DeviceIdentifier;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ScreenshotNameCalculator}
 */
public class ScreenshotNameCalculatorTest {

  private static final String NAME = "name";
  private static final String DEVICE_IDENTIFIER = "device-identifier";
  private static final String TEST_CLASS = "test-class";
  private static final String TEST_NAME = "test-name";

  private ScreenshotNameCalculator calculator;
  private DeviceIdentifier deviceIdentifier;

  @Before public void setUp() throws Exception {
    calculator = new ScreenshotNameCalculator();
    deviceIdentifier = mock(DefaultDeviceIdentifier.class);

    doReturn(DEVICE_IDENTIFIER).when(deviceIdentifier).get();
  }

  @Test public void testNameAndNotDeviceIdentifier() {
    final String screenShotName = calculator.calculate(NAME, null,
        TEST_CLASS, TEST_NAME);

    assertEquals(NAME, screenShotName);
  }

  @Test public void testNameAndDeviceIdentifier() {
    final String screenShotName = calculator.calculate(NAME, deviceIdentifier,
        TEST_CLASS, TEST_NAME);

    final String expectedScreenshotName = DEVICE_IDENTIFIER + "_" + NAME;
    assertEquals(expectedScreenshotName, screenShotName);
  }

  @Test public void testNoNameAndDeviceIdentifier() {
    final String screenShotName = calculator.calculate(null, deviceIdentifier,
        TEST_CLASS, TEST_NAME);

    final String expectedScreenshotName = DEVICE_IDENTIFIER + "_" + TEST_CLASS + "_" + TEST_NAME;
    assertEquals(expectedScreenshotName, screenShotName);
  }

}

package com.facebook.testing.screenshot.internal;

import com.facebook.testing.screenshot.DeviceIdentifier;

public class ScreenshotNameCalculator {

  public String calculate(final String name, final DeviceIdentifier deviceIdentifier,
                          final String testClass, final String testName) {
    final boolean validDeviceIdentifier = isValidDeviceIdentifier(deviceIdentifier);
    if (name != null && validDeviceIdentifier) {
      return deviceIdentifier.get() + "_" + name;
    } else if(name == null && validDeviceIdentifier) {
      return deviceIdentifier.get() + "_" + testClass + "_" + testName;
    } else {
      return name;
    }
  }

  private boolean isValidDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
    if (deviceIdentifier == null) return false;
    final String identifier = deviceIdentifier.get();
    return identifier != null && !identifier.isEmpty();
  }
}

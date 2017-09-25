package com.facebook.testing.screenshot.internal;

import com.facebook.testing.screenshot.DeviceIdentifier;

public class ScreenshotNameCalculator {

  public String calculate(final String name, final DeviceIdentifier deviceIdentifier,
                          final String testClass, final String testName) {
    final boolean validDeviceIdentifier = isValidDeviceIdentifier(deviceIdentifier);
    if (name != null && validDeviceIdentifier) {
      return String.format("%s_%s", deviceIdentifier.generateId(), name);
    } else if(name == null && validDeviceIdentifier) {
      return String.format("%s_%s_%s", deviceIdentifier.generateId(), testClass, testName);
    } else {
      return name;
    }
  }

  private boolean isValidDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
    if (deviceIdentifier == null) return false;
    final String identifier = deviceIdentifier.generateId();
    return identifier != null && !identifier.isEmpty();
  }

}

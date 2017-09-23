package com.example.screenshots;

import com.facebook.testing.screenshot.DeviceIdentifier;

public class CustomDeviceIdentifier implements DeviceIdentifier {

  public String get() {
    return "CustomIdentifier";
  }

}
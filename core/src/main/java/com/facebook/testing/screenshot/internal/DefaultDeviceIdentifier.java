package com.facebook.testing.screenshot.internal;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.testing.screenshot.DeviceIdentifier;

public class DefaultDeviceIdentifier implements DeviceIdentifier {

  private static final String PLAY_SERVICES = "GP";
  private static final String NO_PLAY_SERVICES = "NO_GP";

  private final Context context;

  DefaultDeviceIdentifier(Context context) {
    this.context = context;
  }

  @Override
  public String generateId() {
    final int sdkVersion = Build.VERSION.SDK_INT;
    final String googlePlayServicesString = obtainPlayServicesString();
    final String metricsString = obtainMetricsString();
    final String sdkVersionString = String.valueOf(sdkVersion);
    return String.format("API_%s_%s_%s", sdkVersionString, googlePlayServicesString, metricsString);
  }

  private String obtainPlayServicesString() {
    final boolean hasGooglePlayServices = Utils.isGooglePlayInstalled(context);
    return hasGooglePlayServices ? PLAY_SERVICES : NO_PLAY_SERVICES;
  }

  private String obtainMetricsString() {
    final DisplayMetrics metrics = Utils.getDisplayMetrics(context);
    if (metrics == null) return "";
    final int widthPixels = metrics.widthPixels;
    final int heightPixels = metrics.heightPixels;
    return String.format("%s_%s", String.valueOf(heightPixels), String.valueOf(widthPixels));
  }



}

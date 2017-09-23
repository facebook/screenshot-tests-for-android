package com.facebook.testing.screenshot.internal;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.testing.screenshot.DeviceIdentifier;

public class DefaultDeviceIdentifier implements DeviceIdentifier {

  private final Context context;

  DefaultDeviceIdentifier(Context context) {
    this.context = context;
  }

  public String get() {
    final int sdkVersion = Build.VERSION.SDK_INT;
    final boolean hasGooglePlayServices = isGooglePlayInstalled(context);
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics metrics = new DisplayMetrics();
    wm.getDefaultDisplay().getMetrics(metrics);
    final int widthPixels = metrics.widthPixels;
    final int heightPixels = metrics.heightPixels;
    final String googlePlayServices = hasGooglePlayServices ? "GP" : "NO_GP";
    return "API_" + sdkVersion + "_" + googlePlayServices + "_" + heightPixels + "_" + widthPixels;
  }

  private boolean isGooglePlayInstalled(Context context) {
    PackageManager pm = context.getPackageManager();
    boolean app_installed = false;
    try
    {
      PackageInfo info = pm.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES);
      String label = (String) info.applicationInfo.loadLabel(pm);
      app_installed = (label != null && !label.equals("Market"));
    }
    catch (PackageManager.NameNotFoundException e)
    {
      app_installed = false;
    }
    return app_installed;
  }

}

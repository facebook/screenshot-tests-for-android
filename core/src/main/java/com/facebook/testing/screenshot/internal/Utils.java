package com.facebook.testing.screenshot.internal;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

class Utils {

  static DisplayMetrics getDisplayMetrics(Context context) {
    try {
      WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
      DisplayMetrics metrics = new DisplayMetrics();
      wm.getDefaultDisplay().getMetrics(metrics);
      return metrics;
    } catch (NullPointerException exception) {
      return null;
    }
  }

  static boolean isGooglePlayInstalled(Context context) {
    PackageManager pm = context.getPackageManager();
    boolean app_installed = false;
    try {
      PackageInfo info = pm.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES);
      String label = (String) info.applicationInfo.loadLabel(pm);
      app_installed = (label != null && !label.equals("Market"));
    } catch (PackageManager.NameNotFoundException e) {
      app_installed = false;
    }
    return app_installed;
  }
}

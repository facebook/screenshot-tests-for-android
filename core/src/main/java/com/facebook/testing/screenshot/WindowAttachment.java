/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.WeakHashMap;

import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.android.dx.stock.ProxyBuilder;

public abstract class WindowAttachment {

  /**
   * Keep track of all the attached windows here so that we don't
   * double attach them.
   */
  private static final WeakHashMap<View, Boolean> sAttachments = new WeakHashMap<>();

  /**
   * Dispatch onAttachedToWindow to all the views in the view
   * hierarchy.
   *
   * Detach the view by calling {@code detach()} on the returned {@code Detacher}.
   *
   * Note that if the view is already attached (either via
   * WindowAttachment or to a real window), then both the attach and
   * the corresponding detach will be no-ops.
   *
   * Note that this is hacky, after these calls the views will still
   * say that isAttachedToWindow() is false and getWindowToken() ==
   * null.
   */
  public static Detacher dispatchAttach(View view) {
    if (view.getWindowToken() != null || sAttachments.containsKey(view)) {
      // Screnshot tests can often be run against a View that's
      // attached to a real activity, in which case we have nothing to
      // do
      Log.i("WindowAttachment", "Skipping window attach hack since it's really attached");
      return new NoopDetacher();
    }

    sAttachments.put(view, true);
    invoke(view, "onAttachedToWindow");

    return new RealDetacher(view);
  }

  public interface Detacher {
    public void detach();
  }

  private static class NoopDetacher implements Detacher {
    @Override
    public void detach() {}
  }

  private static class RealDetacher implements Detacher {
    private View mView;

    public RealDetacher(View view) {
      mView = view;
    }
    @Override
    public void detach() {
      dispatchDetach(mView);
      sAttachments.remove(mView);
    }
  }

  /**
   * Similar to dispatchAttach, except dispatchest the corresponding
   * detach.
   */
  private static void dispatchDetach(View view) {
    invoke(view, "onDetachedFromWindow");
  }

  private static void invoke(View view, String methodName) {
    invokeUnchecked(view, methodName);
  }

  private static void invokeUnchecked(View view, String methodName) {
    try {
      Method method = View.class.getDeclaredMethod(methodName);
      method.setAccessible(true);
      method.invoke(view);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    if (view instanceof ViewGroup) {
      ViewGroup vg = (ViewGroup) view;
      for (int i = 0 ; i < vg.getChildCount(); i++) {
        invokeUnchecked(vg.getChildAt(i), methodName);
      }
    }
  }

  /**
   * Simulates the view as being attached.
   */
  public static void setAttachInfo(View view) {
    int viewRootWidth = 0;
    int viewRootHeight = 0;
    setAttachInfo(view, viewRootWidth, viewRootHeight);
  }

  /**
   * Simulates the view as being attached.
   */
  public static void setAttachInfo(View view, int viewRooWidth, int viewRootHeight) {
    try {
      Class cAttachInfo = Class.forName("android.view.View$AttachInfo");
      Class cViewRootImpl = null;

      if (Build.VERSION.SDK_INT >= 11) {
        cViewRootImpl = Class.forName("android.view.ViewRootImpl");
      }

      Class cIWindowSession = Class.forName("android.view.IWindowSession");
      Class cIWindow = Class.forName("android.view.IWindow");
      Class cCallbacks = Class.forName("android.view.View$AttachInfo$Callbacks");

      Context context = view.getContext();
      WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();

      Object viewRootImpl = null;

      Object window = createIWindow();

      Class[] params = null;
      Object[] values = null;

      if (Build.VERSION.SDK_INT >= 17) {
        viewRootImpl = cViewRootImpl.getConstructor(Context.class, Display.class)
          .newInstance(context, display);
        params = new Class[] {
          cIWindowSession,
          cIWindow,
          Display.class,
          cViewRootImpl,
          Handler.class,
          cCallbacks
        };

        values = new Object[] {
          stub(cIWindowSession),
          window,
          display,
          viewRootImpl,
          new Handler(),
          stub(cCallbacks)
        };
      }
      else if (Build.VERSION.SDK_INT >= 16) {
        viewRootImpl = cViewRootImpl.getConstructor(Context.class)
          .newInstance(context);
        params = new Class[] {
          cIWindowSession,
          cIWindow,
          cViewRootImpl,
          Handler.class,
          cCallbacks
        };

        values = new Object[] {
          stub(cIWindowSession),
          window,
          viewRootImpl,
          new Handler(),
          stub(cCallbacks)
        };
      }
      else if (Build.VERSION.SDK_INT <= 15) {
        params = new Class[] {
          cIWindowSession,
          cIWindow,
          Handler.class,
          cCallbacks
        };

        values = new Object[] {
          stub(cIWindowSession),
          window,
          new Handler(),
          stub(cCallbacks)
        };
      }

      Object attachInfo = invokeConstructor(cAttachInfo, params, values);

      setField(attachInfo, "mHasWindowFocus", true, cAttachInfo);
      setField(attachInfo, "mWindowVisibility", View.VISIBLE, cAttachInfo);
      setField(attachInfo, "mInTouchMode", false, cAttachInfo);
      setField(view, "mParent", viewRootImpl, View.class);
      setField(viewRootImpl, "mWidth", viewRooWidth, cViewRootImpl);
      setField(viewRootImpl, "mHeight", viewRootHeight, cViewRootImpl);
      setField(viewRootImpl, "mView", view, cViewRootImpl);
      if (Build.VERSION.SDK_INT >= 11) {
        setField(attachInfo, "mHardwareAccelerated", false, cAttachInfo);
      }

      Method dispatch = View.class
        .getDeclaredMethod("dispatchAttachedToWindow", cAttachInfo, int.class);
      dispatch.setAccessible(true);
      dispatch.invoke(view, attachInfo, 0);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Object invokeConstructor(
      Class clazz,
      Class[] params,
      Object[] values) throws Exception {
    Constructor cons = clazz.getDeclaredConstructor(params);
    cons.setAccessible(true);
    return cons.newInstance(values);
  }

  private static Object createIWindow() throws Exception {
    Class cIWindow = Class.forName("android.view.IWindow");

    // Since IWindow is an interface, I don't need dexmaker for this
    InvocationHandler handler = new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
          if (method.getName().equals("asBinder")) {
            return new Binder();
          }
          return null;
        }
      };

    Object ret = Proxy.newProxyInstance(
      cIWindow.getClassLoader(),
      new Class[] { cIWindow },
      handler);

    return  ret;
  }

  private static Object stub(Class klass) {
    try {
      InvocationHandler handler = new InvocationHandler() {
          @Override
          public Object invoke(Object project, Method method, Object[] args) {
            return null;
          }
        };

      if (klass.isInterface()) {
        return Proxy.newProxyInstance(
          klass.getClassLoader(),
          new Class[] { klass },
          handler);
      } else {
        return ProxyBuilder.forClass(klass)
          .handler(handler)
          .build();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setField(Object o, String fieldName, Object value, Class<?> cls) throws Exception {
    Class clazz = cls;
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(o, value);
  }

  private WindowAttachment() {
  }

}

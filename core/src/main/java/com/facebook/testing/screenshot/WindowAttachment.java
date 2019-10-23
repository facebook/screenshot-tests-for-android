/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.testing.screenshot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

@SuppressLint("PrivateApi")
public abstract class WindowAttachment {

  /** Keep track of all the attached windows here so that we don't double attach them. */
  private static final WeakHashMap<View, Boolean> sAttachments = new WeakHashMap<>();

  @Nullable private static Object sAttachInfo = null;

  private static final InvocationHandler sInvocationHandler =
      new InvocationHandler() {
        @Override
        public @Nullable Object invoke(Object project, Method method, Object[] args) {
          if ("getCoverStateSwitch".equals(method.getName())) {
            // needed for Samsung version of Android 8.0
            return false;
          }
          return null;
        }
      };

  private WindowAttachment() {}

  /**
   * Dispatch onAttachedToWindow to all the views in the view hierarchy.
   *
   * <p>Detach the view by calling {@code detach()} on the returned {@code Detacher}.
   *
   * <p>Note that if the view is already attached (either via WindowAttachment or to a real window),
   * then both the attach and the corresponding detach will be no-ops.
   *
   * <p>Note that this is hacky, after these calls the views will still say that
   * isAttachedToWindow() is false and getWindowToken() == null.
   */
  public static Detacher dispatchAttach(View view) {
    if (view.getWindowToken() != null || sAttachments.containsKey(view)) {
      // Screenshot tests can often be run against a View that's
      // attached to a real activity, in which case we have nothing to
      // do
      Log.i("WindowAttachment", "Skipping window attach hack since it's really attached");
      return new NoopDetacher();
    }

    sAttachments.put(view, true);
    if (Build.VERSION.SDK_INT < 23) {
      // On older versions of Android, requesting focus on a view that would bring the
      // soft keyboard up prior to attachment would result in a NPE in the view's onAttachedToWindow
      // callback. This is due to the fact that it internally calls
      // InputMethodManager.peekInstance() to
      // grab the singleton instance, however it isn't created at that point, leading to the NPE.
      // So in order to avoid that, we just grab the InputMethodManager from the view's context
      // ahead of time to ensure the instance exists.
      // https://android.googlesource.com/platform/frameworks/base/+/a046faaf38ad818e6b5e981a39fd7394cf7cee03
      view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    sAttachInfo = generateAttachInfo(view);
    setAttachInfo(view);

    return new RealDetacher(view);
  }

  /** Similar to dispatchAttach, except dispatchest the corresponding detach. */
  private static void dispatchDetach(View view) {
    invoke(view, "dispatchDetachedFromWindow");
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
  }

  public static void setAttachInfo(View view) {
    if (sAttachInfo == null) {
      sAttachInfo = generateAttachInfo(view);
    }

    try {
      Method dispatch =
          View.class.getDeclaredMethod(
              "dispatchAttachedToWindow", Class.forName("android.view.View$AttachInfo"), int.class);
      dispatch.setAccessible(true);
      dispatch.invoke(view, sAttachInfo, 0);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Simulates the view as being attached. */
  public static @Nullable Object generateAttachInfo(View view) {
    if (sAttachInfo != null) {
      return sAttachInfo;
    }

    try {
      Class cAttachInfo = Class.forName("android.view.View$AttachInfo");
      Class cViewRootImpl;

      if (Build.VERSION.SDK_INT >= 11) {
        cViewRootImpl = Class.forName("android.view.ViewRootImpl");
      } else {
        return null;
      }

      Class cIWindowSession = Class.forName("android.view.IWindowSession");
      Class cIWindow = Class.forName("android.view.IWindow");
      Class cICallbacks = Class.forName("android.view.View$AttachInfo$Callbacks");

      Context context = view.getContext();
      WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();

      Object window = createIWindow();

      final Object viewRootImpl;
      final Class[] viewRootCtorParams;
      final Object[] viewRootCtorValues;

      if (Build.VERSION.SDK_INT >= 26) {
        viewRootImpl =
            cViewRootImpl
                .getConstructor(Context.class, Display.class)
                .newInstance(context, display);

        viewRootCtorParams =
            new Class[] {
              cIWindowSession,
              cIWindow,
              Display.class,
              cViewRootImpl,
              Handler.class,
              cICallbacks,
              Context.class
            };

        viewRootCtorValues =
            new Object[] {
              stub(cIWindowSession),
              window,
              display,
              viewRootImpl,
              new Handler(),
              stub(cICallbacks),
              context
            };
      } else if (Build.VERSION.SDK_INT >= 17) {
        viewRootImpl =
            cViewRootImpl
                .getConstructor(Context.class, Display.class)
                .newInstance(context, display);

        viewRootCtorParams =
            new Class[] {
              cIWindowSession, cIWindow, Display.class, cViewRootImpl, Handler.class, cICallbacks
            };

        viewRootCtorValues =
            new Object[] {
              stub(cIWindowSession), window, display, viewRootImpl, new Handler(), stub(cICallbacks)
            };
      } else if (Build.VERSION.SDK_INT >= 16) {
        viewRootImpl = cViewRootImpl.getConstructor(Context.class).newInstance(context);

        viewRootCtorParams =
            new Class[] {cIWindowSession, cIWindow, cViewRootImpl, Handler.class, cICallbacks};

        viewRootCtorValues =
            new Object[] {
              stub(cIWindowSession), window, viewRootImpl, new Handler(), stub(cICallbacks)
            };
      } else {
        viewRootCtorParams = new Class[] {cIWindowSession, cIWindow, Handler.class, cICallbacks};

        viewRootCtorValues =
            new Object[] {stub(cIWindowSession), window, new Handler(), stub(cICallbacks)};
      }

      Object attachInfo = invokeConstructor(cAttachInfo, viewRootCtorParams, viewRootCtorValues);

      setField(attachInfo, "mHasWindowFocus", true);
      setField(attachInfo, "mWindowVisibility", View.VISIBLE);
      setField(attachInfo, "mInTouchMode", false);
      if (Build.VERSION.SDK_INT >= 11) {
        setField(attachInfo, "mHardwareAccelerated", false);
      }

      return attachInfo;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Object invokeConstructor(Class clazz, Class[] params, Object[] values)
      throws Exception {
    Constructor cons = clazz.getDeclaredConstructor(params);
    cons.setAccessible(true);
    return cons.newInstance(values);
  }

  private static Object createIWindow() throws Exception {
    Class cIWindow = Class.forName("android.view.IWindow");

    // Since IWindow is an interface, I don't need dexmaker for this
    InvocationHandler handler =
        new InvocationHandler() {
          @Override
          public @Nullable Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("asBinder")) {
              return new Binder();
            }
            return null;
          }
        };

    return Proxy.newProxyInstance(cIWindow.getClassLoader(), new Class[] {cIWindow}, handler);
  }

  private static Object stub(Class klass) {
    if (!klass.isInterface()) {
      throw new IllegalArgumentException("Cannot stub an non-interface");
    }

    return Proxy.newProxyInstance(klass.getClassLoader(), new Class[] {klass}, sInvocationHandler);
  }

  private static void setField(Object o, String fieldName, Object value) throws Exception {
    Class clazz = o.getClass();
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(o, value);
  }

  public interface Detacher {
    void detach();
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
}

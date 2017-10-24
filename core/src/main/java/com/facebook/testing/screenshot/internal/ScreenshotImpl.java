/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import com.facebook.testing.screenshot.WindowAttachment;
import com.facebook.testing.screenshot.layouthierarchy.LayoutHierarchyDumper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import org.json.JSONException;

/**
 * Implementation for Screenshot class.
 *
 * The Screenshot class has static methods, because that's how the API
 * should look like, this class has all its implementation for
 * testability.
 *
 * This is public only for implementation convenient for using
 * UiThreadHelper.
 */
public class ScreenshotImpl {
  /**
   * We try to avoid tiling if we can, if the area of the view is less
   * than this number multiplied by the area of a tile, we will not do
   * tiling.
   */
  private static final int TILING_THRESHOLD = 2;
  private static ScreenshotImpl sInstance;
  /**
   * The album of all the screenshots taken in this run.
   */
  private final Album mAlbum;
  private int mTileSize = 512;
  private Bitmap mBitmap = null;
  private Canvas mCanvas = null;
  private boolean mEnableBitmapReconfigure = (Build.VERSION.SDK_INT >= 19);

  /* package */ ScreenshotImpl(Album album) {
    mAlbum = album;
  }

  /**
   * Factory method that creates this instance based on what arguments
   * are passed to the instrumentation
   */
  private static ScreenshotImpl create(
      Context context,
      HostFileSender hostFileSender) {
    Album album = AlbumImpl.createStreaming(context, "default", hostFileSender);
    album.cleanup();
    return new ScreenshotImpl(album);
  }

  /**
   * Get a singleton instance of the ScreenshotImpl
   */
  public static ScreenshotImpl getInstance() {
    if (sInstance != null) {
      return sInstance;
    }

    synchronized (ScreenshotImpl.class) {
      if (sInstance != null) {
        return sInstance;
      }

      Instrumentation instrumentation = Registry.getRegistry().instrumentation;
      Bundle arguments = Registry.getRegistry().arguments;

      HostFileSender hostFileSender = new HostFileSender(
          instrumentation,
          arguments);

      sInstance = create(
          instrumentation.getContext(),
          hostFileSender);

      return sInstance;
    }
  }

  /**
   * Check if getInstance() has ever been called.
   *
   * This is for a minor optimization to avoid creating a
   * ScreenshotImpl at onDestroy() if it was never called during the
   * run.
   */
  public static boolean hasBeenCreated() {
    return sInstance != null;
  }

  // VisibleForTesting
  void setEnableBitmapReconfigure(boolean enableBitmapReconfigure) {
    mEnableBitmapReconfigure = enableBitmapReconfigure;
  }

  public int getTileSize() {
    return mTileSize;
  }

  public void setTileSize(int tileSize) {
    mTileSize = tileSize;
    mBitmap = null;
    mCanvas = null;
  }

  /**
   * Snaps a screenshot of the activity using the testName as the
   * name.
   */
  public RecordBuilderImpl snapActivity(final Activity activity) {
    if (!isUiThread()) {
      return runCallableOnUiThread(new Callable<RecordBuilderImpl>() {
        @Override
        public RecordBuilderImpl call() {
          return snapActivity(activity);
        }
      })
          .setTestClass(TestNameDetector.getTestClass())
          .setTestName(TestNameDetector.getTestName());
    }
    View rootView = activity.getWindow().getDecorView();
    return snap(rootView);
  }

  /**
   * Snaps a screenshot of the view (which should already be measured
   * and layouted) using testName as the name.
   */
  public RecordBuilderImpl snap(final View measuredView) {
    RecordBuilderImpl recordBuilder = new RecordBuilderImpl(this)
        .setView(measuredView)
        .setTestClass(TestNameDetector.getTestClass())
        .setTestName(TestNameDetector.getTestName());

    return recordBuilder;
  }

  // VisibleForTesting
  public void flush() {
    mAlbum.flush();
  }

  private void storeBitmap(final RecordBuilderImpl recordBuilder) {
    if (recordBuilder.getTiling().getAt(0, 0) != null || recordBuilder.getError() != null) {
      return;
    }

    if (!isUiThread()) {
      runCallableOnUiThread(new Callable<Void>() {
        @Override
        public Void call() {
          storeBitmap(recordBuilder);
          return null;
        }
      });
      return;
    }

    View measuredView = recordBuilder.getView();
    if (measuredView.getMeasuredHeight() == 0 ||
        measuredView.getMeasuredWidth() == 0) {
      throw new RuntimeException("Can't take a screenshot, since this view is not measured");
    }

    int tileSize = Math.max(
        measuredView.getWidth(),
        measuredView.getHeight());

    if (measuredView.getMeasuredHeight() * measuredView.getMeasuredWidth()
        > TILING_THRESHOLD * mTileSize * mTileSize) {
      tileSize = mTileSize;
    }

    WindowAttachment.Detacher detacher = WindowAttachment.dispatchAttach(measuredView);
    try {
      int width = measuredView.getWidth();
      int height = measuredView.getHeight();

      int maxi = (width + mTileSize - 1) / mTileSize;
      int maxj = (height + mTileSize - 1) / mTileSize;
      recordBuilder.setTiling(new Tiling(maxi, maxj));

      for (int i = 0; i < maxi; i++) {
        for (int j = 0; j < maxj; j++) {
          drawTile(measuredView, i, j, recordBuilder);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      detacher.detach();
    }
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  private void drawTile(View measuredView, int i, int j, RecordBuilderImpl recordBuilder)
      throws IOException {
    int width = measuredView.getWidth();
    int height = measuredView.getHeight();
    int left = i * mTileSize;
    int top = j * mTileSize;
    int right = Math.min(left + mTileSize, width);
    int bottom = Math.min(top + mTileSize, height);

    lazyInitBitmap();

    if (mEnableBitmapReconfigure) {
      mBitmap.reconfigure(right - left, bottom - top, Bitmap.Config.ARGB_8888);
      mCanvas = new Canvas(mBitmap);
    }
    clearCanvas(mCanvas);

    drawClippedView(measuredView, left, top, mCanvas);
    String tempName = mAlbum.writeBitmap(recordBuilder.getName(), i, j, mBitmap);
    if (tempName == null) {
      throw new NullPointerException();
    }
    recordBuilder.getTiling().setAt(left / mTileSize, top / mTileSize, tempName);
  }

  private void lazyInitBitmap() {
    if (mBitmap != null) {
      return;
    }
    mBitmap = Bitmap.createBitmap(
        mTileSize,
        mTileSize,
        Bitmap.Config.ARGB_8888);
    mCanvas = new Canvas(mBitmap);
  }

  private void clearCanvas(Canvas canvas) {
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
  }

  /**
   * Draw a part of the view, in particular it returns a bitmap of
   * dimensions <code>(right-left)*(bottom-top)</code>, with the
   * rendering of the view starting from position (<code>left</code>,
   * <code>top</code>).
   *
   * For well behaved views, calling this repeatedly shouldn't change
   * the rendering, so it should it okay to render each tile one by
   * one and combine it later.
   */
  private void drawClippedView(View view, int left, int top, Canvas canvas) {
    canvas.translate(-left, -top);
    view.draw(canvas);
    canvas.translate(left, top);
  }

  /**
   * Records the RecordBuilderImpl, and verifies if required
   */
  public void record(RecordBuilderImpl recordBuilder) {
    storeBitmap(recordBuilder);
    OutputStream viewHierarchyDump = null;
    try {
      viewHierarchyDump = mAlbum.openViewHierarchyFile(recordBuilder.getName());
      String dump =
          LayoutHierarchyDumper.create().dumpHierarchy(recordBuilder.getView()).toString(2);
      viewHierarchyDump.write(dump.getBytes());
      viewHierarchyDump.flush();
      mAlbum.addRecord(recordBuilder);
    } catch (IOException | JSONException e) {
      throw new RuntimeException(e);
    } finally {
      if (viewHierarchyDump != null) {
        try {
          viewHierarchyDump.close();
        } catch (IOException e) {
          Log.e("ScreenshotImpl", "Exception closing viewHierarchyDump", e);
        }
      }
    }
  }

  /* package */ Bitmap getBitmap(RecordBuilderImpl recordBuilder) {
    if (recordBuilder.getTiling().getAt(0, 0) != null) {
      throw new IllegalArgumentException("can't call getBitmap() after record()");
    }

    View view = recordBuilder.getView();
    Bitmap bmp = Bitmap.createBitmap(
        view.getWidth(),
        view.getHeight(),
        Bitmap.Config.ARGB_8888);

    WindowAttachment.Detacher detacher = WindowAttachment.dispatchAttach(recordBuilder.getView());
    try {
      drawClippedView(view, 0, 0, new Canvas(bmp));
    } finally {
      detacher.detach();
    }

    return bmp;
  }

  private boolean isUiThread() {
    return Looper.getMainLooper().getThread() == Thread.currentThread();
  }

  private <T> T runCallableOnUiThread(final Callable<T> callable) {
    final T[] ret = (T[]) new Object[1];
    final Exception[] e = new Exception[1];
    final Object lock = new Object();
    Handler handler = new Handler(Looper.getMainLooper());

    synchronized (lock) {
      handler.post(new Runnable() {
        @Override
        public void run() {
          try {
            ret[0] = callable.call();
          } catch (Exception ee) {
            e[0] = ee;
          }
          synchronized (lock) {
            lock.notifyAll();
          }
        }
      });

      try {
        lock.wait();
      } catch (InterruptedException ee) {
        throw new RuntimeException(ee);
      }
    }

    if (e[0] != null) {
      throw new RuntimeException(e[0]);
    }
    return ret[0];
  }
}

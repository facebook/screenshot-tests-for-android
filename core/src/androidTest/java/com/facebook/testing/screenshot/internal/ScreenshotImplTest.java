/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.test.MoreAsserts;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link ScreenshotImpl}
 */
@RunWith(AndroidJUnit4.class)
public class ScreenshotImplTest {
  private AlbumImpl mAlbumImpl;
  private AlbumImpl mSecondAlbumImpl;
  private TextView mTextView;
  private ScreenshotImpl mScreenshot;
  private ScreenshotDirectories mScreenshotDirectories;

  @Before
  public void setUp() throws Exception {
    mScreenshotDirectories = new ScreenshotDirectories(getInstrumentation().getTargetContext());
    mAlbumImpl = AlbumImpl.createLocal(getInstrumentation().getTargetContext(), "verify-in-test");
    mSecondAlbumImpl = AlbumImpl.createLocal(
      getInstrumentation().getTargetContext(),
      "recorded-in-test");
    mTextView = new TextView(getInstrumentation().getTargetContext());
    mTextView.setText("foobar");

    // Unfortunately TextView needs a LayoutParams for onDraw
    mTextView.setLayoutParams(new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));

    measureAndLayout();
    // For most of the tests, we send a null album to verify against
    mScreenshot = new ScreenshotImpl(mAlbumImpl);
  }

  @After
  public void tearDown() throws Exception {
    mAlbumImpl.cleanup();
    mSecondAlbumImpl.cleanup();
  }

  @Test
  public void testBasicFunctionalityHappyPath() throws Throwable {
    mScreenshot.snap(mTextView)
      .setName("fooBar")
      .record();
  }

  @Test
  public void testClassNameIsDetectedOnNonUiThread() throws Throwable {
    assertEquals(getClass().getName(), mScreenshot.snap(mTextView).getTestClass());
  }

  @Test
  public void testTestNameIsDetected() throws Throwable {
    assertEquals("testTestNameIsDetected", mScreenshot.snap(mTextView).getTestName());
  }

  @Test
  public void testRecordBuilderImplHasAHierarchyDumpFile() throws Throwable {
    RecordBuilderImpl rb = mScreenshot.snap(mTextView)
      .setName("blahblah");
    rb.record();
    mScreenshot.flush();

    String fileName =
        new File(mScreenshotDirectories.get("verify-in-test"), "blahblah_dump.json")
            .getAbsolutePath();
    InputStream is = new FileInputStream(fileName);

    StringBuilder builder = new StringBuilder();
    byte[] buffer = new byte[8 * 1024];
    int read;
    while ((read = is.read(buffer)) != -1) {
      builder.append(new String(buffer, 0, read));
    }

    String expected =
        "{"
            + "  \"class\": \"android.widget.TextView\","
            + "  \"left\": 0,"
            + "  \"top\": 0,"
            + "  \"width\": 200,"
            + "  \"height\": 100"
            + "}";
    assertEquals(expected, builder.toString().replace("\n", ""));

    File metadata = mAlbumImpl.getMetadataFile();
    String metadataContents = fileToString(metadata);

    MoreAsserts.assertContainsRegex("blahblah.*.json", metadataContents);
  }

  private String fileToString(File file) {
    try {
      InputStreamReader reader = new InputStreamReader(
        new FileInputStream(file));

      StringBuilder sb = new StringBuilder();

      int ch;
      while ((ch = reader.read()) != -1) {
        sb.append((char) ch);
      }

      return sb.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testBitmapIsSameAsDrawingCache() throws Throwable {
    Bitmap bmp = mScreenshot.snap(mTextView)
      .getBitmap();

    mTextView.setDrawingCacheEnabled(true);
    Bitmap expected = mTextView.getDrawingCache();
    assertBitmapsEqual(expected, bmp);
  }

  @Test
  public void testViewIsAttachedWhileDrawing() throws Throwable {
    mTextView = new MyViewForAttachment(getInstrumentation().getTargetContext());
    measureAndLayout();
    mScreenshot.snap(mTextView)
      .record(); // assertion is taken care of in the view
  }

  public void doTestTiling(boolean enableReconfigure) throws Throwable {
    mScreenshot.setTileSize(1000);
    mScreenshot.setEnableBitmapReconfigure(enableReconfigure);

    final int VIEW_WIDTH = 43;
    final int VIEW_HEIGHT = 32;
    final int TILE_COLS = 5;
    final int TILE_ROWS = 4;

    measureAndLayout(VIEW_WIDTH, VIEW_HEIGHT);

    Bitmap full = mScreenshot.snap(mTextView)
      .getBitmap();

    mScreenshot.setTileSize(10);
    mScreenshot.snap(mTextView)
      .setName("foo")
      .record();

    Bitmap reconstructedFromTiles =
      Bitmap.createBitmap(VIEW_WIDTH, VIEW_HEIGHT, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(reconstructedFromTiles);

    assertEquals(Color.TRANSPARENT, reconstructedFromTiles.getPixel(0, 0));

    for (int i = 0; i < TILE_COLS; i++) {
      for (int j = 0; j < TILE_ROWS; j++) {
        String name = String.format("foo_%d_%d", i, j);
        if (i == 0 && j == 0) {
          name = "foo";
        }

        Bitmap bmp = mAlbumImpl.getScreenshot(name);

        assertNotNull(bmp);

        Paint paint = new Paint();
        int left = i * 10;
        int top = j * 10;

        canvas.drawBitmap(bmp, left, top, paint);

        if (i == TILE_COLS - 1) {
          if (enableReconfigure) {
            assertEquals(3, bmp.getWidth());
          } else {
            assertEquals(10, bmp.getWidth());
          }
        }

        if (j == TILE_ROWS - 1) {
          if (enableReconfigure) {
            assertEquals(2, bmp.getHeight());
          } else {
            assertEquals(10, bmp.getHeight());
          }
        }
      }
    }

    assertBitmapsEqual(full, reconstructedFromTiles);
  }

  @Test
  public void testTiling() throws Throwable {
    doTestTiling(false);
  }

  @SdkSuppress(minSdkVersion=19)
  @Test
  public void testTilingWithReconfigure() throws Throwable {
    doTestTiling(true);
  }

  @Test
  public void testCannotCallgetBitmapAfterRecord() throws Throwable {
    try {
      RecordBuilderImpl rb = mScreenshot.snap(mTextView);
      rb.record();
      rb.getBitmap();
      fail("expected exception");
    } catch (IllegalArgumentException e) {
      MoreAsserts.assertMatchesRegex(".*after.*record.*", e.getMessage());
    }
  }

  @Test(expected=IllegalArgumentException.class)
  public void testNonLatinNamesResultInException() {
    mScreenshot.snap(mTextView)
      .setName("\u06f1")
      .record();
  }

  @Test(expected=IllegalArgumentException.class)
  public void testNamesContainingPathSeparatorsResultInException() {
    mScreenshot.snap(mTextView)
      .setName("simple/test")
      .record();
  }

  @Test
  public void testMultipleOfTileSize() throws Throwable {
    measureAndLayout(512, 512);
    mScreenshot.snap(mTextView)
      .record();
  }

  private void measureAndLayout() {
    measureAndLayout(200, 100);
  }

  private void measureAndLayout(final int width, final int height) {
    try {
      InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
          @Override
          public void run() {
            mTextView.measure(
              View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
              View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            mTextView.layout(0, 0, mTextView.getMeasuredWidth(), mTextView.getMeasuredHeight());
          }
        });
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public class MyViewForAttachment extends TextView {
    private boolean mAttached = false;
    public MyViewForAttachment(Context context) {
      super(context);
    }

    @Override
    protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      mAttached = true;
    }

    @Override
    protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      mAttached = false;
    }

    @Override
    public void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      assertTrue(mAttached);
    }
  }

  private Instrumentation getInstrumentation() {
    return InstrumentationRegistry.getInstrumentation();
  }

  /**
   * Check if the two bitmaps have the same dimensions and pixel data.
   */
  private static void assertBitmapsEqual(Bitmap expected, Bitmap actual) {
    if (expected.getHeight() == 0 || expected.getWidth() == 0) {
      throw new AssertionError("bitmap was empty");
    }

    if (expected.getHeight() != actual.getHeight() ||
        expected.getWidth() != actual.getWidth()) {
      throw new AssertionError("bitmap dimensions don't match");
    }

    for (int i = 0 ; i < expected.getWidth(); i++) {
      for (int j = 0; j < expected.getHeight(); j++) {

        int expectedPixel = expected.getPixel(i, j);
        int actualPixel = actual.getPixel(i, j);

        if (expectedPixel != actualPixel) {
          throw new AssertionError(
            String.format(
              Locale.US,
              "Pixels don't match at (%d, %d), Expected %s, got %s",
              i,
              j,
              Long.toHexString(expected.getPixel(i, j)),
              Long.toHexString(actual.getPixel(i, j))));
        }
      }
    }
  }
}

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SdkSuppress;
import androidx.test.runner.AndroidJUnit4;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

/** Tests for {@link ScreenshotImpl} */
@RunWith(AndroidJUnit4.class)
public class ScreenshotImplTest {
  private AlbumImpl mAlbumImpl;
  private AlbumImpl mSecondAlbumImpl;
  private TextView mTextView;
  private ScreenshotImpl mScreenshot;
  private ScreenshotDirectories mScreenshotDirectories;

  @Rule public Timeout mTimeout = new Timeout(60000);

  @Before
  public void setUp() throws Exception {
    mScreenshotDirectories = new ScreenshotDirectories(getInstrumentation().getTargetContext());
    mAlbumImpl = AlbumImpl.create(getInstrumentation().getTargetContext(), "verify-in-test");
    mSecondAlbumImpl =
        AlbumImpl.create(getInstrumentation().getTargetContext(), "recorded-in-test");
    mTextView = new TextView(getInstrumentation().getTargetContext());
    mTextView.setText("foobar");

    // Unfortunately TextView needs a LayoutParams for onDraw
    mTextView.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

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
    mScreenshot.snap(mTextView).setName("fooBar").record();
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
    RecordBuilderImpl rb = mScreenshot.snap(mTextView).setName("blahblah");
    rb.record();
    mScreenshot.flush();

    ZipFile bundle =
        new ZipFile(
            new File(mScreenshotDirectories.get("verify-in-test"), "screenshot_bundle.zip"));
    Enumeration<? extends ZipEntry> entries = bundle.entries();
    ZipEntry hierarchyEntry = null;
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (entry.getName().equals("blahblah_dump.json")) {
        hierarchyEntry = entry;
        break;
      }
    }

    if (hierarchyEntry == null) {
      throw new IllegalStateException("No hierarchy file found");
    }

    InputStream is = bundle.getInputStream(hierarchyEntry);

    StringBuilder builder = new StringBuilder();
    byte[] buffer = new byte[8 * 1024];
    int read;
    while ((read = is.read(buffer)) != -1) {
      builder.append(new String(buffer, 0, read));
    }
    JSONObject result = new JSONObject(builder.toString());

    assertEquals(3, result.length());
    JSONObject viewHierarchy = result.getJSONObject("viewHierarchy");
    assertEquals(5, viewHierarchy.length());
    assertEquals("android.widget.TextView", viewHierarchy.getString("class"));
    assertEquals(0, viewHierarchy.getInt("left"));
    assertEquals(0, viewHierarchy.getInt("top"));
    assertEquals(200, viewHierarchy.getInt("width"));
    assertEquals(100, viewHierarchy.getInt("height"));

    File metadata = mAlbumImpl.getMetadataFile();
    String metadataContents = fileToString(metadata);

    OldApiBandaid.assertContainsRegex("blahblah.*.json", metadataContents);
  }

  @Test
  public void testLargeViewThrowsWithDefaultMax() throws Throwable {
    measureAndLayout(200, 0xffff00);

    assertEquals(200, mTextView.getMeasuredWidth());
    assertEquals(0xffff00, mTextView.getMeasuredHeight());
    try {
      mScreenshot.snap(mTextView).setName("largeView").record();
      fail("expected exception");
    } catch (RuntimeException e) {
      OldApiBandaid.assertContainsRegex(".*View too large.*", e.getMessage());
    }
  }

  @Test
  public void testLargeViewDoesntThrowWithCustomMax() throws Throwable {
    measureAndLayout(1440, 1000);

    assertEquals(1440, mTextView.getMeasuredWidth());
    assertEquals(1000, mTextView.getMeasuredHeight());
    mScreenshot.snap(mTextView).setMaxPixels(10733760L).setName("largeView").record();
  }

  @Test
  public void testLargeViewDoesntThrowWithNoMax() throws Throwable {
    measureAndLayout(1440, 1000);

    assertEquals(1440, mTextView.getMeasuredWidth());
    assertEquals(1000, mTextView.getMeasuredHeight());
    mScreenshot.snap(mTextView).setMaxPixels(0).setName("largeView").record();
  }

  private String fileToString(File file) {
    try {
      InputStreamReader reader = new InputStreamReader(new FileInputStream(file));

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
    Bitmap bmp = mScreenshot.snap(mTextView).getBitmap();

    mTextView.setDrawingCacheEnabled(true);
    Bitmap expected = mTextView.getDrawingCache();
    assertBitmapsEqual(expected, bmp);
  }

  @Test
  public void testViewIsAttachedWhileDrawing() throws Throwable {
    mTextView = new MyViewForAttachment(getInstrumentation().getTargetContext());
    measureAndLayout();
    mScreenshot.snap(mTextView).record(); // assertion is taken care of in the view
  }

  public void doTestTiling(boolean enableReconfigure) throws Throwable {
    mScreenshot.setTileSize(1000);
    mScreenshot.setEnableBitmapReconfigure(enableReconfigure);

    final int VIEW_WIDTH = 43;
    final int VIEW_HEIGHT = 32;
    final int TILE_COLS = 5;
    final int TILE_ROWS = 4;

    measureAndLayout(VIEW_WIDTH, VIEW_HEIGHT);

    Bitmap full = mScreenshot.snap(mTextView).getBitmap();

    mScreenshot.setTileSize(10);
    mScreenshot.snap(mTextView).setName("foo").record();

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

  @SdkSuppress(minSdkVersion = 19)
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
      OldApiBandaid.assertMatchesRegex(".*after.*record.*", e.getMessage());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonLatinNamesResultInException() {
    mScreenshot.snap(mTextView).setName("\u06f1").record();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamesContainingPathSeparatorsResultInException() {
    mScreenshot.snap(mTextView).setName("simple/test").record();
  }

  @Test
  public void testMultipleOfTileSize() throws Throwable {
    measureAndLayout(512, 512);
    mScreenshot.snap(mTextView).record();
  }

  private void measureAndLayout() {
    measureAndLayout(200, 100);
  }

  private void measureAndLayout(final int width, final int height) {
    try {
      InstrumentationRegistry.getInstrumentation()
          .runOnMainSync(
              new Runnable() {
                @Override
                public void run() {
                  mTextView.measure(
                      View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                      View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
                  mTextView.layout(
                      0, 0, mTextView.getMeasuredWidth(), mTextView.getMeasuredHeight());
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

  /** Check if the two bitmaps have the same dimensions and pixel data. */
  private static void assertBitmapsEqual(Bitmap expected, Bitmap actual) {
    if (expected.getHeight() == 0 || expected.getWidth() == 0) {
      throw new AssertionError("bitmap was empty");
    }

    if (expected.getHeight() != actual.getHeight() || expected.getWidth() != actual.getWidth()) {
      throw new AssertionError("bitmap dimensions don't match");
    }

    for (int i = 0; i < expected.getWidth(); i++) {
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

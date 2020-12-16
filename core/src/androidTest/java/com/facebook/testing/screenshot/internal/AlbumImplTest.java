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

import android.graphics.Bitmap;
import androidx.test.InstrumentationRegistry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link AlbumImpl} */
public class AlbumImplTest {
  private static final int BITMAP_DIMENSION = 10; /* pixels */
  private AlbumImpl mAlbumImpl;
  private Bitmap mSomeBitmap;
  private String mFooFile;
  private String mBarFile;
  private ScreenshotDirectories mScreenshotDirectories;

  @Before
  public void setUp() throws Exception {
    mScreenshotDirectories = new ScreenshotDirectories(InstrumentationRegistry.getTargetContext());
    mAlbumImpl = createAlbumImplForTests();
    mSomeBitmap = Bitmap.createBitmap(BITMAP_DIMENSION, BITMAP_DIMENSION, Bitmap.Config.ARGB_8888);
    mSomeBitmap.setPixel(1, 1, 0xff0000ff);

    mFooFile = mAlbumImpl.writeBitmap("foo", 0, 0, mSomeBitmap);
    mBarFile = mAlbumImpl.writeBitmap("bar", 0, 0, mSomeBitmap);
  }

  private AlbumImpl createAlbumImplForTests() {
    return AlbumImpl.create(InstrumentationRegistry.getTargetContext(), "screenshots");
  }

  @After
  public void tearDown() throws Exception {
    mAlbumImpl.cleanup();
  }

  @Test
  public void testWriteTempBitmap() throws Throwable {
    Bitmap output = mAlbumImpl.getScreenshot(mAlbumImpl.writeBitmap("sfdf", 0, 0, mSomeBitmap));

    int actualBlueness = output.getPixel(1, 1) & 0xff;
    assertTrue("The pixel should be same accounting for compression", actualBlueness > 0xf0);
  }

  @Test
  public void testMultipleBitmapsAreAvailableAfterAlbumRecreation() throws Throwable {
    String firstScreenshotName = mAlbumImpl.writeBitmap("first", 0, 0, mSomeBitmap);
    mAlbumImpl.flush();
    mAlbumImpl = createAlbumImplForTests();
    String secondScreenshotnName = mAlbumImpl.writeBitmap("second", 0, 0, mSomeBitmap);
    mAlbumImpl.flush();

    AlbumImpl newInstance = createAlbumImplForTests();
    Bitmap firstOutput = newInstance.getScreenshot(firstScreenshotName);
    Bitmap secondOutput = newInstance.getScreenshot(secondScreenshotnName);

    int firstActualBlueness = firstOutput.getPixel(1, 1) & 0xff;
    assertTrue("The pixel should be same accounting for compression", firstActualBlueness > 0xf0);

    int secondActualBlueness = secondOutput.getPixel(1, 1) & 0xff;
    assertTrue("The pixel should be same accounting for compression", secondActualBlueness > 0xf0);
  }

  @Test
  public void testCleanupAndGet() throws Throwable {
    mAlbumImpl.addRecord(
        new RecordBuilderImpl(null).setName("foo").setTiling(Tiling.singleTile(mFooFile)));

    assertNotNull(mAlbumImpl.getScreenshot("foo"));
    mAlbumImpl.cleanup();
    assertEquals(null, mAlbumImpl.getScreenshot("foo"));
  }

  @Test
  public void testMultipleCleanups() throws Throwable {
    mAlbumImpl.addRecord(
        new RecordBuilderImpl(null).setName("foo").setTiling(Tiling.singleTile(mFooFile)));
    mAlbumImpl.cleanup();
    mAlbumImpl.cleanup();
  }

  @Test
  public void testNonExistentScreenshotReturnsNull() throws Throwable {
    assertEquals(null, mAlbumImpl.getScreenshot("mehmeh"));
    assertEquals(null, mAlbumImpl.getScreenshotFile("mehmeh"));
  }

  @Test
  public void testMetadataSaving() throws Throwable {
    mAlbumImpl.addRecord(
        new RecordBuilderImpl(null).setTiling(Tiling.singleTile(mFooFile)).setName("foo"));
    mAlbumImpl.addRecord(
        new RecordBuilderImpl(null).setTiling(Tiling.singleTile(mBarFile)).setName("bar"));

    mAlbumImpl.flush();
    JSONArray metadataJson = parseMetadata();

    assertEquals("bar", metadataJson.getJSONObject(1).getString("name"));
  }

  @Test
  public void testMetadataSavingAfterInstanceRecreation() throws Throwable {
    mAlbumImpl.addRecord(
        new RecordBuilderImpl(null).setTiling(Tiling.singleTile(mFooFile)).setName("foo"));
    mAlbumImpl.flush();
    mAlbumImpl = createAlbumImplForTests();
    mAlbumImpl.addRecord(
        new RecordBuilderImpl(null).setTiling(Tiling.singleTile(mBarFile)).setName("bar"));
    mAlbumImpl.flush();

    JSONArray metadataJson = parseMetadata();
    assertEquals("bar", metadataJson.getJSONObject(1).getString("name"));
  }

  @Test
  public void testSavesViewHierachy() throws Throwable {
    mAlbumImpl.writeViewHierarchyFile("foo", "");
    mAlbumImpl.addRecord(
        new RecordBuilderImpl(null).setName("foo").setTiling(Tiling.singleTile(mFooFile)));

    mAlbumImpl.flush();
    JSONArray metadataJson = parseMetadata();
    String actual = metadataJson.getJSONObject(0).getString("viewHierarchy");
    assertEquals("foo_dump.json", actual);
  }

  @Test
  public void testSavesExtra() throws Throwable {
    RecordBuilderImpl rb = new RecordBuilderImpl(null);
    rb.setName("foo").setTiling(Tiling.singleTile(mFooFile)).addExtra("foo", "blah");

    mAlbumImpl.addRecord(rb);

    mAlbumImpl.flush();
    JSONArray metadataJson = parseMetadata();

    assertEquals("blah", metadataJson.getJSONObject(0).getJSONObject("extras").getString("foo"));
  }

  @Test
  public void testSavesMultipleExtras() throws Throwable {
    RecordBuilderImpl rb = new RecordBuilderImpl(null);
    rb.setName("foo")
        .setTiling(Tiling.singleTile(mFooFile))
        .addExtra("foo", "blah")
        .addExtra("bar", "blah2");

    mAlbumImpl.addRecord(rb);

    mAlbumImpl.flush();
    JSONArray metadataJson = parseMetadata();

    assertEquals("blah", metadataJson.getJSONObject(0).getJSONObject("extras").getString("foo"));

    assertEquals("blah2", metadataJson.getJSONObject(0).getJSONObject("extras").getString("bar"));
  }

  @Test
  public void testErrorSaving() throws Throwable {
    mAlbumImpl.addRecord(new RecordBuilderImpl(null).setError("foobar"));
    mAlbumImpl.flush();
    JSONArray metadataJson = parseMetadata();
    String errorFromFile = metadataJson.getJSONObject(0).getString("error");
    assertEquals("foobar", errorFromFile);
  }

  @Test
  public void testSavesGroup() throws Throwable {
    mAlbumImpl.addRecord(
        new RecordBuilderImpl(null)
            .setName("xyz")
            .setTiling(Tiling.singleTile(mFooFile))
            .setGroup("foo_bar"));

    mAlbumImpl.flush();

    JSONArray metadataFile = parseMetadata();
    String actualGroup = metadataFile.getJSONObject(0).getString("group");
    assertEquals("foo_bar", actualGroup);
  }

  private JSONArray parseMetadata() throws Throwable {
    String content = readAllMetadataContent();
    return new JSONArray(content);
  }

  private String readAllMetadataContent() throws Throwable {
    File file = new File(mScreenshotDirectories.get("screenshots"), "metadata.json");
    FileReader fileReader = new FileReader(file);
    BufferedReader bufferedReader = new BufferedReader(fileReader);
    StringBuilder result = new StringBuilder();
    String line = bufferedReader.readLine();
    while (line != null) {
      result.append(line).append("\n");
      line = bufferedReader.readLine();
    }
    bufferedReader.close();
    return result.toString();
  }

  @Test
  public void testMultipleRecordsFromSameTestWithName() throws Throwable {
    mAlbumImpl.addRecord(
        new RecordBuilderImpl(null).setName("foo").setTiling(Tiling.singleTile(mFooFile)));

    try {
      mAlbumImpl.addRecord(
          new RecordBuilderImpl(null).setName("foo").setTiling(Tiling.singleTile(mFooFile)));
    } catch (AssertionError e) {
      OldApiBandaid.assertMatchesRegex(".*same name.*", e.getMessage());
      return;
    }
    fail("expected to see an exception");
  }

  @Test
  public void testRecordWithTiles() throws Throwable {
    final int WIDTH = 3;
    final int HEIGHT = 4;

    RecordBuilderImpl builder =
        new RecordBuilderImpl(null).setName("baz").setTiling(new Tiling(WIDTH, HEIGHT));

    for (int i = 0; i < WIDTH; i++) {
      for (int j = 0; j < HEIGHT; j++) {
        String tempName = mAlbumImpl.writeBitmap("baz", i, j, mSomeBitmap);
        builder.getTiling().setAt(i, j, tempName);
      }
    }

    mAlbumImpl.addRecord(builder);
    mAlbumImpl.flush();

    JSONArray metadataJson = parseMetadata();

    JSONObject screenshot = metadataJson.getJSONObject(0);

    int tileWidth = screenshot.getInt("tileWidth");
    int tileHeight = screenshot.getInt("tileHeight");

    assertEquals(WIDTH, tileWidth);
    assertEquals(HEIGHT, tileHeight);

    JSONArray fileNames = screenshot.getJSONArray("absoluteFilesNames");

    assertEquals(12, fileNames.length());
    String fourthFile = fileNames.getString(4);
    OldApiBandaid.assertMatchesRegex(
        "The x coordinate should be before y coordinate", ".*baz_2_3", fileNames.getString(11));

    OldApiBandaid.assertMatchesRegex(".*baz_1_0", fourthFile);

    JSONArray relativeFileNames = screenshot.getJSONArray("relativeFileNames");

    assertEquals(12, relativeFileNames.length());
    String relativeFourthFile = relativeFileNames.getString(4);
    assertEquals("baz_1_0", relativeFourthFile);
  }
}

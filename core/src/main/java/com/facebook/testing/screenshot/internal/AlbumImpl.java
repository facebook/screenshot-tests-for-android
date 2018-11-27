/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.testing.screenshot.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Xml;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.xmlpull.v1.XmlSerializer;

/** A "local" implementation of Album. */
@SuppressWarnings("deprecation")
public class AlbumImpl implements Album {
  private static final int COMPRESSION_QUALITY = 90;

  private final File mDir;
  private final Set<String> mAllNames = new HashSet<>();
  private XmlSerializer mXmlSerializer;
  private FileOutputStream mOutputStream;

  /* VisibleForTesting */
  AlbumImpl(ScreenshotDirectories screenshotDirectories, String name) {
    mDir = screenshotDirectories.get(name);
  }

  /** Creates a "local" album that stores all the images on device. */
  public static AlbumImpl create(Context context, String name) {
    return new AlbumImpl(new ScreenshotDirectories(context), name);
  }

  @Override
  public void flush() {
    if (mOutputStream != null) {
      endXml();
    }
  }

  private void initXml() {
    if (mOutputStream != null) {
      return;
    }

    try {
      mOutputStream = new FileOutputStream(getMetadataFile());
      mXmlSerializer = Xml.newSerializer();
      mXmlSerializer.setOutput(mOutputStream, "utf-8");
      mXmlSerializer.startDocument("utf-8", null);
      mXmlSerializer.startTag(null, "screenshots");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressLint("SetWorldReadable")
  private void endXml() {
    try {
      mXmlSerializer.endTag(null, "screenshots");
      mXmlSerializer.endDocument();
      mXmlSerializer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      mOutputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Returns the stored screenshot in the album, or null if no such test case exists. */
  @Nullable
  Bitmap getScreenshot(String name) {
    if (getScreenshotFile(name) == null) {
      return null;
    }
    return BitmapFactory.decodeFile(getScreenshotFile(name).getAbsolutePath());
  }

  /**
   * Returns the file in which the screenshot is stored, or null if this is not a valid screenshot
   */
  @Nullable
  File getScreenshotFile(String name) {
    File file = getScreenshotFileInternal(name);
    if (!file.isFile()) {
      return null;
    }
    return file;
  }

  @SuppressLint("SetWorldReadable")
  @Override
  public String writeBitmap(String name, int tilei, int tilej, Bitmap bitmap) throws IOException {
    String tileName = generateTileName(name, tilei, tilej);
    File file = getScreenshotFileInternal(tileName);
    FileOutputStream outputStream;
    outputStream = new FileOutputStream(file);
    bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, outputStream);
    outputStream.close();
    file.setReadable(/* readable = */ true, /* ownerOnly = */ false);
    return tileName;
  }

  /** Delete all screenshots associated with this album */
  @Override
  public void cleanup() {
    if (!mDir.exists()) {
      // We probably failed to even create it, so nothing to clean up
      return;
    }
    for (String s : mDir.list()) {
      new File(mDir, s).delete();
    }
  }

  /**
   * Same as the public getScreenshotFile() except it returns the File even if the screenshot
   * doesn't exist.
   */
  private File getScreenshotFileInternal(String name) {
    return new File(mDir, name + ".png");
  }

  private File getViewHierarchyFile(String name) {
    return new File(mDir, name + "_dump.json");
  }

  @Override
  public OutputStream openViewHierarchyFile(String name) throws IOException {
    File file = getViewHierarchyFile(name);
    OutputStream os = new FileOutputStream(file);
    os.flush();
    return os;
  }

  /**
   * Add the given record to the album. This is called by RecordBuilderImpl#record() and so is an
   * internal detail.
   */
  @SuppressLint("SetWorldReadable")
  @Override
  public void addRecord(RecordBuilderImpl recordBuilder) throws IOException {
    initXml();
    recordBuilder.checkState();
    if (mAllNames.contains(recordBuilder.getName())) {
      if (recordBuilder.hasExplicitName()) {
        throw new AssertionError(
            "Can't create multiple screenshots with the same name: " + recordBuilder.getName());
      }

      throw new AssertionError(
          "Can't create multiple screenshots from the same test, or "
              + "use .setName() to name each screenshot differently");
    }

    mXmlSerializer.startTag(null, "screenshot");
    Tiling tiling = recordBuilder.getTiling();
    addTextNode("description", recordBuilder.getDescription());
    addTextNode("name", recordBuilder.getName());
    addTextNode("test_class", recordBuilder.getTestClass());
    addTextNode("test_name", recordBuilder.getTestName());
    addTextNode("tile_width", String.valueOf(tiling.getWidth()));
    addTextNode("tile_height", String.valueOf(tiling.getHeight()));

    File viewHierarchy = getViewHierarchyFile(recordBuilder.getName());

    if (viewHierarchy.exists()) {
      addTextNode("view_hierarchy", getRelativePath(viewHierarchy, mDir));
      viewHierarchy.setReadable(/* readable = */ true, /* ownerOnly = */ false);
    }

    mXmlSerializer.startTag(null, "extras");
    for (Map.Entry<String, String> entry : recordBuilder.getExtras().entrySet()) {
      addTextNode(entry.getKey(), entry.getValue());
    }
    mXmlSerializer.endTag(null, "extras");

    if (recordBuilder.getError() != null) {
      addTextNode("error", recordBuilder.getError());
    } else {
      saveTiling(recordBuilder);
    }

    if (recordBuilder.getGroup() != null) {
      addTextNode("group", recordBuilder.getGroup());
    }

    mAllNames.add(recordBuilder.getName());

    mXmlSerializer.endTag(null, "screenshot");
    mXmlSerializer.flush();
  }

  private void saveTiling(RecordBuilderImpl recordBuilder) throws IOException {
    Tiling tiling = recordBuilder.getTiling();
    for (int i = 0; i < tiling.getWidth(); i++) {
      for (int j = 0; j < tiling.getHeight(); j++) {
        File file = getScreenshotFileInternal(tiling.getAt(i, j));

        if (!file.isFile()) {
          throw new RuntimeException("The tile file doesn't exist");
        }

        addTextNode("absolute_file_name", file.getAbsolutePath());
        addTextNode("relative_file_name", getRelativePath(file, mDir));
      }
    }
  }

  /** Returns the relative path of file from dir */
  private String getRelativePath(File file, File dir) {
    try {
      String filePath = file.getCanonicalPath();
      String dirPath = dir.getCanonicalPath();

      if (filePath.startsWith(dirPath)) {
        return filePath.substring(dirPath.length() + 1);
      }

      return filePath;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addTextNode(String name, String value) throws IOException {
    mXmlSerializer.startTag(null, name);
    if (value != null) {
      mXmlSerializer.text(value);
    }
    mXmlSerializer.endTag(null, name);
  }

  public File getMetadataFile() {
    return new File(mDir, "metadata.xml");
  }

  /**
   * For a given screenshot, and a tile position, generates a name where we store the screenshot in
   * the album.
   *
   * <p>For backward compatibility with existing screenshot scripts, for the tile (0, 0) we use the
   * name directly.
   */
  private String generateTileName(String name, int i, int j) {
    if (i == 0 && j == 0) {
      return name;
    }

    return String.format("%s_%s_%s", name, String.valueOf(i), String.valueOf(j));
  }
}

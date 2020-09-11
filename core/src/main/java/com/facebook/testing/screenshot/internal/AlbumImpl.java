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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import androidx.annotation.VisibleForTesting;

import static com.facebook.testing.screenshot.ScreenshotRunner.SCREENSHOT_TESTS_RUN_ID;

/** A "local" implementation of Album. */
@SuppressWarnings("deprecation")
public class AlbumImpl implements Album {
  private static final int COMPRESSION_QUALITY = 90;
  private static final int BUFFER_SIZE = 1 << 16; // 64k
  private static final String SCREENSHOT_BUNDLE_FILE_NAME = "screenshot_bundle.tar";
  private static final String SCREENSHOT_TESTS_RUN_ID_FILE_NAME = "tests_run_id";
  private static final int TAR_EOF_RECORD_SIZE = 1024;

  private final File mDir;
  private final Set<String> mAllNames = new HashSet<>();
  private TarArchiveOutputStream mTarOutputStream;
  private final MetadataRecorder mMetadataRecorder;
  private String mPreviousTestRunId;
  private String mCurrentTestRunId;

  /* VisibleForTesting */
  AlbumImpl(ScreenshotDirectories screenshotDirectories, String name) {
    mDir = screenshotDirectories.get(name);
    mPreviousTestRunId = readPreviousTestRunId();
    mCurrentTestRunId = getCurrentTestRunId();
    mMetadataRecorder = new MetadataRecorder(mDir);
  }

  /** Creates a "local" album that stores all the images on device. */
  public static AlbumImpl create(Context context, String name) {
    return new AlbumImpl(new ScreenshotDirectories(context), name);
  }

  @SuppressLint("SetWorldReadable")
  private TarArchiveOutputStream getOrCreateTarOutputStream() throws IOException {
    if (mTarOutputStream == null) {
      File file = new File(mDir, SCREENSHOT_BUNDLE_FILE_NAME);
      file.createNewFile();
      //file.setReadable(/* readable = */ true, /* ownerOnly = */ false);
      RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
      long length = randomAccessFile.length();
      if (length > TAR_EOF_RECORD_SIZE) {
        randomAccessFile.seek(length - TAR_EOF_RECORD_SIZE);
      }
      mTarOutputStream = new TarArchiveOutputStream(
          new BufferedOutputStream(
              new RandomAccessFileOutputStream(randomAccessFile),
              BUFFER_SIZE
          )
      );
    }
    return mTarOutputStream;
  }

  @Override
  public void flush() {
    mMetadataRecorder.flush();
    try {
      if (mTarOutputStream != null) {
        mTarOutputStream.close();
      }
    } catch (IOException e) {
      Log.d(AlbumImpl.class.getName(), "Couldn't close zip file.", e);
    }
    writePreviousTestRunId();
  }

  private String readPreviousTestRunId() {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(new File(mDir, SCREENSHOT_TESTS_RUN_ID_FILE_NAME)));
      return reader.readLine();
    } catch (IOException e) {
      return null;
    }
  }

  private void writePreviousTestRunId() {
    try {
      FileWriter writer = new FileWriter(new File(mDir, SCREENSHOT_TESTS_RUN_ID_FILE_NAME));
      writer.write(mCurrentTestRunId);
      writer.close();
    } catch (IOException e) {
      Log.e(AlbumImpl.class.getName(), "Couldn't write previous test run id.", e);
    }
  }

  private String getCurrentTestRunId() {
    return Registry.getRegistry().arguments.getString(SCREENSHOT_TESTS_RUN_ID, "");
  }

  /** Returns the stored screenshot in the album, or null if no such test case exists. */
  @Nullable
  Bitmap getScreenshot(String name) throws IOException {
    if (getScreenshotFile(name) == null) {
      return null;
    }
    return BitmapFactory.decodeFile(getScreenshotFile(name).getAbsolutePath());
  }

  /**
   * Returns the file in which the screenshot is stored, or null if this is not a valid screenshot
   *
   * <p>TODO: Adjust tests to no longer use this method. It's quite sketchy and inefficient.
   */
  @Nullable
  File getScreenshotFile(String name) throws IOException {
    if (mTarOutputStream != null) {
      // This needs to be a valid file before we can read from it.
      mTarOutputStream.close();
    }

    File bundle = new File(mDir, SCREENSHOT_BUNDLE_FILE_NAME);
    if (!bundle.isFile()) {
      return null;
    }

    TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new FileInputStream(bundle));
    try {
      String filename = getScreenshotFilenameInternal(name);
      byte[] buffer = new byte[BUFFER_SIZE];

      TarArchiveEntry entry;
      while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
        if (!filename.equals(entry.getName())) {
          continue;
        }

        File file = File.createTempFile(name, ".png");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
          int len;
          while ((len = tarArchiveInputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, len);
          }
        } finally {
          fileOutputStream.close();
        }
        return file;
      }
    } finally {
      tarArchiveInputStream.close();
    }
    return null;
  }

  @Override
  public String writeBitmap(String name, int tilei, int tilej, Bitmap bitmap) throws IOException {
    String tileName = generateTileName(name, tilei, tilej);
    String filename = getScreenshotFilenameInternal(tileName);
    TarArchiveOutputStream tarOutputStream = getOrCreateTarOutputStream();
    ByteArrayOutputStream os = new ByteArrayOutputStream(bitmap.getHeight() * bitmap.getWidth() * 2);
    bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, os);
    TarArchiveEntry entry = new TarArchiveEntry(filename);
    entry.setSize(os.size());
    tarOutputStream.putArchiveEntry(entry);
    tarOutputStream.write(os.toByteArray());
    tarOutputStream.closeArchiveEntry();
    return tileName;
  }

  /** Delete all screenshots associated with this album */
  @Override
  public void cleanup() {
    if (mCurrentTestRunId.equals(mPreviousTestRunId)) {
      // AlbumImpl instance was recreated because of ORCHESTRATOR
      return;
    }
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
  private static String getScreenshotFilenameInternal(String name) {
    return name + ".png";
  }

  private static String getViewHierarchyFilename(String name) {
    return name + "_dump.json";
  }

  private static String getAxIssuesFilename(String name) {
    return name + "_issues.json";
  }

  @Override
  public void writeAxIssuesFile(String name, String data) throws IOException {
    writeMetadataFile(getAxIssuesFilename(name), data);
  }

  @Override
  public void writeViewHierarchyFile(String name, String data) throws IOException {
    writeMetadataFile(getViewHierarchyFilename(name), data);
  }

  public void writeMetadataFile(String name, String data) throws IOException {
    byte[] out = data.getBytes();

    TarArchiveEntry archiveEntry = new TarArchiveEntry(name);
    TarArchiveOutputStream tarOutputStream = getOrCreateTarOutputStream();
    archiveEntry.setSize(out.length);
    tarOutputStream.putArchiveEntry(archiveEntry);
    tarOutputStream.write(out);
    tarOutputStream.closeArchiveEntry();
  }

  /**
   * Add the given record to the album. This is called by RecordBuilderImpl#record() and so is an
   * internal detail.
   */
  @SuppressLint("SetWorldReadable")
  @Override
  public void addRecord(RecordBuilderImpl recordBuilder) throws IOException {
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

    Tiling tiling = recordBuilder.getTiling();

    MetadataRecorder.ScreenshotPropertiesRecorder screenshotNode = mMetadataRecorder.startProperty("screenshot")
        .addProperty("description", recordBuilder.getDescription())
        .addProperty("name", recordBuilder.getName())
        .addProperty("test_class", recordBuilder.getTestClass())
        .addProperty("test_name", recordBuilder.getTestName())
        .addProperty("tile_width", String.valueOf(tiling.getWidth()))
        .addProperty("tile_height", String.valueOf(tiling.getHeight()))
        .addProperty("view_hierarchy", getViewHierarchyFilename(recordBuilder.getName()))
        .addProperty("ax_issues", getAxIssuesFilename(recordBuilder.getName()));

    MetadataRecorder.ScreenshotPropertiesRecorder extrasNode = screenshotNode.startProperty("extras");
    for (Map.Entry<String, String> entry : recordBuilder.getExtras().entrySet()) {
      extrasNode.addProperty(entry.getKey(), entry.getValue());
    }
    extrasNode.endProperty();

    if (recordBuilder.getError() != null) {
      screenshotNode.addProperty("error", recordBuilder.getError());
    } else {
      saveTiling(screenshotNode, recordBuilder);
    }

    if (recordBuilder.getGroup() != null) {
      screenshotNode.addProperty("group", recordBuilder.getGroup());
    }

    mAllNames.add(recordBuilder.getName());

    screenshotNode.endProperty();
  }

  @VisibleForTesting
  File getMetadataFile() {
    return mMetadataRecorder.getMetadataFile();
  }

  private void saveTiling(MetadataRecorder.ScreenshotPropertiesRecorder recorder, RecordBuilderImpl recordBuilder) throws IOException{
    Tiling tiling = recordBuilder.getTiling();
    for (int i = 0; i < tiling.getWidth(); i++) {
      for (int j = 0; j < tiling.getHeight(); j++) {
        File file = new File(mDir, generateTileName(recordBuilder.getName(), i, j));

        recorder.addProperty("absolute_file_name", file.getAbsolutePath());
        recorder.addProperty("relative_file_name", getRelativePath(file, mDir));
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

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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MetadataRecorder {

  private final File mDir;
  private List<ScreenshotMetadata> mMetadata;

  MetadataRecorder(File reportDirectory) {
    mDir = reportDirectory;
  }

  void flush() {
    try {
      writeMetadata();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  ScreenshotMetadataRecorder addNewScreenshot() {
    return new ScreenshotMetadataRecorder();
  }

  class ScreenshotMetadataRecorder {

    private final ScreenshotMetadata mCurrentScreenshotMetadata = new ScreenshotMetadata();

    void save() throws IOException {
      List<ScreenshotMetadata> allScreenshotsMetadata = getOrReadMetadata();
      if (allScreenshotsMetadata.contains(mCurrentScreenshotMetadata)) {
        throw new IllegalStateException("metadata was already saved");
      }
      allScreenshotsMetadata.add(mCurrentScreenshotMetadata);
    }

    public ScreenshotMetadataRecorder withDescription(String description) {
      mCurrentScreenshotMetadata.description = description;
      return this;
    }

    public ScreenshotMetadataRecorder withName(String name) {
      mCurrentScreenshotMetadata.name = name;
      return this;
    }

    public ScreenshotMetadataRecorder withTestClass(String testClass) {
      mCurrentScreenshotMetadata.testClass = testClass;
      return this;
    }

    public ScreenshotMetadataRecorder withTestName(String testName) {
      mCurrentScreenshotMetadata.testName = testName;
      return this;
    }

    public ScreenshotMetadataRecorder withTileWidth(int width) {
      mCurrentScreenshotMetadata.tileWidth = width;
      return this;
    }

    public ScreenshotMetadataRecorder withTileHeight(int height) {
      mCurrentScreenshotMetadata.tileHeight = height;
      return this;
    }

    public ScreenshotMetadataRecorder withViewHierarchy(String viewHierarchyFilename) {
      mCurrentScreenshotMetadata.viewHierarchy = viewHierarchyFilename;
      return this;
    }

    public ScreenshotMetadataRecorder withAxIssues(String axIssuesFilename) {
      mCurrentScreenshotMetadata.axIssues = axIssuesFilename;
      return this;
    }

    public ScreenshotMetadataRecorder withExtras(Map<String, String> extras) {
      mCurrentScreenshotMetadata.extras = new HashMap<>(extras);
      return this;
    }

    public ScreenshotMetadataRecorder withError(String error) {
      mCurrentScreenshotMetadata.error = error;
      return this;
    }

    public ScreenshotMetadataRecorder withGroup(String group) {
      mCurrentScreenshotMetadata.group = group;
      return this;
    }

    public ScreenshotMetadataRecorder withAbsoluteFileName(String absolutePath) {
      if (mCurrentScreenshotMetadata.absoluteFilesNames == null) {
        mCurrentScreenshotMetadata.absoluteFilesNames = new ArrayList<>();
      }
      mCurrentScreenshotMetadata.absoluteFilesNames.add(absolutePath);
      return this;
    }

    public ScreenshotMetadataRecorder withRelativeFileName(String relativePath) {
      if (mCurrentScreenshotMetadata.relativeFileNames == null) {
        mCurrentScreenshotMetadata.relativeFileNames = new ArrayList<>();
      }
      mCurrentScreenshotMetadata.relativeFileNames.add(relativePath);
      return this;
    }
  }

  File getMetadataFile() {
    return new File(mDir, "metadata.json");
  }

  private List<ScreenshotMetadata> getOrReadMetadata() throws IOException {
    if (mMetadata == null) {

      File metadataFile = getMetadataFile();
      if (metadataFile.exists()) {
        Gson gson = new Gson();
        JsonReader jsonReader = new JsonReader(new FileReader(getMetadataFile()));
        mMetadata =
            gson.fromJson(jsonReader, new TypeToken<List<ScreenshotMetadata>>() {}.getType());
        if (mMetadata == null) {
          mMetadata = new ArrayList<>();
        }
      } else {
        mMetadata = new ArrayList<>();
      }
    }
    return mMetadata;
  }

  private void writeMetadata() throws IOException {
    if (mMetadata != null) {
      Gson gson = new Gson();
      FileWriter jsonWriter = new FileWriter(getMetadataFile());
      gson.toJson(mMetadata, jsonWriter);
      jsonWriter.close();
    }
  }

  private static class ScreenshotMetadata {
    String description;
    String name;
    String testClass;
    String testName;
    int tileWidth;
    int tileHeight;
    String viewHierarchy;
    String axIssues;
    String error;
    String group;
    List<String> absoluteFilesNames;
    List<String> relativeFileNames;
    Map<String, String> extras;
  }
}

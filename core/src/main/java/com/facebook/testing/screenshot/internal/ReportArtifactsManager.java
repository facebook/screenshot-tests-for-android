/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.annotation.Nullable;

class ReportArtifactsManager {

  private final String mTestRunId;
  private final File mRootDir;
  private File mCurrentTestRunReportsDirectory;

  public ReportArtifactsManager(String testRunId, File rootDir) {
    mTestRunId = testRunId;
    mRootDir = rootDir;
  }

  public void recordFile(String fileName, byte[] content) throws IOException {
    File reportsDirectory = getOrCreateCurrentTestRunReportsDirectory();
    try (FileOutputStream recordedFile =
        new FileOutputStream(new File(reportsDirectory, fileName))) {
      recordedFile.write(content);
    }
  }

  @Nullable
  public File readFile(String fileName) {
    File requestedFile = new File(getOrCreateCurrentTestRunReportsDirectory(), fileName);
    if (requestedFile.isFile()) {
      return requestedFile;
    } else {
      return null;
    }
  }

  private File getOrCreateCurrentTestRunReportsDirectory() {
    if (mCurrentTestRunReportsDirectory == null) {
      mCurrentTestRunReportsDirectory = new File(mRootDir, mTestRunId);
      if (!mCurrentTestRunReportsDirectory.mkdir()
          && !mCurrentTestRunReportsDirectory.isDirectory()) {
        throw new IllegalStateException("Unable to create a directory to store reports.");
      }
    }
    return mCurrentTestRunReportsDirectory;
  }
}

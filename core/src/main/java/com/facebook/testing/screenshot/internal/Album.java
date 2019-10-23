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

import android.graphics.Bitmap;
import java.io.IOException;

/** Stores metadata about an album of screenshots during an instrumentation test run. */
public interface Album {

  /**
   * Writes the bitmap corresponding to the screenshot with the name {@code name} in the {@code
   * (tilei, tilej)} position.
   */
  String writeBitmap(String name, int tilei, int tilej, Bitmap bitmap) throws IOException;

  /** Call after all the screenshots are done. */
  void flush();

  /** Cleanup any disk state associated with this album. */
  void cleanup();

  /**
   * Opens a stream to dump the view hierarchy into. This should be called before addRecord() is
   * called for the given name.
   *
   * <p>It is the callers responsibility to call {@code close()} on the returned stream.
   */
  void writeViewHierarchyFile(String name, String data) throws IOException;

  /**
   * Opens a stream to dump the accessibility issues into. This should be called before addRecord()
   * is called for the given name.
   *
   * <p>It is the callers responsibility to call {@code close()} on the returned stream.
   */
  void writeAxIssuesFile(String name, String data) throws IOException;

  /** This is called after every record is finally set up. */
  void addRecord(RecordBuilderImpl recordBuilder) throws IOException;
}

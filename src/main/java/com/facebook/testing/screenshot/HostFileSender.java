/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;

/**
 * Abstraction for sending a file to the host system while the test is
 * running.
 *
 * When running screenshot tests, the space on the emulator disk can
 * fill up quickly, therefore this tool starts streaming the
 * screenshots while the test is running. However it is the
 * responsibility of the host script to parse the bundles sent over by
 * as instrumentation statuses, and retrieve the file specified in
 * "HostFileSender_filename", and then deleting the said file. If the
 * host script does not support this and does not pass
 * "HostFileSender_supported" argument, then the HostFileSender will
 * discard all files sent to it immediately.
 */
public class HostFileSender {
  final int QUEUE_SIZE = 5;
  private final List<File> mQueue = new ArrayList<>();

  private Instrumentation mInstrumentation;
  private Bundle mArguments;

  public HostFileSender(Instrumentation instrumentation, Bundle arguments) {
    mInstrumentation = instrumentation;
    mArguments = arguments;
  }

  /**
   * Sends the given file to the host system.
   *
   * Once passed in the file is "owned" by HostFileSender and should
   * not be modified beyond this point.
   */
  public synchronized void send(File file) {
    if (isDiscardMode()) {
      file.delete();
      return;
    }

    if (isHostFileSenderSupported()) {
        waitForQueue();

        Bundle bundle = new Bundle();
        bundle.putString("HostFileSender_filename", file.getAbsolutePath());
        mInstrumentation.sendStatus(Activity.RESULT_OK, bundle);
        mQueue.add(file);
    }
  }

  /**
   * Wait for all the files to be sent to host system.
   */
  public void flush() {
    updateQueue();
    while (getQueueSize() > 0) {
      updateQueue();
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /* VisibleForTesting */
  synchronized void updateQueue() {
    Iterator<File> iterator = mQueue.iterator();
    while (iterator.hasNext()) {
      File next = iterator.next();
      if (!next.exists()) {
        iterator.remove();
      }
    }
  }

  /* VisibleForTesting */
  synchronized int getQueueSize() {
    return mQueue.size();
  }

  synchronized private void waitForQueue() {
    updateQueue();
    while (getQueueSize() >= QUEUE_SIZE) {
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      updateQueue();
    }
  }

  /**
   * Returns true if we should discard files immediately instead of
   * waiting for the host system to pull them.
   */
  private boolean isDiscardMode() {
    return !isHostFileSenderSupported() && !isKeepFilesEnabled();
  }

  private boolean isHostFileSenderSupported() {
    return "true".equals(mArguments.getString("HostFileSender_supported"));
  }

  private boolean isKeepFilesEnabled() {
    return "true".equals(mArguments.getString("keep_files"));
  }
}

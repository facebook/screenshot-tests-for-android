/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 *
 * (We don't use the streaming mode internally at Facebook anymore,
 * but it was useful for a while. I don't recommend you use this mode,
 * instead just create an emulator with enough sdcard space.)
 */
public class HostFileSender {
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
    if (!isHostFileSenderSupported()) {
      return;
    }

    if (isDiscardMode()) {
      file.delete();
      return;
    }

    waitForQueue();

    Bundle bundle = new Bundle();
    bundle.putString("HostFileSender_filename", file.getAbsolutePath());
    mInstrumentation.sendStatus(Activity.RESULT_OK, bundle);
    mQueue.add(file);
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
    int queueSize = 5;
    while (getQueueSize() >= queueSize) {
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
   *
   * This is only useful if you're running the tests on a device that
   * does not have enough space on the sdcard to store all the
   * screenshots. In those cases, you still might want to run all the
   * instrumentation tests without the screenshot tests causing the
   * job to fail.
   */
  private boolean isDiscardMode() {
    return "true".equals(mArguments.getString("discard_screenshot_files"));
  }

  private boolean isHostFileSenderSupported() {
    return "true".equals(mArguments.getString("HostFileSender_supported"));
  }
}

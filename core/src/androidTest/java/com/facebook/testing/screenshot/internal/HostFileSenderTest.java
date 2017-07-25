/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link HostFileSender}
 */
@RunWith(AndroidJUnit4.class)
public class HostFileSenderTest {
  final List<Bundle> mStatus = new ArrayList<>();
  @Rule
  public TemporaryFolder mFolder = new TemporaryFolder();
  Instrumentation mInstrumentation;
  HostFileSender mHostFileSender;
  private boolean mDeleted = false;
  private boolean mFinished = false;

  @Before
  public void before() throws Exception {
    Bundle arguments = new Bundle();
    arguments.putString("HostFileSender_supported", "true");
    arguments.putString("keep_files", "true");

    mInstrumentation = new Instrumentation() {
      @Override
      public void sendStatus(int code, Bundle status) {
        mStatus.add(status);
      }
    };
    mHostFileSender = new HostFileSender(mInstrumentation, arguments);
  }

  private File newFile(String filename) throws IOException {
    File file = mFolder.newFile(filename);
    PrintWriter printWriter = null;

    try {
      printWriter = new PrintWriter(file);
      printWriter.append("foobar");
    } finally {
      if (printWriter != null) {
        printWriter.close();
      }
    }

    return file;
  }

  @Test
  public void testCreation() throws Throwable {
  }

  @Test
  public void testSendFileSendsStatus() throws Throwable {
    File file = newFile("foo");
    mHostFileSender.send(file);

    assertEquals(1, mStatus.size());
    assertEquals(file.getAbsolutePath(), mStatus.get(0).getString("HostFileSender_filename"));
  }

  @Test
  public void testQueueSize() throws Throwable {
    assertEquals(0, mHostFileSender.getQueueSize());
    mHostFileSender.send(newFile("foo"));
    assertEquals(1, mHostFileSender.getQueueSize());
  }

  @Test
  public void testUpdateQueueCleansUpStuff() throws Throwable {
    File file = newFile("foo");
    assertEquals(0, mHostFileSender.getQueueSize());
    mHostFileSender.send(file);
    assertEquals(1, mHostFileSender.getQueueSize());

    mHostFileSender.updateQueue();
    assertEquals(1, mHostFileSender.getQueueSize());

    file.delete();
    mHostFileSender.updateQueue();
    assertEquals(0, mHostFileSender.getQueueSize());
  }

  @Test
  public void testWaitForQueue() throws Throwable {
    final File toDelete = newFile("foo");
    mHostFileSender.send(toDelete);
    for (int i = 0; i < 4; i++) {
      mHostFileSender.send(newFile(String.valueOf(i)));
    }

    // The next one should block until we delete a file
    Thread thread = new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        synchronized (HostFileSenderTest.this) {
          mDeleted = true;
          toDelete.delete();
        }
      }
    };
    thread.start();
    mHostFileSender.send(newFile("6"));

    synchronized (this) {
      assertTrue(mDeleted);
    }
    thread.join();
  }

  @Test
  public void testFlushWithoutDiscard() throws Throwable {
    final File one = newFile("one");
    final File two = newFile("two");
    mHostFileSender.send(one);
    mHostFileSender.send(two);

    Thread thread = new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        assertFalse(mFinished);
        one.delete();
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        assertFalse(mFinished);
        two.delete();
      }
    };

    synchronized (this) {
      thread.start();
      mHostFileSender.flush();
      mFinished = true;
    }

    thread.join();
  }

  @Test
  public void testDiscardMode() throws Throwable {
    Bundle args = new Bundle();
    args.putString("HostFileSender_supported", "true");
    mHostFileSender = new HostFileSender(mInstrumentation, args);

    File file = newFile("foo");
    assertTrue(file.exists());
    mHostFileSender.send(file);
    assertTrue(file.exists());
  }

  @Test
  public void testExplicitDiscard() throws Throwable {
    Bundle args = new Bundle();
    args.putString("HostFileSender_supported", "true");
    args.putString("discard_screenshot_files", "true");
    mHostFileSender = new HostFileSender(mInstrumentation, args);

    File file = newFile("foo");
    assertTrue(file.exists());
    mHostFileSender.send(file);
    assertFalse(file.exists());
  }

  @Test
  public void testNoArgsDontDiscard() throws Throwable {
    Bundle args = new Bundle();
    mHostFileSender = new HostFileSender(mInstrumentation, args);

    File file = newFile("foo");
    assertTrue(file.exists());
    mHostFileSender.send(file);
    assertTrue(file.exists());
  }

  @Test
  public void testFixesExternalDirectory() throws Throwable {
    Bundle args = new Bundle();
    String externalDirectory = System.getenv("EXTERNAL_STORAGE");
    assertThat(externalDirectory,
        is(not(isEmptyOrNullString())));

    ScreenshotDirectories sd = new ScreenshotDirectories(InstrumentationRegistry.getTargetContext());
    File file = sd.get("default");
    mHostFileSender.send(file);

    assertThat(mStatus.get(0).getString("HostFileSender_filename"),
        startsWith(externalDirectory));
  }
}

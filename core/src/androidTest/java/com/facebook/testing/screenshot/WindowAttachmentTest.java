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
package com.facebook.testing.screenshot;

import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import android.app.KeyguardManager;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.LinearLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests {@link WindowAttachment} */
@RunWith(AndroidJUnit4.class)
public class WindowAttachmentTest extends ActivityInstrumentationTestCase2<MyActivity> {

  public WindowAttachmentTest() {
    super(MyActivity.class);
  }

  private Context mContext;
  private int mAttachedCalled = 0;
  private int mDetachedCalled = 0;
  private KeyguardManager.KeyguardLock mLock;

  @Before
  public void setUp() throws Exception {
    mContext = InstrumentationRegistry.getTargetContext();
    injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    super.setUp();
    KeyguardManager km =
        (KeyguardManager)
            getInstrumentation().getTargetContext().getSystemService(Context.KEYGUARD_SERVICE);
    mLock = km.newKeyguardLock("SelectAtTagActivityTest");
    mLock.disableKeyguard();
  }

  @After
  public void tearDown() throws Exception {
    mLock.reenableKeyguard();
    super.tearDown();
  }

  @Test
  public void testCalled() throws Throwable {
    MyView view = new MyView(mContext);
    WindowAttachment.Detacher detacher = WindowAttachment.dispatchAttach(view);
    assertEquals(1, mAttachedCalled);
    assertEquals(0, mDetachedCalled);

    detacher.detach();
    assertEquals(1, mDetachedCalled);
  }

  @Test
  public void testCalledForViewGroup() throws Throwable {
    Parent view = new Parent(mContext);
    WindowAttachment.Detacher detacher = WindowAttachment.dispatchAttach(view);
    assertEquals(1, mAttachedCalled);
    assertEquals(0, mDetachedCalled);

    detacher.detach();
    assertEquals(1, mDetachedCalled);
  }

  @Test
  public void testForNested() throws Throwable {
    Parent view = new Parent(mContext);
    MyView child = new MyView(mContext);
    view.addView(child);

    WindowAttachment.Detacher detacher = WindowAttachment.dispatchAttach(view);
    assertEquals(2, mAttachedCalled);
    assertEquals(0, mDetachedCalled);

    detacher.detach();
    assertEquals(2, mDetachedCalled);
  }

  @Test
  public void testAReallyAttachedViewIsntAttacedAgain() throws Throwable {
    final View[] view = new View[1];

    getActivity();
    InstrumentationRegistry.getInstrumentation()
        .runOnMainSync(
            new Runnable() {
              @Override
              public void run() {
                view[0] = new MyView(getActivity());
                getActivity().setContentView(view[0]);
              }
            });

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    // Call some espress method to make sure we're ready:
    Espresso.onView(withId(android.R.id.content)).perform(click());

    mAttachedCalled = 0;
    mDetachedCalled = 0;

    WindowAttachment.Detacher detacher = WindowAttachment.dispatchAttach(view[0]);
    detacher.detach();

    assertEquals(0, mAttachedCalled);
    assertEquals(0, mDetachedCalled);
  }

  @Test
  public void testSetAttachInfo() throws Throwable {
    final MyView view = new MyView(mContext);
    InstrumentationRegistry.getInstrumentation()
        .runOnMainSync(
            new Runnable() {
              @Override
              public void run() {
                WindowAttachment.setAttachInfo(view);
              }
            });

    assertNotNull(view.getWindowToken());
  }

  public class MyView extends View {
    public MyView(Context context) {
      super(context);
    }

    @Override
    protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      mAttachedCalled++;
    }

    @Override
    protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      mDetachedCalled++;
    }
  }

  public class Parent extends LinearLayout {
    public Parent(Context context) {
      super(context);
    }

    @Override
    protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      mAttachedCalled++;
    }

    @Override
    protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      mDetachedCalled++;
    }
  }
}

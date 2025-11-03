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

package com.facebook.testing.screenshot;

import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests {@link WindowAttachment} */
@RunWith(AndroidJUnit4.class)
public class WindowAttachmentTest {

  private Context mContext;
  private int mAttachedCalled = 0;
  private int mDetachedCalled = 0;
  private KeyguardManager.KeyguardLock mLock;

  @Rule
  public ActivityTestRule<MyActivity> activityTestRule = new ActivityTestRule<>(MyActivity.class);

  @Before
  public void setUp() throws Exception {
    mContext = InstrumentationRegistry.getTargetContext();
    KeyguardManager km = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
    mLock = km.newKeyguardLock("SelectAtTagActivityTest");
    mLock.disableKeyguard();
  }

  @After
  public void tearDown() throws Exception {
    mLock.reenableKeyguard();
  }

  @Test
  public void testCalled() throws Throwable {
    MyView view = new MyView(mContext);
    WindowAttachment.Detacher detacher = WindowAttachment.dispatchAttach(view);
    assertThat(mAttachedCalled).isEqualTo(1);
    assertThat(mDetachedCalled).isEqualTo(0);

    detacher.detach();
    assertThat(mDetachedCalled).isEqualTo(1);
  }

  @Test
  public void testCalledForViewGroup() throws Throwable {
    Parent view = new Parent(mContext);
    WindowAttachment.Detacher detacher = WindowAttachment.dispatchAttach(view);
    assertThat(mAttachedCalled).isEqualTo(1);
    assertThat(mDetachedCalled).isEqualTo(0);

    detacher.detach();
    assertThat(mDetachedCalled).isEqualTo(1);
  }

  @Test
  public void testForNested() throws Throwable {
    Parent view = new Parent(mContext);
    MyView child = new MyView(mContext);
    view.addView(child);

    WindowAttachment.Detacher detacher = WindowAttachment.dispatchAttach(view);
    assertThat(mAttachedCalled).isEqualTo(2);
    assertThat(mDetachedCalled).isEqualTo(0);

    detacher.detach();
    assertThat(mDetachedCalled).isEqualTo(2);
  }

  @Test
  public void testAReallyAttachedViewIsntAttacedAgain() throws Throwable {
    final View[] view = new View[1];

    activityTestRule.getActivity();
    InstrumentationRegistry.getInstrumentation()
        .runOnMainSync(
            new Runnable() {
              @Override
              public void run() {
                view[0] = new MyView(activityTestRule.getActivity());
                activityTestRule.getActivity().setContentView(view[0]);
              }
            });

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    // Call some espress method to make sure we're ready:
    Espresso.onView(withId(android.R.id.content)).perform(click());

    mAttachedCalled = 0;
    mDetachedCalled = 0;

    WindowAttachment.Detacher detacher = WindowAttachment.dispatchAttach(view[0]);
    detacher.detach();

    assertThat(mAttachedCalled).isEqualTo(0);
    assertThat(mDetachedCalled).isEqualTo(0);
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

    assertThat(view.getWindowToken()).isNotNull();
  }

  public class MyView extends View {
    public MyView(Context context) {
      super(context);
      try {
        Looper.prepare();
      } catch (Throwable t) {

      }
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
      try {
        Looper.prepare();
      } catch (Throwable t) {

      }
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

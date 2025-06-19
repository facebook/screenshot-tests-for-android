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

import android.app.Instrumentation;
import android.os.Bundle;
import com.facebook.infer.annotation.Nullsafe;
import javax.annotation.Nullable;

/** Stores some of the static state. We bundle this into a class for easy cleanup. */
@Nullsafe(Nullsafe.Mode.LOCAL)
public class Registry {
  @Nullable private static Registry sRegistry;
  // NULLSAFE_FIXME[Field Not Initialized]
  public Instrumentation instrumentation;
  // NULLSAFE_FIXME[Field Not Initialized]
  public Bundle arguments;

  Registry() {}

  public static Registry getRegistry() {
    if (sRegistry == null) {
      sRegistry = new Registry();
    }

    return sRegistry;
  }

  public static void clear() {
    sRegistry = null;
  }
}

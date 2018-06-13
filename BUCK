# BUILD FILE SYNTAX: SKYLARK
# Copyright 2014-present Facebook, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
load("//:screenshot_test_defs.bzl", "SCREENSHOT_TESTS_CORE_TARGET", "SCREENSHOT_TESTS_LAYOUT_HIERARCHY_COMMON_TARGET", "SCREENSHOT_TESTS_LAYOUT_HIERARCHY_LITHO_TARGET", "SCREENSHOT_TESTS_VISIBILITY", "fb_core_android_library")

fb_core_android_library(
    name = "screenshot",
    visibility = SCREENSHOT_TESTS_VISIBILITY,
    deps = [
        SCREENSHOT_TESTS_CORE_TARGET,
        SCREENSHOT_TESTS_LAYOUT_HIERARCHY_COMMON_TARGET,
        SCREENSHOT_TESTS_LAYOUT_HIERARCHY_LITHO_TARGET,
    ],
    exported_deps = [
        SCREENSHOT_TESTS_CORE_TARGET,
        SCREENSHOT_TESTS_LAYOUT_HIERARCHY_COMMON_TARGET,
        SCREENSHOT_TESTS_LAYOUT_HIERARCHY_LITHO_TARGET,
    ],
)

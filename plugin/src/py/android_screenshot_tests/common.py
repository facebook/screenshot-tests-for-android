#!/usr/bin/env python
# Copyright (c) Facebook, Inc. and its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import re
import subprocess
import sys

def get_image_file_name(name, x, y):
    image_file = name
    if x != 0 or y != 0:
        image_file += "_%d_%d" % (x, y)

    image_file += ".png"
    return image_file

def get_android_sdk():
    android_sdk = os.environ.get('ANDROID_SDK') or os.environ.get('ANDROID_HOME')

    if not android_sdk:
        raise RuntimeError("ANDROID_SDK or ANDROID_HOME needs to be set")

    return os.path.expanduser(android_sdk)

def get_adb():
    return os.path.join(get_android_sdk(), "platform-tools", "adb")

# a version of subprocess.check_output that returns a utf-8 string
def check_output(args, **kwargs):
    return subprocess.check_output(args, **kwargs).decode('utf-8')

# a compat version for py3, since assertRegexpMatches is deprecated
def assertRegex(testcase, regex, string):
    if sys.version_info >= (3,):
        testcase.assertRegex(regex, string)
    else:
        testcase.assertRegexpMatches(regex, string)

def get_connected_devices():
    try:
        output = check_output([get_adb(), "devices"]).splitlines()
        target_pattern = re.compile(r"\b(device|emulator)\b")
        return [line.split()[0] for line in output if target_pattern.search(line) and "offline" not in line]
    except subprocess.CalledProcessError:
        return None

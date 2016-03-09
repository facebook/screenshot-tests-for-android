#!/usr/bin/env python
#
# Copyright (c) 2014-present, Facebook, Inc.
# All rights reserved.
#
# This source code is licensed under the BSD-style license found in the
# LICENSE file in the root directory of this source tree. An additional grant
# of patent rights can be found in the PATENTS file in the same directory.
#

import os
import sys
import subprocess

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

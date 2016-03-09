#!/usr/bin/env python
#
# Copyright (c) 2014-present, Facebook, Inc.
# All rights reserved.
#
# This source code is licensed under the BSD-style license found in the
# LICENSE file in the root directory of this source tree. An additional grant
# of patent rights can be found in the PATENTS file in the same directory.
#

import unittest
import os
from . import common
import subprocess
import sys

class TestCommon(unittest.TestCase):
    def setUp(self):
        self.android_sdk = common.get_android_sdk()
        self._environ = dict(os.environ)
        os.environ.pop('ANDROID_SDK', None)
        os.environ.pop('ANDROID_HOME', None)

    def tearDown(self):
        os.environ.clear()
        os.environ.update(self._environ)

    def test_get_android_sdk_happy_path(self):
        os.environ['ANDROID_SDK'] = '/tmp/foo'
        self.assertEqual("/tmp/foo", common.get_android_sdk())

    def test_tilde_is_expanded(self):
        if sys.version_info >= (3,):
            return

        os.environ['ANDROID_SDK'] = '~/foobar'

        home = os.environ['HOME']

        self.assertEqual(os.path.join(home, 'foobar'), common.get_android_sdk())

    def test_get_adb_can_run_in_subprocess(self):
        os.environ['ANDROID_SDK'] = self.android_sdk
        subprocess.check_call([common.get_adb(), "devices"])

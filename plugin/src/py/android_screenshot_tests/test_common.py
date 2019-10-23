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

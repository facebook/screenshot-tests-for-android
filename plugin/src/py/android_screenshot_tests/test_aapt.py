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

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import unittest
from . import aapt
import os
import tempfile
import shutil
from os.path import join, dirname
from .common import assertRegex

CURDIR = dirname(__file__)

class TestAapt(unittest.TestCase):
    def setUp(self):
        os.oldenviron = dict(os.environ)
        self.android_sdk = tempfile.mkdtemp()
        os.mkdir(join(self.android_sdk, "build-tools"))
        os.environ['ANDROID_SDK'] = os.environ.get('ANDROID_SDK') or os.environ.get('ANDROID_HOME')
        os.environ.pop('ANDROID_HOME', None)

    def tearDown(self):
        os.environ.clear()
        os.environ.update(os.oldenviron)
        shutil.rmtree(self.android_sdk)

    def _use_mock(self):
        os.environ['ANDROID_SDK'] = self.android_sdk

    def _add_aapt(self, version):
        f = join(self.android_sdk, "build-tools", version)
        os.mkdir(f)
        open(join(f, "aapt"), "w").close()

    def test_finds_aapt_happy_path(self):
        self.assertTrue(aapt.get_aapt_bin().endswith("aapt"))

    def test_finds_an_aapt_happy_path(self):
        self._use_mock()
        self._add_aapt("21.0")
        self.assertEqual(join(self.android_sdk, "build-tools", "21.0", "aapt"), aapt.get_aapt_bin())

    def test_finds_the_aapt_with_highest_version(self):
        self._use_mock()
        self._add_aapt("21.0")
        self._add_aapt("22.0")
        self.assertEqual(join(self.android_sdk, "build-tools", "22.0", "aapt"), aapt.get_aapt_bin())

    def test_does_not_use_old_android_versions(self):
        self._use_mock()
        self._add_aapt("21.0")
        self._add_aapt("22.0")
        self._add_aapt("android-4.1")
        self.assertEqual(join(self.android_sdk, "build-tools", "22.0", "aapt"), aapt.get_aapt_bin())

    def test_no_android_sdk(self):
        os.environ.pop('ANDROID_SDK')

        try:
            aapt.get_aapt_bin()
            self.fail("expected exception")
        except RuntimeError as e:
            assertRegex(self, e.args[0], ".*ANDROID_SDK.*")

    def test_no_build_tools(self):
        self._use_mock()

        try:
            aapt.get_aapt_bin()
            self.fail("expected exception")
        except RuntimeError as e:
            assertRegex(self, e.args[0], ".*Could not find build-tools.*")

    def test_get_package_name(self):
        self.assertEqual('com.facebook.testing.screenshot.examples',
                          aapt.get_package(join(CURDIR, "example.apk")))

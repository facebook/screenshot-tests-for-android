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

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import unittest
from .simple_puller import SimplePuller
import subprocess
import tempfile
from .common import get_adb
import shutil
import os
from . import common

class TestSimplePuller(unittest.TestCase):
    def setUp(self):
        self.puller = SimplePuller()
        self.serial = common.check_output(
            [get_adb(), "get-serialno"]).strip()

        subprocess.check_call([
            get_adb(), "shell",
            "echo foobar > /sdcard/blah"])
        self.tmpdir = tempfile.mkdtemp()

    def tearDown(self):
        shutil.rmtree(self.tmpdir)
        subprocess.check_call([
            get_adb(), "shell", "rm", "-f", "/sdcard/blah"])

    def test_pull_integration(self):
        file = os.path.join(self.tmpdir, "foo")
        self.puller.pull("/sdcard/blah", file)

        with open(file, "rt") as f2:
            self.assertEqual("foobar\n", f2.read())

    def test_file_exists(self):
        self.assertTrue(self.puller.remote_file_exists("/sdcard/blah"))
        self.assertFalse(self.puller.remote_file_exists("/sdcard/sdfdsfdf"))

    def test_pull_with_filter(self):
        self.puller = SimplePuller(["-s", self.serial])
        self.test_pull_integration()

    def test_get_external_data_dir(self):
        accepted_dirs = [
            '/mnt/sdcard',
            '/sdcard',
            '/storage/sdcard',
            '/storage/emulated/legacy',
        ]
        self.assertIn(self.puller.get_external_data_dir(), accepted_dirs)

if __name__ == '__main__':
    unittest.main()

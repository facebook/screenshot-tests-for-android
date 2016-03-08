#!/usr/bin/env python
#
# Copyright (c) 2014-present, Facebook, Inc.
# All rights reserved.
#
# This source code is licensed under the BSD-style license found in the
# LICENSE file in the root directory of this source tree. An additional grant
# of patent rights can be found in the PATENTS file in the same directory.
#

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import unittest
from .simple_puller import SimplePuller
import subprocess
import tempfile
from .common import get_adb

class TestSimplePuller(unittest.TestCase):
    def setUp(self):
        self.puller = SimplePuller()
        self.serial = subprocess.check_output(
            [get_adb(), "get-serialno"]).strip()

        subprocess.check_call([
            get_adb(), "shell",
            "echo foobar > /sdcard/blah"])

    def tearDown(self):
        subprocess.check_call([
            get_adb(), "shell", "rm", "-f", "/sdcard/blah"])

    def test_pull_integration(self):
        with tempfile.NamedTemporaryFile() as f:
            self.puller.pull("/sdcard/blah", f.name)

            with open(f.name, "r") as f2:
                self.assertEquals("foobar\n", f2.read())

    def test_file_exists(self):
        self.assertTrue(self.puller.remote_file_exists("/sdcard/blah"))
        self.assertFalse(self.puller.remote_file_exists("/sdcard/sdfdsfdf"))

    def test_pull_with_filter(self):
        self.puller = SimplePuller(["-s", self.serial])
        self.test_pull_integration()

    def test_get_external_data_dir(self):
        self.assertRegexpMatches(self.puller.get_external_data_dir(), '.*sdcard.*')

if __name__ == '__main__':
    unittest.main()

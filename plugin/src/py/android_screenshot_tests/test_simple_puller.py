#!/usr/bin/env python3
# Copyright (c) Meta Platforms, Inc. and affiliates.
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

from __future__ import absolute_import, division, print_function, unicode_literals

import os
import shutil
import subprocess
import tempfile
import unittest

from . import common
from .common import get_adb
from .simple_puller import SimplePuller


class TestSimplePuller(unittest.TestCase):
    def setUp(self):
        self.puller = SimplePuller()
        self.serial = common.check_output([get_adb(), "get-serialno"]).strip()

        subprocess.check_call([get_adb(), "shell", "echo foobar > /sdcard/blah"])
        self.tmpdir = tempfile.mkdtemp()

    def tearDown(self):
        shutil.rmtree(self.tmpdir)
        subprocess.check_call([get_adb(), "shell", "rm", "-f", "/sdcard/blah"])

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
            "/mnt/sdcard",
            "/sdcard",
            "/storage/sdcard",
            "/storage/emulated/legacy",
        ]
        self.assertIn(self.puller.get_external_data_dir(), accepted_dirs)

    def test_pull_folder(self):
        target_remote_folder = "/sdcard/folder"
        target_remote_sub_folders = [".", "a", "b"]
        subprocess.check_call([get_adb(), "shell", f"mkdir -p {target_remote_folder}"])
        for sub_folder in target_remote_sub_folders:
            subprocess.check_call(
                [get_adb(), "shell", f"mkdir -p {target_remote_folder}/{sub_folder}"]
            )
            for i in range(10):
                subprocess.check_call(
                    [
                        get_adb(),
                        "shell",
                        f"echo foobar{i} > {target_remote_folder}/{sub_folder}/pic{i}.png",
                    ]
                )
        self.puller.pull_folder(target_remote_folder, self.tmpdir)

        for sub_folder in target_remote_sub_folders:
            for i in range(10):
                file = os.path.join(self.tmpdir, sub_folder, f"pic{i}.png")
                with open(file, "rt") as f2:
                    self.assertEqual(f"foobar{i}\n", f2.read())


if __name__ == "__main__":
    unittest.main()

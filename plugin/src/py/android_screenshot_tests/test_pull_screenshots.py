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
import os
import sys
import subprocess
from . import pull_screenshots
import tempfile
import shutil

from mock import *

from mockproc import mockprocess

TESTING_PACKAGE = 'com.foo'
CURRENT_DIR = os.path.dirname(__file__)

class AdbPuller:
    def pull(self, src, dest):
        src = CURRENT_DIR + "/fixtures/" + src
        shutil.copyfile(src, dest)

    def remote_file_exists(self, src):
        src = CURRENT_DIR + "/fixtures/" + src
        return os.path.exists(src)

class TestAdbHelpers(unittest.TestCase):
    def setUp(self):
        self.tmpdir = tempfile.mkdtemp(prefix="screenshots")

    def tearDown(self):
        shutil.rmtree(self.tmpdir)

    def test_pull_metadata_without_metadata(self):
        adb_instance = MagicMock()

        adb_instance.remote_file_exists = MagicMock()
        adb_instance.remote_file_exists.return_value = False

        adb_instance.pull = MagicMock()
        adb_instance.pull.side_effect = Exception("should not be called")

        pull_screenshots.pull_all("com.facebook.testing.tests", self.tmpdir, adb_puller=AdbPuller())

        self.assertTrue(os.path.exists(self.tmpdir + "/metadata.xml"))

class TestPullScreenshots(unittest.TestCase):
    def setUp(self):
        self.output_file = tempfile.mkstemp(prefix="final_screenshot", suffix=".png")[1]
        os.unlink(self.output_file)
        self.tmpdir = None
        self.scripts = mockprocess.MockProc()
        self.oldstdout = sys.stdout

    def tearDown(self):
        if self.oldstdout:
            sys.stdout = self.oldstdout

        if os.path.exists(self.output_file):
            os.unlink(self.output_file)
        if self.tmpdir:
            shutil.rmtree(self.tmpdir)

    # def test_integration(self):
    #     self.scripts.append("which", returncode=0)
    #     self.scripts.append("wkhtmltoimage", script="""#!/bin/sh\ntouch $2""")

    #     with self.scripts:
    #         pull_screenshots.main(["./a.out",
    #                                TESTING_PACKAGE,
    #                                "--generate-png=%s" % self.output_file])
    #         self.assertTrue(os.path.exists(self.output_file))

    def test_index_html_created(self):
        self.tmpdir = tempfile.mkdtemp(prefix='screenshots')
        pull_screenshots.pull_screenshots(
            TESTING_PACKAGE,
            adb_puller=AdbPuller(),
            temp_dir=self.tmpdir)
        self.assertTrue(os.path.exists(self.tmpdir + "/index.html"))

    def test_image_is_linked(self):
        self.tmpdir = tempfile.mkdtemp(prefix='screenshots')
        pull_screenshots.pull_screenshots(
            TESTING_PACKAGE,
            adb_puller=AdbPuller(),
            temp_dir=self.tmpdir)
        with open(self.tmpdir + "/index.html", "r") as f:
            contents = f.read()
            self.assertRegexpMatches(contents, ".*com.foo.*")
            self.assertTrue(contents.find('<img src="./com.foo.') >= 0)

    def test_generate_html_returns_a_valid_file(self):
        self.tmpdir = tempfile.mkdtemp(prefix='screenshots')
        pull_screenshots.pull_all(TESTING_PACKAGE, self.tmpdir, adb_puller=AdbPuller())
        html = pull_screenshots.generate_html(self.tmpdir)
        self.assertTrue(os.path.exists(html))

    def test_adb_puller_sanity(self):
        self.assertTrue(AdbPuller().remote_file_exists("/sdcard"))

    # def test_integration_with_filter(self):
    #     self.scripts.append("which", returncode=0)
    #     self.scripts.append("wkhtmltoimage", script="#!/bin/sh\ntouch $2")
    #     with self.scripts:
    #         pull_screenshots.main(["process-name",
    #                                TESTING_PACKAGE,
    #                                "--filter-name-regex=.*ScreenshotFixture.*",
    #                                "--generate-png=%s" % self.output_file])
    #         self.assertTrue(os.path.exists(self.output_file))

    def test_copy_file_zip_aware_real_file(self):
        with tempfile.NamedTemporaryFile() as f, tempfile.NamedTemporaryFile() as f2:
            f.write("foobar")
            f.flush()
            pull_screenshots._copy_file(f.name, f2.name)

            self.assertEquals("foobar", f2.read())

    def test_copy_file_inside_zip(self):
        with tempfile.NamedTemporaryFile() as f:
            pull_screenshots._copy_file(CURRENT_DIR + '/fixtures/dummy.zip/AndroidManifest.xml',
                                        f.name)

            self.assertRegexpMatches(f.read(), '.*manifest.*')

    def test_summary_happyPath(self):
        with tempfile.NamedTemporaryFile() as f:
            sys.stdout = f
            pull_screenshots._summary(CURRENT_DIR + '/fixtures/sdcard/screenshots/' + TESTING_PACKAGE + '/screenshots-default')
            sys.stdout.flush()

            f.seek(0)
            message = f.read()
            self.assertRegexpMatches(message, ".*3 screenshots.*")


if __name__ == '__main__':
    unittest.main()

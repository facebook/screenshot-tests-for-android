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

import errno
import json
import os
import shutil
import sys
import tempfile
import unittest
from os.path import join

from . import pull_screenshots

if sys.version_info >= (3,):
    from unittest.mock import *
else:
    from mock import *

from .common import assertRegex

TESTING_PACKAGE = "com.foo"
CURRENT_DIR = os.path.dirname(__file__)
FIXTURE_DIR = "%s/fixtures/sdcard/screenshots/%s/screenshots-default" % (
    CURRENT_DIR,
    TESTING_PACKAGE,
)


class LocalFileHelper:
    def setup(self, dir, test_run_id):
        shutil.copyfile(
            FIXTURE_DIR + "/metadata_no_errors.json", dir + "/metadata.json"
        )
        shutil.copyfile(
            FIXTURE_DIR
            + "/"
            + test_run_id
            + "/com.foo.ScriptsFixtureTest_testGetTextViewScreenshot.png",
            dir + "/com.foo.ScriptsFixtureTest_testGetTextViewScreenshot.png",
        )
        shutil.copyfile(
            FIXTURE_DIR
            + "/"
            + test_run_id
            + "/com.foo.ScriptsFixtureTest_testSecondScreenshot.png",
            dir + "/com.foo.ScriptsFixtureTest_testSecondScreenshot.png",
        )


def assert_nice_filename(filename):
    if "//" in filename:
        raise RuntimeError(
            "%s is not a canonical filename and can cause problems that are hard to debug"
            % filename
        )


class AdbPuller:
    def __init__(self, fixture_dir=join(CURRENT_DIR, "fixtures")):
        self.fixture_dir = fixture_dir

    def pull(self, src, dest):
        self._valid_src(src)
        assert_nice_filename(src)
        src = self.fixture_dir + src
        self._copy(src, dest)

    def pull_folder(self, src, dest):
        self.pull(src, dest)

    def remote_file_exists(self, src):
        self._valid_src(src)
        assert_nice_filename(src)
        src = self.fixture_dir + src
        return os.path.exists(src)

    def _valid_src(self, src):
        if not src.startswith("/"):
            raise RuntimeError("src must be absolute, not: " + src)

    def get_external_data_dir(self):
        return "/sdcard"

    def _copy(self, src, dst):
        try:
            shutil.copytree(src, dst)
        except OSError as exc:
            if exc.errno == errno.ENOTDIR:
                shutil.copy(src, dst)
            else:
                raise


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

        pull_screenshots.pull_all(
            "com.facebook.testing.tests",
            self.tmpdir,
            test_run_id="unittest",
            adb_puller=AdbPuller(),
        )

        self.assertTrue(os.path.exists(self.tmpdir + "/metadata.json"))


class TestPullScreenshots(unittest.TestCase):
    def setUp(self):
        fd, self.output_file = tempfile.mkstemp(
            prefix="final_screenshot", suffix=".png"
        )
        os.close(fd)
        os.unlink(self.output_file)
        self.tmpdir = None
        self.oldstdout = sys.stdout
        self.oldenviron = dict(os.environ)

    def tearDown(self):
        os.environ.clear()
        os.environ.update(self.oldenviron)
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
        self.tmpdir = tempfile.mkdtemp(prefix="screenshots")
        pull_screenshots.pull_screenshots(
            TESTING_PACKAGE,
            adb_puller=AdbPuller(),
            temp_dir=self.tmpdir,
            test_run_id="unittest",
        )
        self.assertTrue(os.path.exists(self.tmpdir + "/index.html"))

    def test_image_is_linked(self):
        self.tmpdir = tempfile.mkdtemp(prefix="screenshots")
        pull_screenshots.pull_screenshots(
            TESTING_PACKAGE,
            adb_puller=AdbPuller(),
            temp_dir=self.tmpdir,
            test_run_id="unittest",
        )
        with open(self.tmpdir + "/index.html", "r") as f:
            contents = f.read()
            print(contents)
            assertRegex(self, contents, ".*com.foo.*")
            self.assertTrue(contents.find('<img src="./com.foo.') >= 0)

    def test_generate_html_returns_a_valid_file(self):
        self.tmpdir = tempfile.mkdtemp(prefix="screenshots")
        pull_screenshots.pull_all(
            TESTING_PACKAGE, self.tmpdir, "unittest", adb_puller=AdbPuller()
        )
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
        self.tmpdir = tempfile.mkdtemp()

        f1 = os.path.join(self.tmpdir, "foo")
        f2 = os.path.join(self.tmpdir, "bar")

        with open(f1, "wt") as f:
            f.write("foobar")
            f.flush()

        pull_screenshots._copy_file(f1, f2)

        with open(f2, "rt") as f:
            self.assertEqual("foobar", f.read())

    def test_copy_file_inside_zip(self):
        with tempfile.NamedTemporaryFile() as f:
            f.close()
            pull_screenshots._copy_file(
                CURRENT_DIR + "/fixtures/dummy.zip/AndroidManifest.xml", f.name
            )
            with open(f.name, "rt") as ff:
                assertRegex(self, ff.read(), ".*manifest.*")

    def test_summary_happyPath(self):
        with tempfile.NamedTemporaryFile(mode="w+t") as f:
            sys.stdout = f
            pull_screenshots._summary(
                CURRENT_DIR
                + "/fixtures/sdcard/screenshots/"
                + TESTING_PACKAGE
                + "/screenshots-default"
            )
            sys.stdout.flush()

            f.seek(0)
            message = f.read()
            assertRegex(self, message, ".*3 screenshots.*")

    def test_setup_paths(self):
        os.environ["ANDROID_SDK"] = "foobar"
        pull_screenshots.setup_paths()
        assertRegex(self, os.environ["PATH"], ".*:foobar/platform-tools.*")

    def test_no_pull_argument_does_not_use_adb_on_verify(self):
        source = tempfile.mkdtemp()
        dest = tempfile.mkdtemp()

        LocalFileHelper().setup(source, "unittest")
        LocalFileHelper().setup(dest, "unittest")

        pull_screenshots.pull_screenshots(
            TESTING_PACKAGE,
            adb_puller=None,
            perform_pull=False,
            temp_dir=source,
            test_run_id="unittest",
            verify=dest,
        )

    def test_no_pull_argument_does_not_use_adb_on_record(self):
        source = tempfile.mkdtemp()
        dest = tempfile.mkdtemp()

        LocalFileHelper().setup(source, "unittest")
        LocalFileHelper().setup(dest, "unittest")

        pull_screenshots.pull_screenshots(
            TESTING_PACKAGE,
            adb_puller=None,
            perform_pull=False,
            temp_dir=source,
            test_run_id="unittest",
            record=dest,
        )

    def test_no_pull_argument_must_have_temp_dir(self):
        try:
            pull_screenshots.pull_screenshots(
                TESTING_PACKAGE,
                adb_puller=None,
                perform_pull=False,
                temp_dir=None,
                verify=tempfile.mkdtemp(),
            )
            self.fail("expected exception")
        except RuntimeError as e:
            assertRegex(self, e.args[0], "You must supply a directory for temp_dir")

    def test_screenshots_with_same_group_ordered_together(self):
        loaded_json = json.loads(
            # language=json
            """[
                    {
                        "name": "one",
                        "group": "foo"
                    },
                    {
                        "name": "two"
                    },
                    {
                        "name": "three",
                        "group": "foo"
                    }
               ]
"""
        )

        screenshots = pull_screenshots.sort_screenshots(loaded_json)

        self.assertEqual(["two", "one", "three"], [x.get("name") for x in screenshots])

    def test_invalid_json(self):
        source = join(tempfile.mkdtemp(), "foo")
        shutil.copytree(join(CURRENT_DIR, "fixtures"), source)

        metadata_file = join(
            source, "sdcard/screenshots/com.foo/screenshots-default/metadata.json"
        )
        self.assertTrue(os.path.exists(metadata_file))

        with open(metadata_file, "w") as f:
            f.write("{invalid-json{")
            f.flush()

        adb_puller = AdbPuller(source)

        try:
            pull_screenshots.pull_screenshots(
                TESTING_PACKAGE, test_run_id="unittest", adb_puller=adb_puller
            )
            self.fail("expected exception")
        except RuntimeError as e:
            assertRegex(self, e.args[0], ".*ScreenshotRunner.*")


class TestAndroidJoin(unittest.TestCase):
    def test_simple(self):
        self.assertEqual("/foo/bar", pull_screenshots.android_path_join("/foo", "bar"))
        self.assertEqual("/foo/bar", pull_screenshots.android_path_join("/foo/", "bar"))

    def test_multiple(self):
        self.assertEqual(
            "/foo/bar/car", pull_screenshots.android_path_join("/foo", "bar/", "car")
        )

    def test_root(self):
        self.assertEqual("/bar", pull_screenshots.android_path_join("/foo", "/bar"))


if __name__ == "__main__":
    unittest.main()

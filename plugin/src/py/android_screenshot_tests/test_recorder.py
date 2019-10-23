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

import tempfile
import unittest
import shutil
import os
from os.path import join, exists
from .recorder import Recorder, VerifyError

from PIL import Image

class TestRecorder(unittest.TestCase):
    def setUp(self):
        self.outputdir = tempfile.mkdtemp()
        self.inputdir = tempfile.mkdtemp()
        self.failureDir = tempfile.mkdtemp()
        self.tmpimages = []
        self.recorder = Recorder(self.inputdir, self.outputdir, self.failureDir)

    def create_temp_image(self, name, dimens, color):
        im = Image.new("RGBA", dimens, color)
        filename = os.path.join(self.inputdir, name)
        im.save(filename, "PNG")
        im.close()
        return filename

    def make_metadata(self, str):
        with open(os.path.join(self.inputdir, "metadata.xml"), "w") as f:
            f.write(str)

    def tearDown(self):
        for f in self.tmpimages:
            f.close()

        shutil.rmtree(self.outputdir)
        shutil.rmtree(self.inputdir)

    def test_create_temp_image(self):
        im = self.create_temp_image("foobar", (100, 10), "blue")
        self.assertTrue(os.path.exists(im))

    def test_recorder_creates_dir(self):
        shutil.rmtree(self.outputdir)
        self.make_metadata("""<screenshots></screenshots>""")
        self.recorder.record()

        self.assertTrue(os.path.exists(self.outputdir))

    def test_single_input(self):
        self.create_temp_image("foobar.png", (10, 10), "blue")
        self.make_metadata("""<screenshots>
<screenshot>
   <name>foobar</name>
   <tile_width>1</tile_width>
   <tile_height>1</tile_height>
</screenshot>
</screenshots>""")

        self.recorder.record()
        self.assertTrue(exists(join(self.outputdir, "foobar.png")))

    def test_two_files(self):
        self.create_temp_image("foo.png", (10, 10), "blue")
        self.create_temp_image("bar.png", (10, 10), "red")
        self.make_metadata("""<screenshots>
<screenshot>
   <name>foo</name>
   <tile_width>1</tile_width>
   <tile_height>1</tile_height>
</screenshot>
<screenshot>
   <name>bar</name>
   <tile_width>1</tile_width>
   <tile_height>1</tile_height>
</screenshot>
</screenshots>""")

        self.recorder.record()
        self.assertTrue(exists(join(self.outputdir, "foo.png")))
        self.assertTrue(exists(join(self.outputdir, "bar.png")))

    def test_one_col_tiles(self):
        self.create_temp_image("foobar.png", (10, 10), "blue")
        self.create_temp_image("foobar_0_1.png", (10, 10), "red")

        self.make_metadata("""<screenshots>
<screenshot>
   <name>foobar</name>
    <tile_width>1</tile_width>
    <tile_height>2</tile_height>
</screenshot>
</screenshots>""")

        self.recorder.record()

        with Image.open(join(self.outputdir, "foobar.png")) as im:
            (w, h) = im.size

            self.assertEqual(10, w)
            self.assertEqual(20, h)

            self.assertEqual((0, 0, 255, 255), im.getpixel((1, 1)))
            self.assertEqual((255, 0, 0, 255), im.getpixel((1, 11)))

    def test_one_row_tiles(self):
        self.create_temp_image("foobar.png", (10, 10), "blue")
        self.create_temp_image("foobar_1_0.png", (10, 10), "red")

        self.make_metadata("""<screenshots>
<screenshot>
   <name>foobar</name>
    <tile_width>2</tile_width>
    <tile_height>1</tile_height>
</screenshot>
</screenshots>""")

        self.recorder.record()

        with Image.open(join(self.outputdir, "foobar.png")) as im:
            (w, h) = im.size
            self.assertEqual(20, w)
            self.assertEqual(10, h)

            self.assertEqual((0, 0, 255, 255), im.getpixel((1, 1)))
            self.assertEqual((255, 0, 0, 255), im.getpixel((11, 1)))

    def test_fractional_tiles(self):
        self.create_temp_image("foobar.png", (10, 10), "blue")
        self.create_temp_image("foobar_1_0.png", (9, 10), "red")
        self.create_temp_image("foobar_0_1.png", (10, 8), "red")
        self.create_temp_image("foobar_1_1.png", (9, 8), "blue")

        self.make_metadata("""<screenshots>
<screenshot>
   <name>foobar</name>
    <tile_width>2</tile_width>
    <tile_height>2</tile_height>
</screenshot>
</screenshots>""")

        self.recorder.record()

        with Image.open(join(self.outputdir, "foobar.png")) as im:
            (w, h) = im.size
            self.assertEqual(19, w)
            self.assertEqual(18, h)

            self.assertEqual((0, 0, 255, 255), im.getpixel((1, 1)))
            self.assertEqual((255, 0, 0, 255), im.getpixel((11, 1)))

            self.assertEqual((0, 0, 255, 255), im.getpixel((11, 11)))
            self.assertEqual((255, 0, 0, 255), im.getpixel((1, 11)))

    def test_verify_success(self):
        self.create_temp_image("foobar.png", (10, 10), "blue")
        self.make_metadata("""<screenshots>
<screenshot>
   <name>foobar</name>
    <tile_width>1</tile_width>
    <tile_height>1</tile_height>
</screenshot>
</screenshots>""")

        self.recorder.record()
        self.recorder.verify()

    def test_verify_failure(self):
        self.create_temp_image("foobar.png", (10, 10), "blue")
        self.make_metadata("""<screenshots>
<screenshot>
   <name>foobar</name>
    <tile_width>1</tile_width>
    <tile_height>1</tile_height>
</screenshot>
</screenshots>""")

        self.recorder.record()
        os.unlink(join(self.inputdir, "foobar.png"))
        self.create_temp_image("foobar.png", (11, 11), "green")

        try:
            self.recorder.verify()
            self.fail("expected exception")
        except VerifyError:
            pass  # expected

        self.assertTrue(os.path.exists(join(self.failureDir, "foobar_actual.png")))
        self.assertTrue(os.path.exists(join(self.failureDir, "foobar_expected.png")))
        self.assertTrue(os.path.exists(join(self.failureDir, "foobar_diff.png")))

        # check colored diff
        with Image.open(join(self.failureDir, "foobar_diff.png")) as im:
            (w, h) = im.size
            self.assertEqual(11, w)
            self.assertEqual(11, h)

            self.assertEqual((255, 0, 0, 255), im.getpixel((0, 1)))
            self.assertEqual((255, 0, 0, 255), im.getpixel((10, 1)))
            
            self.assertEqual((0, 128, 0, 255), im.getpixel((1, 1)))
            self.assertEqual((0, 128, 0, 255), im.getpixel((9, 1)))

if __name__ == '__main__':
    unittest.main()

#!/usr/bin/env python
#
# Copyright (c) 2014-present, Facebook, Inc.
# All rights reserved.
#
# This source code is licensed under the BSD-style license found in the
# LICENSE file in the root directory of this source tree. An additional grant
# of patent rights can be found in the PATENTS file in the same directory.
#

import tempfile
import unittest
import shutil
import os

from PIL import Image

class TestRecorder(unittest.TestCase):
    def setUp(self):
        self.outputdir = tempfile.mkdtemp()
        self.inputdir = tempfile.mkdtemp()
        self.tmpimages = []

    def create_temp_image(self, dimens, color):
        im = Image.new("RGBA", dimens, color)
        file = tempfile.NamedTemporaryFile(suffix=".png", dir=self.inputdir)
        self.tmpimages.append(file)
        im.save(file.name, "PNG")
        return file

    def tearDown(self):
        for f in self.tmpimages:
            f.close()

        shutil.rmtree(self.outputdir)
        shutil.rmtree(self.inputdir)

    def test_create_temp_image(self):
        im = self.create_temp_image((100, 10), "blue")
        self.assertTrue(os.path.exists(im.name))

if __name__ == '__main__':
    unittest.main()

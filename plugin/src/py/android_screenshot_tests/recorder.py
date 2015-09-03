#!/usr/bin/env python
#
# Copyright (c) 2014-present, Facebook, Inc.
# All rights reserved.
#
# This source code is licensed under the BSD-style license found in the
# LICENSE file in the root directory of this source tree. An additional grant
# of patent rights can be found in the PATENTS file in the same directory.
#

import xml.etree.ElementTree as ET
import os

from os.path import join
from PIL import Image, ImageChops
from . import common
import shutil
import tempfile

class VerifyError(StandardError):
    pass

class Recorder:
    def __init__(self, input, output):
        self._input = input
        self._output = output
        self._realoutput = output

    def _copy(self, name, w, h):
        tilewidth, tileheight = Image.open(join(self._input,
                                                common.get_image_file_name(name, 0, 0))).size

        canvaswidth = 0

        for i  in range(w):
            input_file = common.get_image_file_name(name, i, 0)
            canvaswidth += Image.open(join(self._input, input_file)).size[0]


        canvasheight = 0

        for j in range(h):
            input_file = common.get_image_file_name(name, 0, j)
            canvasheight += Image.open(join(self._input, input_file)).size[1]

        im = Image.new("RGBA", (canvaswidth, canvasheight))

        for i in range(w):
            for j in range(h):
                input_file = common.get_image_file_name(name, i, j)
                input_image = Image.open(join(self._input, input_file))

                im.paste(input_image, (i * tilewidth, j * tileheight))

        im.save(join(self._output, name + ".png"))

    def _get_metadata_root(self):
        return ET.parse(join(self._input, "metadata.xml")).getroot()

    def _record(self):
        root = self._get_metadata_root()
        for screenshot in root.iter("screenshot"):
            self._copy(screenshot.find('name').text,
                       int(screenshot.find('tile_width').text),
                       int(screenshot.find('tile_height').text))

    def _clean(self):
        if os.path.exists(self._output):
            shutil.rmtree(self._output)
        os.mkdir(self._output)

    def _is_image_same(self, file1, file2):
        im1 = Image.open(file1)
        im2 = Image.open(file2)

        return ImageChops.difference(im1, im2).getbbox() is None

    def record(self):
        self._clean()
        self._record()

    def verify(self):
        self._output = tempfile.mkdtemp()
        self._record()

        root = self._get_metadata_root()
        for screenshot in root.iter("screenshot"):
            name = screenshot.find('name').text + ".png"
            actual = join(self._output, name)
            expected = join(self._realoutput, name)
            if not self._is_image_same(expected,
                                       actual):
                raise VerifyError("Image %s is not same as %s" % (actual, expected))

        shutil.rmtree(self._output)

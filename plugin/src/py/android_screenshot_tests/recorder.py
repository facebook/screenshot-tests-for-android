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

import json
import os
import shutil
import sys
import tempfile
import xml.etree.ElementTree as ET
from os.path import join

from PIL import Image, ImageChops, ImageDraw

from . import common


class VerifyError(Exception):
    pass


class Recorder:
    def __init__(self, input, output, failure_output):
        self._input = input
        self._output = output
        self._realoutput = output
        self._failure_output = failure_output

    def _get_image_size(self, file_name):
        with Image.open(file_name) as im:
            return im.size

    def _copy(self, name, w, h):
        tilewidth, tileheight = self._get_image_size(
            join(self._input, common.get_image_file_name(name, 0, 0))
        )

        canvaswidth = 0

        for i in range(w):
            input_file = common.get_image_file_name(name, i, 0)
            canvaswidth += self._get_image_size(join(self._input, input_file))[0]

        canvasheight = 0

        for j in range(h):
            input_file = common.get_image_file_name(name, 0, j)
            canvasheight += self._get_image_size(join(self._input, input_file))[1]

        im = Image.new("RGBA", (canvaswidth, canvasheight))

        for i in range(w):
            for j in range(h):
                input_file = common.get_image_file_name(name, i, j)
                with Image.open(join(self._input, input_file)) as input_image:
                    im.paste(input_image, (i * tilewidth, j * tileheight))
                    input_image.close()

        im.save(join(self._output, name + ".png"))
        im.close()

    def _get_metadata_json(self):
        file = open(join(self._input, "metadata.json"), "r")
        return json.load(file)

    def _record(self):
        metadata = self._get_metadata_json()
        for screenshot in metadata:
            self._copy(
                screenshot["name"],
                int(screenshot["tileWidth"]),
                int(screenshot["tileHeight"]),
            )

    def _clean(self):
        if os.path.exists(self._output):
            shutil.rmtree(self._output)
        os.makedirs(self._output)

    def _draw_diff_outline(self, failure_file, diff, image_to_draw_on):
        draw = ImageDraw.Draw(image_to_draw_on)
        draw.rectangle(list(diff), outline=(255, 0, 0))
        image_to_draw_on.save(failure_file)

    def _is_image_same(self, file1, file2, failure_file):
        with Image.open(file1) as im1, Image.open(file2) as im2:
            diff_image = ImageChops.difference(im1, im2)
            try:
                diff = diff_image.getbbox()
                if diff is None:
                    if (im1.size != im2.size) or (im1.getbbox() != im2.getbbox()):
                        # First image is longer than the second
                        if im1.getbbox()[3] > im2.getbbox()[3]:
                            diff = [(0, im2.getbbox()[3]), (im1.getbbox()[2]-1, im1.getbbox()[3] - 1)]
                            self._draw_diff_outline(failure_file, diff, im1)

                        # First image is shorter than the first
                        if im1.getbbox()[3] < im2.getbbox()[3]:
                            diff = [(0, im1.getbbox()[3]), (im2.getbbox()[2]-1, im2.getbbox()[3] - 1)]
                            self._draw_diff_outline(failure_file, diff, im2)

                        # First image is wider than the second
                        if im1.getbbox()[2] > im2.getbbox()[2]:
                            diff = [(im2.getbbox()[2], 0), (im1.getbbox()[2]-1, im1.getbbox()[3] - 1)]
                            self._draw_diff_outline(failure_file, diff, im1)

                        # First image is narrower than the first
                        if im1.getbbox()[2] < im2.getbbox()[2]:
                            diff = [(im1.getbbox()[2], 0), (im2.getbbox()[2]-1, im2.getbbox()[3] - 1)]
                            self._draw_diff_outline(failure_file, diff, im2)

                        return False
                    return True
                else:
                    if failure_file:
                        diff_list = list(diff) if diff else []
                        draw = ImageDraw.Draw(im2)
                        draw.rectangle(diff_list, outline=(255, 0, 0))
                        im2.save(failure_file)
                    return False
            finally:
                diff_image.close()

    def record(self):
        self._clean()
        self._record()

    def verify(self):
        self._output = tempfile.mkdtemp()
        self._record()

        screenshots = self._get_metadata_json()
        failures = []
        for screenshot in screenshots:
            name = screenshot["name"] + ".png"
            actual = join(self._output, name)
            expected = join(self._realoutput, name)
            if self._failure_output:
                diff_name = screenshot["name"] + "_diff.png"
                diff = join(self._failure_output, diff_name)

                if not self._is_image_same(expected, actual, diff):
                    expected_name = screenshot["name"] + "_expected.png"
                    actual_name = screenshot["name"] + "_actual.png"

                    shutil.copy(actual, join(self._failure_output, actual_name))
                    shutil.copy(expected, join(self._failure_output, expected_name))

                    failures.append((expected, actual))
            else:
                if not self._is_image_same(expected, actual, None):
                    raise VerifyError("Image %s is not same as %s" % (expected, actual))

        if failures:
            reason = ""
            for expected, actual in failures:
                reason = reason + "\nImage %s is not same as %s" % (expected, actual)
            raise VerifyError(reason)

        shutil.rmtree(self._output)

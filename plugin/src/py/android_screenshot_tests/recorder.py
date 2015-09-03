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

from os.path import join
import shutil

class Recorder:
    def __init__(self, input, output):
        self._input = input
        self._output = output

    def record(self):
        root = ET.parse(join(self._input, "metadata.xml")).getroot()

        for screenshot in root.iter("screenshot"):
            name = screenshot.find('name').text
            name += ".png"
            shutil.copyfile(join(self._input, name),
                            join(self._output, name))

    def verify(self):
        pass

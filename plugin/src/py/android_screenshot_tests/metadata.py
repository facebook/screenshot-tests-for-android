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
import tempfile
import shutil
import os
import xml.etree.ElementTree as ET
import re

# Given a metadata file locally, this transforms it (in-place), to
# remove any screenshot elements that don't satisfy the given filter
# criteria
def filter_screenshots(metadata_file, name_regex=None):
    parsed = ET.parse(metadata_file)
    root = parsed.getroot()
    to_remove = []
    for s in root.iter('screenshot'):
        if name_regex and not (re.search(name_regex, s.find('name').text)):
            to_remove.append(s)

    for s in to_remove:
        root.remove(s)

    parsed.write(metadata_file)

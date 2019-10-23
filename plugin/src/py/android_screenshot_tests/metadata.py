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

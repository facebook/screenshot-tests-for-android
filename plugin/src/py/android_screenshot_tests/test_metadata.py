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
from . import metadata

# Tests for the metadata package
class TestMetadata(unittest.TestCase):
    def setUp(self):
        fd, self.tmp_metadata = tempfile.mkstemp(prefix="TempMetadataXml")
        os.close(fd)
        os.unlink(self.tmp_metadata)

        self.fixture_metadata = os.path.join(os.path.dirname(__file__), 'metadata_fixture.xml')
        shutil.copyfile(self.fixture_metadata, self.tmp_metadata)

    def tearDown(self):
        if os.path.exists(self.tmp_metadata):
            os.unlink(self.tmp_metadata)

    def test_nothing_removed_for_empty_filter(self):
        metadata.filter_screenshots(self.tmp_metadata)

        self.assertEqual(
            self.get_num_screenshots_in(self.fixture_metadata),
            self.get_num_screenshots_in(self.tmp_metadata))

    def test_exactly_one_result(self):
        metadata.filter_screenshots(self.tmp_metadata,
                                    name_regex="testAddPlaceIsShowing")

        self.assertEqual(1, self.get_num_screenshots_in(self.tmp_metadata))

    def test_regex(self):
        metadata.filter_screenshots(self.tmp_metadata,
                                    name_regex=".*testAddPlaceIsShowing.*")

        self.assertEqual(1, self.get_num_screenshots_in(self.tmp_metadata))

    def test_regex(self):
        metadata.filter_screenshots(self.tmp_metadata,
                                    name_regex=".*CheckinTitleBar.*")

        self.assertEqual(7, self.get_num_screenshots_in(self.tmp_metadata))

    def get_num_screenshots_in(self, metadata_file):
        """Gets the number of screenshots in the given metadata file"""
        return len(list(ET.parse(metadata_file).getroot().iter('screenshot')))

if __name__ == '__main__':
    unittest.main()

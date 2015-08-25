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

        self.assertEquals(
            self.get_num_screenshots_in(self.fixture_metadata),
            self.get_num_screenshots_in(self.tmp_metadata))

    def test_exactly_one_result(self):
        metadata.filter_screenshots(self.tmp_metadata,
                                    name_regex="testAddPlaceIsShowing")

        self.assertEquals(1, self.get_num_screenshots_in(self.tmp_metadata))

    def test_regex(self):
        metadata.filter_screenshots(self.tmp_metadata,
                                    name_regex=".*testAddPlaceIsShowing.*")

        self.assertEquals(1, self.get_num_screenshots_in(self.tmp_metadata))

    def test_regex(self):
        metadata.filter_screenshots(self.tmp_metadata,
                                    name_regex=".*CheckinTitleBar.*")

        self.assertEquals(7, self.get_num_screenshots_in(self.tmp_metadata))

    def get_num_screenshots_in(self, metadata_file):
        """Gets the number of screenshots in the given metadata file"""
        return len(list(ET.parse(metadata_file).getroot().iter('screenshot')))

if __name__ == '__main__':
    unittest.main()

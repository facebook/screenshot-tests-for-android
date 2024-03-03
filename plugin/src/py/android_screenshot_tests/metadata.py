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

import json
import os
import re
import shutil
import tempfile
import unittest


# Given a metadata file locally, this transforms it (in-place), to
# remove any screenshot elements that don't satisfy the given filter
# criteria
def filter_screenshots(metadata_file, name_regex=None):
    with open(metadata_file, "r") as f:
        parsed = json.load(f)
        to_remove = []
        for s in parsed:
            if name_regex and not (re.search(name_regex, s["name"])):
                to_remove.append(s)

        for s in to_remove:
            parsed.remove(s)

    with open(metadata_file, "w") as f:
        f.write(json.dumps(parsed))

#!/usr/bin/env python
#
# Copyright (c) 2014-present, Facebook, Inc.
# All rights reserved.
#
# This source code is licensed under the BSD-style license found in the
# LICENSE file in the root directory of this source tree. An additional grant
# of patent rights can be found in the PATENTS file in the same directory.
#

def get_image_file_name(name, x, y):
    image_file = name
    if x != 0 or y != 0:
        image_file += "_%d_%d" % (x, y)

    image_file += ".png"
    return image_file

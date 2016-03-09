from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import os
import subprocess
import tempfile
from os.path import exists, join

from . import common

def _check_output(args, **kwargs):
    with tempfile.TemporaryFile() as f:
        kwargs['stderr'] = f
        return common.check_output(args, **kwargs)

def parse_package_line(line):
    """The line looks like this:
    package: name='com.facebook.testing.tests' versionCode='1' versionName=''"""

    for word in line.split():
        if word.startswith("name='"):
            return word[len("name='"):-1]

def get_aapt_bin():
    """Find the binary for aapt from $ANDROID_SDK"""
    android_sdk = common.get_android_sdk()

    build_tools = os.path.join(android_sdk, 'build-tools')

    versions = os.listdir(build_tools)
    versions = sorted(versions, key=lambda x: "0000000" + x if x.startswith("android") else x, reverse=True)

    for v in versions:
        aapt = join(build_tools, v, "aapt")
        if exists(aapt) or exists(aapt + ".exe"):
            return aapt

    raise RuntimeError("Could not find build-tools in " + android_sdk)

def get_package(apk):
    output = _check_output([get_aapt_bin(), 'dump', 'badging', apk], stderr=os.devnull)
    for line in output.split('\n'):
        if line.startswith('package:'):
            return parse_package_line(line)

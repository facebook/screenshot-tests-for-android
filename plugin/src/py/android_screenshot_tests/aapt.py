from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import os
import subprocess
from os.path import exists, join

def _check_output(args, **kwargs):
    with open(os.devnull) as f:
        kwargs['stderr'] = f
        return subprocess.check_output(args, **kwargs)

def parse_package_line(line):
    """The line looks like this:
    package: name='com.facebook.testing.tests' versionCode='1' versionName=''"""

    for word in line.split():
        if word.startswith("name='"):
            return word[len("name='"):-1]

def get_aapt_bin():
    """Find the binary for aapt from $ANDROID_SDK"""
    android_sdk = os.environ.get('ANDROID_SDK') or os.environ.get('ANDROID_HOME')

    if not android_sdk:
        raise RuntimeError("ANDROID_SDK or ANDROID_HOME needs to be set")

    build_tools = os.path.join(android_sdk, 'build-tools')

    versions = os.listdir(build_tools)
    versions = sorted(versions, key=lambda x: "0000000" + x if x.startswith("android") else x, reverse=True)

    for v in versions:
        aapt = join(build_tools, v, "aapt")
        if exists(aapt):
            return aapt

    raise RuntimeError("Could not find build-tools in " + android_sdk)

def get_package(apk):
    output = _check_output([get_aapt_bin(), 'dump', 'badging', apk], stderr=os.devnull)
    for line in output.split('\n'):
        if line.startswith('package:'):
            return parse_package_line(line)

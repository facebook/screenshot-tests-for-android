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

import subprocess
import tarfile
import tempfile

from . import common
from .common import get_adb


class SimplePuller:
    """Pulls a given file from the device"""

    def __init__(self, adb_args=[]):
        self._adb_args = list(adb_args)

    def remote_file_exists(self, src):
        output = common.check_output(
            [get_adb()]
            + self._adb_args
            + ["shell", "ls %s && echo EXISTS || echo DOES_NOT_EXIST" % src]
        )
        return "EXISTS" in output

    def pull(self, src, dest):
        subprocess.check_call(
            [get_adb()] + self._adb_args + ["pull", src, dest], stderr=subprocess.STDOUT
        )

    @staticmethod
    def _get_tar_name(src):
        return "{}.tar.gz".format(src)

    def _tar(self, src):
        subprocess.check_call(
            [get_adb()]
            + self._adb_args
            + [
                "shell",
                "tar",
                "-zcvf",
                SimplePuller._get_tar_name(src),
                "-C",
                src,
                ".",
            ],
            stderr=subprocess.STDOUT,
        )

    def _remove_temp_tar(self, src):
        subprocess.check_call(
            [get_adb()]
            + self._adb_args
            + ["shell", "rm", SimplePuller._get_tar_name(src)],
            stderr=subprocess.STDOUT,
        )

    def pull_folder(self, src, dest):
        # Pulling a folder with lots of files is very slow, as each file transmission needs
        # to reestablish the connection, slowing down the overall throughput.
        # Hence taring the entire folder first.
        self._tar(src)
        with tempfile.NamedTemporaryFile() as f:
            self.pull(SimplePuller._get_tar_name(src), f.name)
            local_file = tarfile.open(f.name)
            local_file.extractall(dest)
            local_file.close()
        self._remove_temp_tar(src)

    def get_external_data_dir(self):
        output = common.check_output(
            [get_adb()] + self._adb_args + ["shell", "echo", "$EXTERNAL_STORAGE"]
        )
        return output.strip().split()[-1]

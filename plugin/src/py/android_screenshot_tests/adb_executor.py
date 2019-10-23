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

from . import common


class AdbExecutor:
    def __init__(self):
        pass

    def execute(self, command):
        result = common.check_output([common.get_adb()] + command)
        if result is None:
            raise RuntimeError("ERROR: you shouldn't see this in normal operation,"
                               "file a bug report please.\n\n "
                               "Trying to execute adb " + ' '.join(command))
        return result

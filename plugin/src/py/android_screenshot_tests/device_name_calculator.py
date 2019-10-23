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

import re
import subprocess

from .adb_executor import AdbExecutor


class DeviceNameCalculator:

    def __init__(self, executor=AdbExecutor()):
        self.executor = executor

    def name(self):
        api_version_text = self._api_version_text()
        play_services_text = self._play_services_text()
        screen_density_text = self._screen_density_text()
        screen_size_text = self._screen_size_text()
        architecture_text = self._architecture_text()
        locale = self._locale()

        device_parameters = [api_version_text, play_services_text,
                             screen_density_text, screen_size_text,
                             architecture_text, locale]

        if None in device_parameters:
            raise RuntimeError("ERROR: you shouldn't see this in normal operation,"
                               "file a bug report please.\n\n "
                               "One or more device params are None")

        return "{0}_{1}_{2}_{3}_{4}_{5}".format(api_version_text,
                                                play_services_text,
                                                screen_density_text,
                                                screen_size_text,
                                                architecture_text,
                                                locale)

    def _screen_density_text(self):
        density = int(self._screen_density())

        if density in range(0, 121):
            return 'LDPI'
        elif density in range(121, 161):
            return 'MDPI'
        elif density in range(161, 241):
            return 'HDPI'
        elif density in range(241, 321):
            return 'XHDPI'
        elif density in range(321, 481):
            return 'XXHDPI'

        return 'XXXHDPI'

    def _screen_density(self):
        result = self.executor.execute(['shell', 'wm', 'density'])
        density = re.search('[0-9]+', result)
        if density:
            return density.group(0)

    def _screen_size_text(self):
        result = self.executor.execute(['shell', 'wm', 'size'])
        density = re.search('[0-9]+x[0-9]+', result)
        if density:
            return density.group(0)

    def _has_play_services(self):
        try:
            output = self.executor.execute(['shell', 'pm', 'path', 'com.google.android.gms'])
            return True if output else False
        except subprocess.CalledProcessError:
            return False

    def _play_services_text(self):
        play_services = self._has_play_services()
        return 'GP' if play_services else 'NO_GP'

    def _api_version(self):
        return self.executor.execute(['shell', 'getprop', 'ro.build.version.sdk'])

    def _api_version_text(self):
        return 'API_{0}'.format(int(self._api_version()))

    def _architecture_text(self):
        architecture = self.executor.execute(['shell', 'getprop', 'ro.product.cpu.abi'])
        return architecture.rstrip()

    def _locale(self):
        persist_locale = self.executor.execute(['shell', 'getprop', 'persist.sys.locale'])
        product_locale = self.executor.execute(['shell', 'getprop', 'ro.product.locale'])
        return persist_locale.rstrip() if persist_locale else product_locale.rstrip()

from . import common
import re

class DeviceNameCalculator:
    def __init__(self):
        pass

    def name(self):
        api_version_text = self._api_version_text()
        play_services_text = self._play_services_text()
        screen_density_text = self._screen_density_text()
        screen_size_text = self._screen_size_text()
        architecture_text = self._architecture_text()

        device_parameters = [api_version_text, play_services_text,
                             screen_density_text, screen_size_text,
                             architecture_text]

        if None in device_parameters:
            raise RuntimeError("ERROR: you shouldn't see this in normal operation,"
                               "file a bug report please.\n\n "
                               "One or more device params are None")

        return "{0}_{1}_{2}_{3}_{4}".format(api_version_text,
                                            play_services_text,
                                            screen_density_text,
                                            screen_size_text,
                                            architecture_text)

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
        result = self._execute_adb_command(['shell', 'wm', 'density'])
        density = re.search('[0-9]+', result)
        if density:
            return density.group(0)

    def _screen_size_text(self):
        result = self._execute_adb_command(['shell', 'wm', 'size'])
        density = re.search('[0-9]+x[0-9]+', result)
        if density:
            return density.group(0)

    def _has_play_services(self):
        output = self._execute_adb_command(['shell', 'pm', 'path', 'com.google.android.gms'])
        return True if output else False

    def _play_services_text(self):
        play_services = self._has_play_services()
        return 'GP' if play_services else 'NO_GP'

    def _api_version(self):
        return self._execute_adb_command(['shell', 'getprop', 'ro.build.version.sdk'])

    def _api_version_text(self):
        return 'API_{0}'.format(int(self._api_version()))

    def _architecture_text(self):
        architecture = self._execute_adb_command(['shell', 'getprop', 'ro.product.cpu.abi'])
        return architecture.rstrip()

    def _execute_adb_command(self, command):
        result = common.check_output([common.get_adb()] + command)
        if result is None:
            raise RuntimeError("ERROR: you shouldn't see this in normal operation,"
                               "file a bug report please.\n\n "
                               "Trying to execute adb " + ' '.join(command))
        return result

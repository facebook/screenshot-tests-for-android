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

        return "{0}_{1}_{2}_{3}".format(api_version_text, play_services_text,
                                        screen_density_text, screen_size_text)

    def _screen_density_text(self):
        density = self._screen_density()

        try:
            density = int(density)
        except:
            return 'UNKNOWN'

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
        else:
            return 'XXXHDPI'

    def _screen_density(self):
        result = common.check_output([common.get_adb()] + ['shell', 'wm', 'density'])
        density = re.search('[0-9]+', result)
        if density:
            return density.group(0)

    def _screen_size(self):
        result = common.check_output([common.get_adb()] + ['shell', 'wm', 'size'])
        density = re.search('[0-9]+x[0-9]+', result)
        if density:
            return density.group(0)

    def _screen_size_text(self):
        return self._screen_size()

    def _has_play_services(self):
        output = common.check_output(
            [common.get_adb()] + ['shell', 'pm', 'dump', 'com.google.android.gms', '|', 'grep',
                                  'version'])
        return True if output else False

    def _api_version(self):
        return common.check_output(
            [common.get_adb()] + ['shell', 'getprop', 'ro.build.version.sdk'])

    def _api_version_text(self):
        api_version = ''.join(self._api_version().splitlines())
        return 'API_' + api_version

    def _play_services_text(self):
        play_services = self._has_play_services()
        return 'GP' if play_services else 'NO_GP'

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

def get_aapt_bin():
    """Find the binary for aapt from $ANDROID_SDK"""
    android_sdk = os.environ.get('ANDROID_SDK') or os.environ.get('ANDROID_HOME')
    build_tools = os.path.join(android_sdk, 'build-tools')

    all = list(glob.glob(os.path.join(build_tools, '*/aapt')))
    bad = list(glob.glob(os.path.join(build_tools, 'android-*/aapt')))
    good = list(Set(all) - Set(bad))

    good.sort()
    bad.sort()

    if len(good) == 0:
        return bad[-1]

    return good[-1]

def get_package(apk):
    output = _check_output([get_aapt_bin(), 'dump', 'badging', apk], stderr=os.devnull)
    for line in output.split('\n'):
        if line.startswith('package:'):
            return parse_package_line(line)

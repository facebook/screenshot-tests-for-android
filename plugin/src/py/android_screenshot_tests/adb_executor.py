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

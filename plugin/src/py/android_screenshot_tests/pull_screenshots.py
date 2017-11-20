#!/usr/bin/env python
#
# Copyright (c) 2014-present, Facebook, Inc.
# All rights reserved.
#
# This source code is licensed under the BSD-style license found in the
# LICENSE file in the root directory of this source tree. An additional grant
# of patent rights can be found in the PATENTS file in the same directory.
#

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals
import os
import sys
import tempfile
import subprocess
import xml.etree.ElementTree as ET
import getopt
import shutil
import codecs
import json
from . import metadata
from .simple_puller import SimplePuller
import zipfile
from . import aapt
from . import common

from os.path import join
from os.path import abspath

try:
    from Queue import Queue
except ImportError:
    from queue import Queue


OLD_ROOT_SCREENSHOT_DIR = '/data/data/'
KEY_CLASS = 'class'
KEY_LEFT = 'left'
KEY_TOP = 'top'
KEY_WIDTH = 'width'
KEY_HEIGHT = 'height'

def usage():
    print("usage: ./scripts/screenshot_tests/pull_screenshots com.facebook.apk.name.tests [--generate-png]", file=sys.stderr)
    return

def sort_screenshots(screenshots):
    def sort_key(screenshot):
        group = screenshot.find('group')

        group = group.text if group is not None else ""

        return (group, screenshot.find('name').text)

    return sorted(list(screenshots), key=sort_key)


def generate_html(dir):
    root = ET.parse(join(dir, 'metadata.xml')).getroot()
    alternate = False
    index_html = abspath(join(dir, "index.html"))
    with codecs.open(index_html, mode="w", encoding="utf-8") as html:
        html.write('<!DOCTYPE html>')
        html.write('<html>')
        html.write('<head>')
        html.write('<title>Screenshot Test Results</title>')
        html.write('<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>')
        html.write('<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.3/jquery-ui.min.js"></script>')
        html.write('<script src="default.js"></script>')
        html.write('<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.3/themes/smoothness/jquery-ui.css" />')
        html.write('<link rel="stylesheet" href="default.css"></head>')
        html.write('<body>')

        screenshot_num = 0
        for screenshot in sort_screenshots(root.iter('screenshot')):
            screenshot_num += 1
            alternate = not alternate
            canonical_name = screenshot.find('name').text
            package = ""
            name = canonical_name
            if '.' in canonical_name:
                last_seperator = canonical_name.rindex('.') + 1
                package = canonical_name[:last_seperator]
                name = canonical_name[last_seperator:]

            html.write('<div class="screenshot %s">' % ('alternate' if alternate else ''))
            html.write('<div class="screenshot_name">')
            html.write('<span class="demphasize">%s</span>%s' % (package, name))
            html.write('</div>')

            group = screenshot.find('group')
            if group:
                html.write('<div class="screenshot_group">%s</div>' % group.text)

            extras = screenshot.find('extras')
            if extras is not None:
                str = ""
                for node in extras:
                    if node.text is not None:
                        str = str + "*****" + node.tag + "*****\n\n" + node.text + "\n\n\n"
                if str != "":
                    extra_html = '<button class="extra" data="%s">Extra info</button>' % str
                    html.write(extra_html.encode('utf-8').strip())

            description = screenshot.find('description')
            if description is not None:
                html.write('<div class="screenshot_description">%s</div>' % description.text)

            error = screenshot.find('error')
            if error is not None:
                html.write('<div class="screenshot_error">%s</div>' % error.text)
            else:
                hierarchy = get_view_hierarchy(dir, screenshot)
                html.write('<div class="flex-wrapper">')
                write_image(hierarchy, dir, html, screenshot, screenshot_num)
                html.write('<div class="command-wrapper">')
                write_commands(html)
                write_view_hierarchy(hierarchy, html, screenshot_num)
                html.write('</div>')
                html.write('</div>')

            html.write('</div>')
            html.write('<div class="clearfix"></div>')
            html.write('<hr/>')

        html.write('</body></html>')
        return index_html


def write_commands(html):
    html.write('<button class="toggle_dark">Toggle Dark Background</button>')
    html.write('<button class="toggle_hierarchy">Toggle View Hierarchy Overlay</button>')


def write_view_hierarchy(hierarchy, html, parent_id):
    if not hierarchy:
        return

    html.write('<h3>View Hierarchy</h3>')
    html.write('<div class="view-hierarchy">')
    write_view_hierarchy_tree_node(hierarchy, html, parent_id)
    html.write('</div>')


def write_view_hierarchy_tree_node(node, html, parent_id):
    html.write('<details target="#%s-%s">' % (parent_id, get_view_hierarchy_overlay_node_id(node)))
    html.write('<summary>%s</summary>' % node['class'])
    html.write('<ul>')
    for item in sorted(node):
        if item == 'children' or item == 'class':
            continue
        html.write('<li><strong>%s:</strong> %s</li>' % (item, node[item]))

    html.write('</ul>')
    if 'children' in node:
        for child in node['children']:
            write_view_hierarchy_tree_node(child, html, parent_id)

    html.write('</details>')


def write_view_hierarchy_overlay_nodes(hierarchy, html, parent_id):
    if not hierarchy:
        return

    to_output = Queue()
    to_output.put(hierarchy)
    while not to_output.empty():
        node = to_output.get()
        left = node[KEY_LEFT]
        top = node[KEY_TOP]
        width = node[KEY_WIDTH] - 4
        height = node[KEY_HEIGHT] - 4
        id = get_view_hierarchy_overlay_node_id(node)
        node_html = """
        <div
          class="hierarchy-node"
          style="left:%dpx;top:%dpx;width:%dpx;height:%dpx;"
          id="%s-%s"></div>
        """
        html.write(node_html % (left, top, width, height, parent_id, id))

        if 'children' in node:
            for child in node['children']:
                to_output.put(child)


def get_view_hierarchy_overlay_node_id(node):
    cls = node[KEY_CLASS]
    x = node[KEY_LEFT]
    y = node[KEY_TOP]
    width = node[KEY_WIDTH]
    height = node[KEY_HEIGHT]
    return "node-%s-%d-%d-%d-%d" % (cls.replace(".", "-"), x, y, width, height)


def get_view_hierarchy(dir, screenshot):
    json_path = join(dir, screenshot.find('name').text + "_dump.json")
    if not os.path.exists(json_path):
        return None
    with codecs.open(json_path, mode="r", encoding='utf-8') as dump:
        return json.loads(dump.read())


def write_image(hierarchy, dir, html, screenshot, parent_id):
    html.write('<div class="img-wrapper">')
    html.write('<table>')
    for y in range(int(screenshot.find('tile_height').text)):
        html.write('<tr>')
        for x in range(int(screenshot.find('tile_width').text)):
            html.write('<td>')
            image_file = "./" + common.get_image_file_name(screenshot.find('name').text, x, y)

            if os.path.exists(join(dir, image_file)):
                html.write('<img src="%s" />' % image_file)

            html.write('</td>')
        html.write('</tr>')
    html.write('</table>')
    html.write('<div class="hierarchy-overlay">')
    write_view_hierarchy_overlay_nodes(hierarchy, html, parent_id)
    html.write('</div></div>')


def test_for_wkhtmltoimage():
    if subprocess.call(['which', 'wkhtmltoimage']) != 0:
        raise RuntimeError("""Could not find wkhtmltoimage in your path, we need this for generating pngs
Download an appropriate version from:
    http://wkhtmltopdf.org/downloads.html""")


def generate_png(path_to_html, path_to_png):
    test_for_wkhtmltoimage()
    subprocess.check_call(['wkhtmltoimage', path_to_html, path_to_png], stdout=sys.stdout)


def copy_assets(destination):
    """Copy static assets required for rendering the HTML"""
    _copy_asset("default.css", destination)
    _copy_asset("default.js", destination)
    _copy_asset("background.png", destination)
    _copy_asset("background_dark.png", destination)


def _copy_asset(filename, destination):
    thisdir = os.path.dirname(__file__)
    _copy_file(abspath(join(thisdir, filename)), join(destination, filename))


def _copy_file(src, dest):
    if os.path.exists(src):
        shutil.copyfile(src, dest)
    else:
        _copy_via_zip(src, None, dest)


def _copy_via_zip(src_zip, zip_path, dest):
    if os.path.exists(src_zip):
        zip = zipfile.ZipFile(src_zip)
        input = zip.open(zip_path, 'r')
        with open(dest, 'wb') as output:
            output.write(input.read())
    else:
        # walk up the tree
        head, tail = os.path.split(src_zip)

        _copy_via_zip(head, tail if not zip_path else (tail + "/" + zip_path), dest)


def _android_path_join_two(a, b):
    if b.startswith("/"):
        return b

    if not a.endswith("/"):
        a += "/"

    return a + b


def android_path_join(a, *args):
    """Similar to os.path.join(), but might differ in behavior on Windows"""

    if args == []:
        return a

    if len(args) == 1:
        return _android_path_join_two(a, args[0])

    return android_path_join(android_path_join(a, args[0]), *args[1:])


def pull_metadata(package, dir, adb_puller):
    root_screenshot_dir = android_path_join(adb_puller.get_external_data_dir(), "screenshots")
    metadata_file = android_path_join(
        root_screenshot_dir,
        package,
        'screenshots-default/metadata.xml')

    old_metadata_file = android_path_join(
        OLD_ROOT_SCREENSHOT_DIR,
        package,
        'app_screenshots-default/metadata.xml')

    if adb_puller.remote_file_exists(metadata_file):
        adb_puller.pull(metadata_file, join(dir, 'metadata.xml'))
    elif adb_puller.remote_file_exists(old_metadata_file):
        adb_puller.pull(old_metadata_file, join(dir, 'metadata.xml'))
        metadata_file = old_metadata_file
    else:
        create_empty_metadata_file(dir)

    return metadata_file.replace("metadata.xml", "")


def create_empty_metadata_file(dir):
    with open(join(dir, 'metadata.xml'), 'w') as out:
        out.write(

    """<?xml version="1.0" encoding="UTF-8"?>
<screenshots>
</screenshots>""")


def pull_images(dir, device_dir, adb_puller):
    root = ET.parse(join(dir, 'metadata.xml')).getroot()
    for s in root.iter('screenshot'):
        filename_nodes = s.findall('relative_file_name')
        for filename_node in filename_nodes:
            adb_puller.pull(
                android_path_join(device_dir, filename_node.text),
                join(dir, os.path.basename(filename_node.text)))
        dump_node = s.find('view_hierarchy')
        if dump_node is not None:
            adb_puller.pull(android_path_join(device_dir, dump_node.text), join(dir, os.path.basename(dump_node.text)))


def pull_all(package, dir, adb_puller):
    device_dir = pull_metadata(package, dir, adb_puller=adb_puller)
    pull_images(dir, device_dir, adb_puller=adb_puller)


def pull_filtered(package, dir, adb_puller, filter_name_regex=None):
    device_dir = pull_metadata(package, dir, adb_puller=adb_puller)
    _validate_metadata(dir)
    metadata.filter_screenshots(join(dir, 'metadata.xml'), name_regex=filter_name_regex)
    pull_images(dir, device_dir, adb_puller=adb_puller)


def _summary(dir):
    root = ET.parse(join(dir, 'metadata.xml')).getroot()
    count = len(root.findall('screenshot'))
    print("Found %d screenshots" % count)


def _validate_metadata(dir):
    try:
        ET.parse(join(dir, 'metadata.xml'))
    except ET.ParseError:
        raise RuntimeError("Unable to parse metadata file, this commonly happens if you did not call ScreenshotRunner.onDestroy() from your instrumentation")


def pull_screenshots(process,
                     adb_puller,
                     perform_pull=True,
                     temp_dir=None,
                     filter_name_regex=None,
                     record=None,
                     verify=None,
                     opt_generate_png=None):

    if not perform_pull and temp_dir is None:
        raise RuntimeError("""You must supply a directory for temp_dir if --no-pull is present""")

    temp_dir = temp_dir or tempfile.mkdtemp(prefix='screenshots')

    if not os.path.exists(temp_dir):
        os.makedirs(temp_dir)

    copy_assets(temp_dir)

    if perform_pull is True:
        pull_filtered(process, adb_puller=adb_puller, dir=temp_dir, filter_name_regex=filter_name_regex)

    _validate_metadata(temp_dir)

    path_to_html = generate_html(temp_dir)

    if record or verify:
        # don't import this early, since we need PIL to import this
        from .recorder import Recorder
        recorder = Recorder(temp_dir, record or verify)
        if verify:
            recorder.verify()
        else:
            recorder.record()

    if opt_generate_png:
        generate_png(path_to_html, opt_generate_png)
        shutil.rmtree(temp_dir)
    else:
        print("\n\n")
        _summary(temp_dir)
        print('Open the following url in a browser to view the results: ')
        print('  file://%s' % path_to_html)
        print("\n\n")


def setup_paths():
    android_home = common.get_android_sdk()
    os.environ['PATH'] = os.environ['PATH'] + ":" + android_home + "/platform-tools/"


def main(argv):
    setup_paths()
    try:
        opt_list, rest_args = getopt.gnu_getopt(
            argv[1:],
            "eds:",
            ["generate-png=", "filter-name-regex=", "apk", "record=", "verify=", "temp-dir=", "no-pull"])
    except getopt.GetoptError:
        usage()
        return 2

    if len(rest_args) != 1:
        usage()
        return 2

    process = rest_args[0]  # something like com.facebook.places.tests

    opts = dict(opt_list)

    if "--apk" in opts:
        # treat process as an apk instead
        process = aapt.get_package(process)

    should_perform_pull = ("--no-pull" not in opts)

    puller_args = []
    if "-e" in opts:
        puller_args.append("-e")

    if "-d" in opts:
        puller_args.append("-d")

    if "-s" in opts:
        puller_args += ["-s", opts["-s"]]

    return pull_screenshots(process,
                            perform_pull=should_perform_pull,
                            temp_dir=opts.get('--temp-dir'),
                            filter_name_regex=opts.get('--filter-name-regex'),
                            opt_generate_png=opts.get('--generate-png'),
                            record=opts.get('--record'),
                            verify=opts.get('--verify'),
                            adb_puller=SimplePuller(puller_args))


if __name__ == '__main__':
    sys.exit(main(sys.argv))

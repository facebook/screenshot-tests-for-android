#!/bin/bash

set -x
set -e
set -o pipefail

echo $ANDROID_SERIAL

rm -rf ~/.m2 ~/.gradle/caches

gradle :core:install
gradle :plugin:install

cd examples/one
gradle connectedAndroidTest --info
gradle screenshotTests --info

rm -rf ~/.m2 ~/.gradle/caches

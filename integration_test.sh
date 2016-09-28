#!/bin/bash

set -x
set -e
set -o pipefail

echo $ANDROID_SERIAL

cleanup() {
    rm -rf ~/.m2 ~/.gradle/caches
    rm -rf */build/
    rm -rf examples/one/build/
}

cleanup

./gradlew :plugin:install
./gradlew :core:install

cd examples/app-example
./gradlew connectedAndroidTest
./gradlew screenshotTests

cleanup

#!/bin/bash
set -ev

if [ "${TRAVIS}" = "true" ]; then
  homedir=$TRAVIS_BUILD_DIR
else
  homedir=`pwd`
fi

bundle exec jekyll build --destination ${homedir}/parse/public

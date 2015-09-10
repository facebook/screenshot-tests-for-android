#!/bin/bash
set -ev

if [ "${TRAVIS}" = "true" ]; then
  homedir=$TRAVIS_BUILD_DIR
else
  homedir=`pwd`
fi

workingdir=${homedir}/parse

cd $workingdir

sed -e 's/PARSE_APPLICATION_ID/'"${PARSE_APPLICATION_ID}"'/g' \
    -e 's/PARSE_MASTER_KEY/'"${PARSE_MASTER_KEY}"'/g' ${workingdir}/config/template.json > ${workingdir}/config/global.json


parse deploy

cd $homedir

#!/bin/bash

homedir=`pwd`

workingdir=${homedir}/parse

sed -e 's/applicationId":[^"]*"[^"]*"/applicationId": "PARSE_APPLICATION_ID"/g' \
    -e 's/masterKey":[^"]*"[^"]*"/masterKey": "PARSE_MASTER_KEY"/g' ${workingdir}/config/global.json > ${workingdir}/config/template.json

#!/bin/bash
if [[ "$2" == "" ]] ; then
  echo "Usage: $0 page-id 'Page title'"
  exit 1
fi
cat >> "_docs/$1.md" <<EOF
---
id: $1
title: $2
layout: docs
permalink: /docs/$1/
---

XXX
EOF

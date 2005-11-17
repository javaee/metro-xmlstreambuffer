#!/bin/sh

find $2 -name '*.c14n.xml' -exec rm {} \;

find $2 -name "CVS" -prune -o -name '..' -prune -o -type f -exec $1 {} \;


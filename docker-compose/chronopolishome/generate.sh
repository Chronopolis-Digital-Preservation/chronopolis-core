#!/bin/sh

MANIFEST="manifest-sha256.txt"
TAGMANIFEST="tagmanifest-sha256.txt"
TEMPLATE=bag.json.template
 
if [ $# -lt 2 ]; then
  echo "usage: $0 <depositor> <bag ...>"
  exit 1
fi
DEPOSITOR=$1
shift

EXPORT=/export/outgoing/bags/$DEPOSITOR
if [ ! -d $EXPORT ]; then
  echo "missing $EXPORT"
  exit 1
fi

if [ ! -f $TEMPLATE ]; then
  echo "missing $TEMPLATE"
  exit 1
fi

    for bag in $*; do
        current_manifest="$EXPORT/$bag/$MANIFEST"
        if [ ! -f $current_manifest ]; then
          echo "missing $current_manifest"
          continue
        fi
        current_tagmanifest="$EXPORT/$bag/$TAGMANIFEST"
        if [ ! -f $current_tagmanifest ]; then
          echo "missing $current_tagmanifest"
          continue
        fi
 
# src must end in / in case it is a symlink
        src=$EXPORT/${bag}/
        if [ ! -d $src ]; then
          echo "missing $src"
          continue
        fi
        SIZE=`find $src -type f -printf "%s\n" | awk '{sum+=$1}END{print sum}'`
        TOTALFILES=`find $src -type f -print | wc -l`
        sed "
          s/COLLECTION/${bag}/g
          s/DEPOSITOR/$DEPOSITOR/g
          s/SIZE/$SIZE/g
          s/TOTALFILES/$TOTALFILES/g
        " $TEMPLATE > ${bag}-bag.json

    done


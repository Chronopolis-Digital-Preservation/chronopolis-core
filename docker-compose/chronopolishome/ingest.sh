#!/bin/sh

INGEST_USER=admin
INGEST_PASSWORD=admin
INGEST_BAG_CREATE="http://localhost:8080/api/bags"

if [ $# -lt 1 ]; then
  echo "usage: $0 <bag ...>"
  exit 1
fi

for bag in $*; do
  echo ${bag}

  bagjson=${bag}-bag.json
  if [ ! -f $bagjson ]; then
    echo "missing $bagjson"
    continue
  fi

  runjson=${bag}-run.json

  curl -k --user ${INGEST_USER}:${INGEST_PASSWORD} --header "Content-Type: application/json" --data @$bagjson ${INGEST_BAG_CREATE} -o $runjson
  res=$?
  if [ $res -ne 0 ]; then
    echo "curl $bagjson failed"
  fi
done

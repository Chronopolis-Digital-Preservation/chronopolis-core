#!/bin/bash

docker pull uazlibraries/ace-audit-manager:1.12
docker pull uazlibraries/ace-integrity-management:1.12
docker pull uazlibraries/ace-dbstore-mysql:1.12
docker pull dpage/pgadmin4
docker pull postgres:12.2
docker build --rm -t local/chron-build .
cd ../chron-ingest
./build.sh
cd ../chron-replication
./build.sh

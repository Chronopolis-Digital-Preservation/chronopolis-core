#!/bin/bash
docker run -p 8060:80 \
    -e 'PGADMIN_DEFAULT_EMAIL=chron@localhost.localdomain' \
    -e 'PGADMIN_DEFAULT_PASSWORD=password' \
    -d dpage/pgadmin4

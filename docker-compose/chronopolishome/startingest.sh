#!/bin/bash
chown -R chronopolis:chronopolis /scratch1
chown -R chronopolis:chronopolis /export

/usr/sbin/sshd

su chronopolis <<ENDOFSU
cd /usr/local/chronopolis/ingest
/usr/bin/java -Dspring.config.location=file:/usr/local/chronopolis/ingest/application.yml -jar /chronopolis-core/ingest-rest/target/ingest-rest-3.3.0-SNAPSHOT.jar
ENDOFSU

while true; do
    sleep 30
done

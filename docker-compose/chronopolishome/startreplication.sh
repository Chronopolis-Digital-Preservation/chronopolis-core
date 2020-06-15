#!/bin/bash
chown -R chronopolis:chronopolis /scratch1
chown -R chronopolis:chronopolis /export
su - chronopolis <<ENDOFSU
cd /usr/local/chronopolis/replication
/usr/bin/java -Dspring.config.location=file:/usr/local/chronopolis/replication/application.yml -jar /chronopolis-core/replication-shell/target/replication-shell-3.3.0-SNAPSHOT.jar
ENDOFSU

/usr/sbin/sshd

while true; do
    sleep 30
done
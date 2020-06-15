FROM centos:7

RUN adduser chronopolis

RUN mkdir /var/log/chronopolis
RUN chown chronopolis:chronopolis /var/log/chronopolis

COPY files/ssh/ /etc/ssh/

# RUN mkdir -p /export/outgoing/bags
# RUN chown -R chronopolis:chronopolis /export/outgoing/bags
# RUN mkdir -p /export/outgoing/tokens
# RUN chown -R chronopolis:chronopolis /export/outgoing/tokens

RUN chmod 777 /var/run

# RUN yum -y update
RUN yum -y install java-1.8.0-openjdk-devel

# We are going to need Maven
RUN yum -y install maven

# We need ssh for rsync to connect to...
RUN yum -y install openssh-server passwd openssh-clients
RUN mkdir /var/run/sshd

# We also need rsync
RUN yum -y install rsync
RUN yum -y install perl

# And psql for luck...
# RUN yum -y install postgresql

EXPOSE 8080
EXPOSE 8009

ENTRYPOINT ["/home/chronopolis/startingest.sh"]

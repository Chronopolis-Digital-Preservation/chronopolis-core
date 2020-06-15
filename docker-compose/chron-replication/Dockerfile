FROM centos:7

RUN adduser chronopolis

RUN mkdir /var/log/chronopolis
RUN chown chronopolis:chronopolis /var/log/chronopolis

RUN chmod 777 /var/run

# RUN yum -y update
RUN yum -y install java-1.8.0-openjdk-devel

# We are going to need Maven
RUN yum -y install maven

RUN yum -y install openssh-clients

# We also need rsync
RUN yum -y install rsync
RUN yum -y install perl

ENTRYPOINT ["/home/chronopolis/startreplication.sh"]

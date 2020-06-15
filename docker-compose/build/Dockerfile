FROM centos:7

RUN adduser chronopolis

RUN yum -y install java-1.8.0-openjdk-devel

# We are going to need Maven
RUN yum -y install maven

# We also need rsync
RUN yum -y install rsync

ENTRYPOINT ["/home/chronopolis/chronopolisbuild.sh"]

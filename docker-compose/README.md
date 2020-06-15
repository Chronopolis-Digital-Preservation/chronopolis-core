
# Chronopolis Docker ( DockerChron )


Current work:

To enable better development and broader environment, we need a self-contained docker environment for developers.

The ACE components used in this build are available and documented here:

[https://github.com/ualibraries/ace-dbstore-mysql](https://github.com/ualibraries/ace-dbstore-mysql)

# Building the docker infrastructure

Since the chronopolis system has a large number of dependencies, this process was designed to allow developers to easily edit and modify the source code on their local disk using local tools, then shut down the chronopolis system, recompile the code, and bring it all back up with state still intact.

First we grab the source from gitlab...
```jsx
git clone [https://gitlab.com/chronopolis/chronopolis-core.git](https://gitlab.com/chronopolis/chronopolis-core.git)
```
...then build the docker images we need and start up the build environment...
```jsx
cd chronopolis-core/docker-compose/build
./buildimages.sh
./buildup.sh
```
...we ssh into the build container and compile the source code...this can take a while the first time...
```jsx
./buildssh.sh
su - chronopolis
cd /chronopolis-core
./mvnw install
```
...we initialize the postgresql db with the tables we will need...
```jsx
cd ingest-rest
../mvnw -Dflyway.user=postgres -Dflyway.password=password -Dflyway.url=jdbc:postgresql://postgresql:5432/ingestdb flyway:migrate
```
...and finally, we exit out of the build environment, shut it down, and then bring up chronpolis.
```jsx
exit
exit
./builddown.sh
cd ..
./chronup.sh
```
The first build can be quite long, since it downloads source from external repositories and uses them in compiling the rest of chronopolis.  The system stores these external resources going forward and subsequent compiles are MUCH faster.  This relies on Maven caching things in the chronopolis-core/docker-compose/chronopolishome/.m2 directory.  If for some reason you need to download/refresh/empty that cache, just delete everything in that directory and you can recompile.

The build environment includes a pgadmin container that you can use to examine the postgresql db running on the postgresql container.

Your infrastructure is now up and running.

# Setting up a test environment

## Ace

First, we go to the ACE audit manager interface ( [http://localhost:8090/ace-am](http://localhost:8090/ace-am)  ).  Once there, click on the status link at the top and it will ask you to log in ( username:admin password:admin ).  A link called 'System Settings' will appear at the top and when you click on it, a configuration window will appear.  Put the work 'integrity' into the box labelled 'IMS Host'.

Hit the submit link at the bottom.

The configuration and mysql data for ACE is stored in a persistent volume associated with the container group the docker compose creates and will survive stopping and starting of containers within the environment during development.  If you prune or remove these volumes, the token, audit, and configuration will need to be recreated.

## Chronopolis

The ingest server webUI is at: [http://localhost:8070/](http://localhost:8070/)

username: admin
password: admin

To set up the environment, select 'User Config'

Add a user as follows:

Username: developer
Password: developer
Role: Role_User
**Make sure to click the check box for 'Is a node'**
Click 'Create User'

Now select Depositors from the admin menu, then click Add Depositor.

Organization Name: development
Organization Address: development
Namespace: development
**Select the user 'developer' we just created in the Replicating Nodes box so that it is highlighted.**
Click on 'Add Depositor'

Make sure you click on the word 'developer' in the replicating nodes box before you select Add Depositor.

Now select Storage Region Create and create two storage regions:

The first should have these values:
Owning Node: developer
Data Type: BAG
Storage Type: LOCAL
Total Capacity: 1
Storage Unity: GB
Replication Server: chron-ingest
Replication Path: /bags
Replication Username: chronopolis

The second should have these values:
Owning Node: developer
Data Type: TOKEN
Storage Type: LOVAL
Storage Unit: GB
Replication Server: chron-ingest
Replication Path: /tokens
Replication Username: chronopolis

You now have a basic environment to test in.

The database that holds the configuration you just created is persistent on disk in the chronopolis-core/docker-compose/postgresql/data directory.  This directory will persist regardless of actions take within docker.  If you need to clean up, shutdown the chron environment, delete everything inside that directory, bring up the build environment, and re-run the database initialization step executed during the build process.

# Testing Ingest

Both the ingest and replication servers have scripts in the docker-compose directory that will give you a terminal session on those containers.

Run the ingest.sh command in  and you will be on the ingest server ( chron-ingest ) container.

First we need to make something to ingest...

```jsx
su - chronopolis
cd /export/outgoing/bags
mkdir development
cd development
mkdir devbag
cd devbag
echo "This is some content." > test.txt
cd ..
bag baginplace --tagmanifestalgorithm sha256 --payloadmanifestalgorithm sha256 devbag
cd
./generate.sh development devbag
./ingest.sh devbag
```

The web page on the ingest web server should look like this:

> Written with [StackEdit](https://stackedit.io/).


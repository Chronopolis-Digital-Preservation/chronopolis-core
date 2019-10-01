Chronopolis-Core
================

The Chronopolis Core project contains services running internally at Chronopolis
for distribution of digital preservation content.

Documentation for the [Ingest HTTP API ][1]
Documentation for the [Ingest Database][2]
Documentation for [starting development][3]


The layout of the project is as follows:

    Core Chronopolis /
         |    docs                  // Developer documentation
         |    common                // Common code to be shared between services
         |    rest-models           // Models/interfaces defining the internal Chronopolis HTTP API
         |    rest-entities         // Database entities the for Chronopolis Ingest Server
         |    rest-common           // Common classes for interacting with the HTTP API
         |    tokenizer             // Service to create ACE Tokens for BagIt Bags
         |    tokenizer-mq          // Alternative implementation of tokenizer using a message queue
         |    ingest-rest           // Implementation of the internal Chronopolis HTTP API
         |    replication-shell     // Service for handling distribution of content at Chronopolis nodes


Development
===========
Please make changes to the develop branch first for testing :)
```
git fetch origin
git checkout --track origin/develop
```
Todo: 

* Add property for setting the from field for smtp (may want to copy ace and do from@localhost)


[1]: https://chronopolis-docs.umiacs.io/
[2]: https://chronopolis-docs.umiacs.io/database.html
[3]: https://chronopolis-docs.umiacs.io/development.html

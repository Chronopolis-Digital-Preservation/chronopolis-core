#!/bin/bash
../mvnw -Dflyway.user=postgres -Dflyway.password=password -Dflyway.url=jdbc:postgresql://postgresql:5432/ingestdb flyway:migrate

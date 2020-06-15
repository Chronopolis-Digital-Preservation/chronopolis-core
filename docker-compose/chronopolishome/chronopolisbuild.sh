#!/bin/bash

chown -R chronopolis:chronopolis /home/chronopolis

su chronopolis <<ENDOFSU
  # Things you might want to do as chronopolis when you bring up the build env.
ENDOFSU 

while true; do
    sleep 30
done

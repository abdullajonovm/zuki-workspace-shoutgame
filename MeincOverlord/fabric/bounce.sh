#!/bin/bash

#don't actually deploy anything, just restart both servers

fab mrsoa_stop mrsoa_start -Rstage-sync,stage-collector -P
#fab mrsoa_stop mrsoa_start -Rreal-stage-sync,real-stage-collector -P

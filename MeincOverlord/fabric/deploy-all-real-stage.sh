#!/bin/bash

fab install_services:start=0 install_webapps:start=0 mrsoa_start -Rreal-stage-sync,real-stage-collector -P

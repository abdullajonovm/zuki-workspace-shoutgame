#!/bin/bash

fab install_bundles:start=0 install_services:start=0 install_webapps:start=0 mrsoa_start -Rdc1-sync,dc1-collector -P

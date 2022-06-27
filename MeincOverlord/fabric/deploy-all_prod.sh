#!/bin/bash

fab install_bundles:start=0 install_services:start=0 install_webapps:start=0 mrsoa_start -Rprod-sync,prod-collector -P --set=skip_prod_prompt=1

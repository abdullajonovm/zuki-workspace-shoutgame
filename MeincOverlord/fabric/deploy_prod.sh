#!/bin/bash

fab install_services:name="$1",start=0 mrsoa_start -Rprod-sync,prod-collector --set=skip_prod_prompt=1
#fab install_services:name="$1",start=0 -Rprod-sync,prod-collector -P --set=skip_prod_prompt=1
#fab install_services:name="$1",start=0 install_webapps:start=0 -Rprod-sync,prod-collector -P --set=skip_prod_prompt=1
#fab install_services:name="$1",start=0 install_webapps:start=0 mrsoa_start -Rprod-sync,prod-collector -P --set=skip_prod_prompt=1
#fab install_services:start=0 install_bundles:start=0 install_webapps:start=0 mrsoa_start -Rprod-sync,prod-collector -P --set=skip_prod_prompt=1
#fab install_services:start=0 install_bundles:start=0 install_webapps:start=0 -Rprod-sync,prod-collector --set=skip_prod_prompt=1
#fab install_services:start=0 install_bundles:start=0 install_webapps:start=0 -Rprod-temp --set=skip_prod_prompt=1
#fab install_services:start=0 install_bundles:start=0 install_webapps:start=0 -Hhttp://ec2-107-22-29-58.compute-1.amazonaws.com/,ec2-54-80-80-10.compute-1.amazonaws.com,ec2-54-221-168-130.compute-1.amazonaws.com --set=skip_prod_prompt=1
#fab install_services:start=0 install_bundles:start=0 install_webapps:start=0 -Rprod-sync,prod-collector -P --set=skip_prod_prompt=1
#fab install_services:start=0 install_webapps:start=0 mrsoa_start -Rprod-sync,prod-collector -P --set=skip_prod_prompt=1
#fab install_webapps:start=0 mrsoa_start -Rprod-sync,prod-collector -P --set=skip_prod_prompt=1

#!/bin/bash

#fab install_webapps:start=0 mrsoa_start -Rstage-sync,stage-collector -P
#fab install_services:name="$1",start=0 install_webapps:start=0 mrsoa_start -Rstage-sync,stage-collector -P
#fab install_services:start=0,name="$1" mrsoa_start -Rstage-sync,stage-collector -P
#fab install_services:start=0 mrsoa_start -Rstage-sync,stage-collector -P
fab install_services:start=0 install_webapps:start=0 install_bundles:start=0 mrsoa_start -Rdc1-sync,dc1-collector -P
#fab install_services:start=0 install_bundles:start=0 mrsoa_start -Rstage-sync,stage-collector -P
#fab install_bundles:start=0,name="$1" mrsoa_start -Rstage-sync,stage-collector -P
#fab install_services:start=0 install_webapps:start=0 mrsoa_start -Rstage-sync,stage-collector -P
#fab install_services:name="$1",start=0 mrsoa_start -Rstage-sync,stage-collector -P
#fab install_webapps:name="$1",start=0 mrsoa_start -Rstage-sync,stage-collector -P


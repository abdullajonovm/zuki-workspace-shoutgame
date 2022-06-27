#!/bin/bash

#don't actually deploy anything, just restart both servers

fab mrsoa_stop mrsoa_start -Rprod-sync,prod-collector -P --set=skip_prod_prompt=1

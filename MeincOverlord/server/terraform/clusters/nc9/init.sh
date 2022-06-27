#!/bin/bash

set -e

echo -e "Initializing Terraform\n"
(
    set -x
    terraform init
)

echo
read -rp "Import existing Route53 Zone for shoutgameplay.com into local Terraform state (y/n)? " DO_IMPORT
if [ "$DO_IMPORT" = "y" ]; then
    echo
    (
        set -x
        terraform import module.homogeneous.aws_route53_zone.main Z4WL1XIAZQK94
    )
fi

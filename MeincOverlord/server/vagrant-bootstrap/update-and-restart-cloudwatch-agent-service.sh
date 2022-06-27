#!/bin/bash

set -ex

AGENT_HOME=/opt/aws/amazon-cloudwatch-agent
JSON_PATH=$AGENT_HOME/etc/amazon-cloudwatch-agent.json
TOML_PATH=$AGENT_HOME/etc/amazon-cloudwatch-agent.toml
CONF_PATH=$AGENT_HOME/etc/common-config.toml

$AGENT_HOME/bin/config-translator --input $JSON_PATH --output $TOML_PATH --mode ec2 --config $CONF_PATH
    
if ! $AGENT_HOME/bin/amazon-cloudwatch-agent -schematest -config $TOML_PATH; then
    set +x
    echo "Agent configuration failed!" >&2
    exit 1
fi

HOSTNAME="$(hostname)"
sed -i -e "s/\$\[hostname\]/$HOSTNAME/g" $AGENT_HOME/etc/amazon-cloudwatch-agent.json

CLUSTERNAME="$(awk -F '-' '{print $1}' <<<"$HOSTNAME")"
sed -i -e "s/\$\[clustername\]/$CLUSTERNAME/g" $AGENT_HOME/etc/amazon-cloudwatch-agent.json

#X $AGENT_HOME/bin/amazon-cloudwatch-agent-ctl -a stop
$AGENT_HOME/bin/amazon-cloudwatch-agent-ctl -a start

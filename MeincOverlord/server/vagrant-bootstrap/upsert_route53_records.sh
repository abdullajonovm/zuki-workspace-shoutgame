#!/bin/bash

if ! command -v aws >/dev/null 2>&1; then
    echo "Missing aws command line utility" >&2
    exit 1
fi

HOSTED_ZONE_ID="Z4WL1XIAZQK94"  #shoutgameplay.com
if [ "$1" == "-z" ]; then
    HOSTED_ZONE_ID="$2"
    shift 2
fi

if [ "$1" == "-a" ]; then
    USE_A_RECORDS=1
    shift
fi

NOMAD_HOSTS_FILE="/etc/nomad-dnsmasq/hostsdir/hosts"
if [ ! -f "$NOMAD_HOSTS_FILE" ]; then
    echo "Missing hosts file. Make sure nomad-dnsmasq is running." >&2
    exit 1
fi

hosts_record_lines="$(grep '^\([[:digit:]]\{,3\}\.\)\{3\}[[:digit:]]\{,3\}[[:space:]]\+[[:alnum:]]\+' "$NOMAD_HOSTS_FILE")"
if [ $? -ne 0 ]; then
    echo "Invalid hosts file. Make sure all desired nomad jobs are running." >&2
    exit 1
fi
readarray -t hosts_records <<<"$hosts_record_lines"

FQDN="$(hostname -f)"
if [ "$1" == "-h" ]; then
	FQDN="$2"
	shift 2
fi
fqdn_len=${#FQDN}

echo -e "\nREQUEST:"
changes_json=
for hosts_record in "${hosts_records[@]}"; do
    hosts_record_len=${#hosts_record}
    hosts_record_domain_index=$((hosts_record_len - fqdn_len))
    hosts_record_domain="${hosts_record:hosts_record_domain_index}"
    if [ "$hosts_record_domain" == "$FQDN" ]; then
        ((upsert_count += 1))
        read -r hosts_record_ip hosts_record_name <<<"$hosts_record"
        if [ "$USE_A_RECORDS" ]; then
            hosts_record_type="A"
            hosts_record_value="$hosts_record_ip"
        else
            hosts_record_type="CNAME"
            hosts_record_value="$FQDN"
        fi
        echo "$hosts_record_name ➜ $hosts_record_value"
        read -rd '' changes_json_new <<-EOF
			{
				"Action": "UPSERT",
				"ResourceRecordSet": {
					"Name": "$hosts_record_name",
					"Type": "$hosts_record_type",
					"TTL": 60,
					"ResourceRecords": [
						{
							"Value": "$hosts_record_value"
						}
					]
				}
			}
		EOF
        changes_json="${changes_json}${changes_json:+,}${changes_json_new}"
    fi
done

if [ ! "$changes_json" ]; then
    echo "Empty hosts file. Make sure all desired nomad jobs are running." >&2
    exit 1
fi

read -rd '' changes_json_first <<-EOF
{
	"HostedZoneId": "$HOSTED_ZONE_ID",
	"ChangeBatch": {
		"Comment": "automated upsert of $FQDN",
		"Changes": [
EOF

read -rd '' changes_json_last <<-EOF
		]
	}
}
EOF

changes_json="${changes_json_first}${changes_json}${changes_json_last}"

echo -e "\nRESPONSE:"
if ! aws route53 change-resource-record-sets \
        --hosted-zone-id "$HOSTED_ZONE_ID" \
        --cli-input-json "$changes_json"; then
    echo "Error upserting records"
fi

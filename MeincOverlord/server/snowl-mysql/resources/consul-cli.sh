#!/bin/bash

set -e

if [ ! "$CONSUL_HOST" ]; then
    if grep -q docker /proc/1/cgroup; then
        # We're in a docker container
        HOST_IP=$(ip route | awk '/^default/ {print $3}')
        CONSUL_HOST="$HOST_IP:8500"
    else
        CONSUL_HOST="127.0.0.1:8500"
    fi
fi

if ! command -v curl >&-; then
    echo "Cannot find curl executable" >&2
    exit 1
fi
if ! command -v jq >&-; then
    echo "Cannot find jq executable" >&2
    exit 1
fi
if ! command -v base64 >&-; then
    echo "Cannot find base64 executable" >&2
    exit 1
fi

function exit_with_usage {
    cat <<-EOF >&2
		$(basename $0) <command> <subcommand> [...]

		    kv get [options] <key>

		        Retrieves the value from Consul's key-value store for the given key name.

		         -delimit <delimiter>
		             Use supplied delimiter string instead of newline characters to delimit records
		             returned in output
		         -recurse
		             Supplied key name is used as a key name prefix, and multiple key-value pairs can be
		             returned. The colon character is used to separate the key and value of each pair.
		         -keys
		             Supplied key name is used as a key name prefix, and all key names with this prefix
		             are returned. No values are returned.

		    kv put <key> <value>

		        Writes the supplied value to the given key in Consul's key-value store.

                        -base64
                            Store value in Consul as a base64 encoded value. Note that control
                            characters (e.g. newline, null, etc.) aren't handled properly embedded
                            in the command line. If you wish to encode such characters, base64
                            encode the value beforehand.
	EOF
    exit 0
}

if [ ! "$1" ]; then
    exit_with_usage
fi

function urlencode() {
    # Usage: urlencode <string-to-encode> <return-variable-name>
    # Use standard ASCII codeset
    local LC_COLLATE=C
    local src_length="${#1}"
    unset $2
    for (( i = 0; i < src_length; i++ )); do
        local src_char="${1:i:1}"
        local dest_char=''
        case $src_char in
            [a-zA-Z0-9.~_\-/])
                printf -v $2 "%s%s" "${!2}" "$src_char"
                ;;
            *)
                # printf argument strings beginning with a single quote convert the next character
                # to its decimal value within the current language codeset
                printf -v $2 '%s%%%02X' "${!2}" "'$src_char"
                ;;
        esac
    done
}

while [ "$1" ]; do
    COMMAND="$1"
    shift
    case "$COMMAND" in
        "kv")
            SUBCOMMAND="$1"
            shift
            case "$SUBCOMMAND" in
                "get")
                    CMD_PREFIX="curl --silent http://$CONSUL_HOST/v1/kv"
                    DELIMITER=$'\n'
                    unset RECURSE_FLAG KEYS_FLAG KEEP_PREFIX
                    while [ "$1" ]; do
                        NEXT_ARG="$1"
                        shift
                        case "$NEXT_ARG" in
                            "-delimit")
                                DELIMITER="$1"
                                shift
                                ;;
                            "-recurse")
                                RECURSE_FLAG=1
                                ;;
                            "-keys")
                                KEYS_FLAG=1
                                ;;
                            "-keep_prefix")
                                KEEP_PREFIX=1
                                ;;
                            *)
                                CONSUL_KEY="$NEXT_ARG"
                                break
                                ;;
                        esac
                    done

                    if [ $RECURSE_FLAG ]; then
                        urlencode "$CONSUL_KEY" CONSUL_KEY_URLPART
                        CMD="$CMD_PREFIX/$CONSUL_KEY_URLPART?recurse"
                        RESULT="$($CMD | jq -r '.[] | "\(.Key):\(.Value)"')"
                        IFS=$'\n'; for pair in $RESULT; do
                            key="${pair%%:*}"
                            [ ! $KEEP_PREFIX ] &&
                                key="${key#$CONSUL_KEY}"
                            val="${pair#*:}"
                            echo -n "$key:$(base64 -d <<<"$val")$DELIMITER"
                        done; unset IFS
                    elif [ $KEYS_FLAG ]; then
                        urlencode "$CONSUL_KEY" CONSUL_KEY_URLPART
                        CMD="$CMD_PREFIX/$CONSUL_KEY_URLPART?keys"
                        RESULT="$($CMD | jq -r '.[]')"
                        IFS=$'\n'; for key in $RESULT; do
                            echo -n "$key$DELIMITER"
                        done; unset IFS
                    else
                        urlencode "$CONSUL_KEY" CONSUL_KEY_URLPART
                        CMD="$CMD_PREFIX/$CONSUL_KEY_URLPART?raw"
                        RESULT="$($CMD)"
                        # We use a for loop even though a single result is returned so that null
                        # responses don't output a newline suffix
                        IFS=$'\n'; for val in $RESULT; do
                            echo -n "$val$DELIMITER"
                        done; unset IFS
                    fi

                    exit 0
                    ;;
                "put")
                    [ ! "$1" -o ! "$2" ] &&
                        exit_with_usage
                    unset BASE64_FLAG
                    while [ "$1" ]; do
                        NEXT_ARG="$1"
                        shift
                        case "$NEXT_ARG" in
                            "-base64")
                                BASE64_FLAG=1
                                ;;
                            *)
                                # if CONSUL_KEY in unset
                                if [ ! ${CONSUL_KEY+1} ]; then
                                    CONSUL_KEY="$NEXT_ARG"
                                else
                                    CONSUL_VALUE="$NEXT_ARG"
                                    break
                                fi
                                ;;
                        esac
                    done
                    [ ! "$CONSUL_KEY" -o ! "$CONSUL_VALUE" ] &&
                        exit_with_usage

                    if [ $BASE64_FLAG ]; then
                        CONSUL_VALUE="$(base64 -w0 - <<<"$CONSUL_VALUE")"
                    fi
                    CMD_PREFIX="curl --silent --request PUT --data-binary @- http://$CONSUL_HOST/v1/kv"
                    urlencode "$CONSUL_KEY" CONSUL_KEY_URLPART
                    CMD="$CMD_PREFIX/$CONSUL_KEY_URLPART"
                    RESULT="$($CMD <<<"$CONSUL_VALUE")"
                    if [ "$RESULT" != "true" ]; then
                        echo "Operation failed" >&2
                        exit 2
                    fi
                    exit 0
                    ;;
                *)
                    echo -e "Unknown option to kv command '$1'\n" >&2
                    exit_with_usage
                    ;;
            esac
            ;;
        "-h"|"--help")
            exit_with_usage
            ;;
        *)
            echo -e "Unknown command '$COMMAND'\n" >&2
            exit_with_usage
            ;;
    esac
done

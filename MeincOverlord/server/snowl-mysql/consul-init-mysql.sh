#!/bin/bash

set -e

cd "/var/lib"

HOST_DIR="/mnt/host/$NOMAD_ALLOC_INDEX/data"
mkdir -p "$HOST_DIR"
chown -R mysql:mysql "$HOST_DIR"
chmod 755 "$HOST_DIR"
ln -sf "$HOST_DIR" mysql-data

cd "/var/log"

HOST_DIR="/mnt/host/$NOMAD_ALLOC_INDEX/log"
mkdir -p "$HOST_DIR"
chown -R mysql:mysql "$HOST_DIR"
chmod 755 "$HOST_DIR"
rm -r /var/log/mysql
ln -sf "$HOST_DIR" mysql

if command -v dclone.sh >&-; then
    dclone.sh
fi

sed -i'' 's#^datadir\>.*$#datadir = /var/lib/mysql-data#' /etc/mysql/mysql.conf.d/mysqld.cnf

if [ ! -d /var/lib/mysql-data/mysql ]; then
    # Database hasn't been initialized yet

    function random_pass {
        echo -n "$(tr -cd 'A-Z-a-z-0-9!@#$%^&*()_\-+={\[}\]:;/?.>,<~\\|' </dev/urandom | head -c 16)"
    }

    INIT_SQL_FILE="/docker-entrypoint-initdb.d/00_consul-init-mysql.sql"
    >$INIT_SQL_FILE
    function append_to_init_sql {
        echo "CREATE USER IF NOT EXISTS '$user'@'$host' IDENTIFIED BY '$pass';" >>$INIT_SQL_FILE
        echo "GRANT ALL ON *.* TO '$user'@'$host' WITH GRANT OPTION;" >>$INIT_SQL_FILE
    }

    DELIMITER=$'\x1E'  # RS char (record separator)
    LOGINS=$(consul-cli.sh kv get -delimit $DELIMITER -recurse mysql/login/)
    IFS=$DELIMITER; for pair in $LOGINS; do
        user="${pair%%:*}"
        pass="${pair#*:}"
        if [ "$pass" = "!" ]; then
            pass="$(random_pass)"
            consul-cli.sh kv put "mysql/login/$user" "$pass"
        fi
        host="%"
        [ "$user" = "root" ] && root_pass="$pass"
        append_to_init_sql
    done; unset IFS

    if [ ! "$root_pass" ]; then
        user="root"
        pass="$(random_pass)"
        host="%"
        append_to_init_sql
        consul-cli.sh kv put "mysql/login/$user" "$pass"
    fi

    # This export works around a bug in docker-entrypoint.sh (v5.7) where when the root host is not
    # specified nor the localhost, the script attempts to create a root@'%' user, and somehow
    # corrupts the user table
    export MYSQL_ROOT_HOST=localhost
    # This export instructs the docker-entrypoint.sh to not create users. Instead we do that
    # ourselves above
    export MYSQL_ALLOW_EMPTY_PASSWORD=1
fi

exec docker-entrypoint.sh mysqld "$@"

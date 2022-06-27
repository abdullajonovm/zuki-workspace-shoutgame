FROM mysql:5.7
MAINTAINER Matt Pontius <mpontius@shout.tv>
LABEL description="Shout Database Server"

RUN  apt-get update && apt-get install -y vim curl jq wget

#ADD http://localhost:40080/launch-with-consul-template.sh /usr/local/bin/
ADD http://localhost:40080/consul-cli.sh /usr/local/bin/
#ADD http://localhost:40080/consul-init-mysql.sh /usr/local/bin/
COPY consul-init-mysql.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/*.sh

COPY snowl-mysql.cnf /etc/mysql/conf.d/
COPY snowl_stripped.sql.gz /docker-entrypoint-initdb.d/

RUN echo "alias ll='ls -l --color=auto'" >>/root/.bashrc

VOLUME /mnt/host

ENTRYPOINT ["consul-init-mysql.sh"]

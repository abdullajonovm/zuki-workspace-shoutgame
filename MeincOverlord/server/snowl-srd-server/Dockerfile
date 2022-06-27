FROM scm.shoutgameplay.com:5000/srd-server:0.2.1
MAINTAINER Matt Pontius <mpontius@shout.tv>
LABEL description="Snowy Owl SRD Server"

COPY default.conf.ctmpl /etc/srd-server/
COPY servers.conf.ctmpl /etc/srd-server/

CMD ["-template", "/etc/srd-server/default.conf.ctmpl:/etc/srd-server/default.conf", \
     "-template", "/etc/srd-server/servers.conf.ctmpl:/etc/srd-server/servers.conf"]

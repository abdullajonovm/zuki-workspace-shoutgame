FROM scm.shoutgameplay.com:5000/wds:1.6
MAINTAINER Matt Pontius <mpontius@shout.tv>
LABEL description="Snowy Owl Web Server"

COPY web.nginx.conf.ctmpl /dclone/etc/nginx/sites-available/

RUN  cd /dclone/etc/nginx/sites-available \
     && rm shout_wds_cache shout_wds_origin shout_wms_origin \
     && cd /etc/nginx/sites-enabled \
     && ln -s ../sites-available/web.nginx.conf


COPY www/ /dclone/var/www/

RUN  mkdir /dclone/var/www/srd
COPY srd0.json.ctmpl /dclone/var/www/srd/

RUN  find /dclone/var/www -not -user nginx -exec chown nginx:nginx {} +

CMD ["-template", "/var/www/srd/srd0.json.ctmpl:/var/www/srd/srd0.json", \
     "-template", "/etc/nginx/sites-available/web.nginx.conf.ctmpl:/etc/nginx/sites-available/web.nginx.conf"]

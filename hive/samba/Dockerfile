ARG baseimage=meldsun/samba:24.12-develop-109fe34
FROM $baseimage
RUN echo "latest" > /version.txt

ENTRYPOINT ["samba-entry.sh"]


USER root

ADD samba.sh /opt/samba/bin/samba.sh
RUN chmod +x /opt/samba/bin//samba.sh
ENTRYPOINT ["/opt/samba/bin/samba.sh"]

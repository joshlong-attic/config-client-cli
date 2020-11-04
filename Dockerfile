FROM ubuntu
COPY target/config-client /config-client
RUN mkdir -p /config
CMD ["/config-client"]


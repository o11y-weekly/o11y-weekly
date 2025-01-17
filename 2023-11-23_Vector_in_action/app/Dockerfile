FROM timberio/vector:0.34.0-distroless-static AS vector

FROM alpine:3.18.4

# coreutils: nanoseconds %N date GNU support
RUN apk add --no-cache \
    coreutils \ 
    supervisor

ARG APPLICATION_USER=localuser
RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER
USER 1000
WORKDIR /workspace/

ENV LOG_BASE_PATH=/workspace/app/log/

RUN mkdir -p /workspace/app/ /workspace/app/log /workspace/vector /workspace/vector/data /workspace/vector/config

COPY --chown=1000:1000 --from=vector /usr/local/bin/vector /workspace/vector
COPY --chown=1000:1000 ["badlog.sh", "log.sh", "app.sh", "/workspace/app/"]

COPY --chown=1000:1000 ./supervisor/supervisord.conf /workspace/app/supervisord.conf
COPY --chown=1000:1000 ./supervisor/supervisor.d/ /workspace/app/supervisor.d/

WORKDIR /workspace/app
CMD ["/usr/bin/supervisord", "-n"]

VOLUME [ "/workspace/vector/data" ]
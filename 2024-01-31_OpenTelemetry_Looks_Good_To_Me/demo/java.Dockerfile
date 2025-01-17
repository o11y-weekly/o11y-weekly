ARG JDK_IMAGE
ARG DISTRO
ARG OTEL_CONTRIB_COL

FROM ${JDK_IMAGE} AS jdk
# required for strip-debug to work
RUN apk add --no-cache binutils
RUN $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /jre-lightweight

FROM ${JDK_IMAGE} AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:resolve dependency:go-offline -B 

COPY src ./src
RUN ./mvnw -o package

FROM otel/opentelemetry-collector-contrib:${OTEL_CONTRIB_COL} AS otelcontribcol
FROM ${DISTRO}

RUN apk add --no-cache supervisor

ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=jdk /jre-lightweight $JAVA_HOME

ARG APPLICATION_USER=appuser
ARG OTEL_JAVA_AGENT_VERSION

RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER

RUN mkdir -p /etc/otelcol-contrib && chown 1000:1000 /etc/otelcol-contrib

USER 1000
WORKDIR /app

RUN mkdir -p /app/log /app/logstate/app /app/logstate/agent

ADD --chown=1000:1000 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/${OTEL_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar ./opentelemetry-javaagent.jar
ADD --chown=1000:1000 https://github.com/glowroot/glowroot/releases/download/v0.14.0/glowroot-0.14.0-dist.zip ./glowroot-javaagent.zip

RUN unzip ./glowroot-javaagent.zip && rm ./glowroot-javaagent.zip

COPY --chown=1000:1000 ./glowroot/admin.json /app/glowroot/admin.json
COPY --chown=1000:1000 --from=otelcontribcol /otelcol-contrib /app/otelcol-contrib

COPY --chown=1000:1000 ./supervisord.conf /app/supervisord.conf
COPY --chown=1000:1000 ./supervisor.d/ /app/supervisor.d/

COPY --chown=1000:1000 --from=build /app/target/main.jar /app/main.jar

EXPOSE 8080
CMD ["/usr/bin/supervisord", "-n"]
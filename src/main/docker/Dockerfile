FROM openjdk:8-jre-alpine

# Duplicated as FROM resets arg scope
ARG BASE_IMAGE_VERSION
ARG PROJECT_BUILD_TIMESTAMP
ARG PROJECT_VERSION
ARG PROJECT_ORGANISATION
ARG PROJECT_NAME
ARG PROJECT_DESCRIPTION

# set label as per OCI spec
# https://github.com/opencontainers/image-spec/blob/master/annotations.md
LABEL org.opencontainers.image.created="${PROJECT_BUILD_TIMESTAMP}"
LABEL org.opencontainers.image.version="${PROJECT_VERSION}"
LABEL org.opencontainers.image.vendor="${PROJECT_ORGANISATION}"
LABEL org.opencontainers.image.title="${PROJECT_NAME}"
LABEL org.opencontainers.image.description="${PROJECT_DESCRIPTION}"

ARG JAR_FILE

COPY target/${JAR_FILE} /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

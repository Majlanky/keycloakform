FROM quay.io/keycloak/keycloak:${keycloak.version} as builder

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
#ENV KC_FEATURES=token-exchange,impersonation
ENV KC_DB=mariadb

# identity services SPI
ADD --chown=keycloak:keycloak maven/libs/* /opt/keycloak/providers/

WORKDIR /opt/keycloak
# for demonstration purposes only, please make sure to use proper certificates in production instead
RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -keystore conf/server.keystore
RUN /opt/keycloak/bin/kc.sh build


FROM quay.io/keycloak/keycloak:${keycloak.version}
COPY --from=builder /opt/keycloak/ /opt/keycloak/

#ENV KC_HOSTNAME=localhost
ENV KC_LOG_LEVEL=INFO,CONSOLE:info

Create self-signed Certificates

```
mkdir -p ~/cp-ssl/certs && cd ~/cp-ssl/certs
```

# Create a local Certificate Authority
```
openssl req -x509 -new -nodes -days 3650 \
  -keyout ca.key -out ca.crt -subj "/CN=Local-CA"
```
# Create a server keypair for Kafka/Schema Registry (JKS)
```
keytool -genkeypair -alias kafka \
  -keystore kafka.server.keystore.jks \
  -storepass changeit -keypass changeit \
  -dname "CN=localhost" -keyalg RSA -keysize 2048 -validity 3650
```
# Create CSR and sign with our CA (include SAN for localhost)
```
keytool -certreq -alias kafka -keystore kafka.server.keystore.jks \
  -storepass changeit -file kafka.csr
```

```
printf "subjectAltName=DNS:localhost,IP:127.0.0.1\n" > san.cnf
```
```
openssl x509 -req -in kafka.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
  -out kafka.crt -days 3650 -extfile san.cnf
```

# Import CA + signed cert back into the keystore
```
keytool -importcert -alias CARoot -file ca.crt \
  -keystore kafka.server.keystore.jks -storepass changeit -noprompt
```
```
keytool -importcert -alias kafka -file kafka.crt \
  -keystore kafka.server.keystore.jks -storepass changeit -noprompt
```
# Create a truststore (for broker/registry/clients)
```
keytool -importcert -alias CARoot -file ca.crt \
  -keystore kafka.server.truststore.jks -storepass changeit -noprompt
```

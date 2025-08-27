Create self-signed Certificates

```
mkdir -p ~/cp-ssl/certs && cd ~/cp-ssl/certs
```

### Create a local Certificate Authority
```
openssl req -x509 -new -nodes -days 3650 \
  -keyout ca.key -out ca.crt -subj "/CN=Local-CA"
```
### Create a server keypair for Kafka/Schema Registry (JKS)
```
keytool -genkeypair -alias kafka \
  -keystore kafka.server.keystore.jks \
  -storepass changeit -keypass changeit \
  -dname "CN=localhost" -keyalg RSA -keysize 2048 -validity 3650
```
### Create CSR and sign with our CA (include SAN for localhost)
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

### Import CA + signed cert back into the keystore
```
keytool -importcert -alias CARoot -file ca.crt \
  -keystore kafka.server.keystore.jks -storepass changeit -noprompt
```
```
keytool -importcert -alias kafka -file kafka.crt \
  -keystore kafka.server.keystore.jks -storepass changeit -noprompt
```
### Create a truststore (for broker/registry/clients)
```
keytool -importcert -alias CARoot -file ca.crt \
  -keystore kafka.server.truststore.jks -storepass changeit -noprompt
```

### Start zookeeper plaintext, no SSL

```
cd "$CONFLUENT_HOME"
$CONFLUENT_HOME/bin/zookeeper-server-start $CONFLUENT_HOME/etc/kafka/zookeeper.properties
```

### Start Kafka broker over SSL

```
cd "$CONFLUENT_HOME"

$CONFLUENT_HOME/bin/kafka-server-start $CONFLUENT_HOME/etc/kafka/server.properties \
  --override listeners=SSL://:9093 \
  --override advertised.listeners=SSL://localhost:9093 \
  --override listener.security.protocol.map=SSL:SSL \
  --override security.inter.broker.protocol=SSL \
  --override ssl.keystore.location=$HOME/cp-ssl/certs/kafka.server.keystore.jks \
  --override ssl.keystore.password=changeit \
  --override ssl.key.password=changeit \
  --override ssl.truststore.location=$HOME/cp-ssl/certs/kafka.server.truststore.jks \
  --override ssl.truststore.password=changeit \
  --override ssl.client.auth=none

```

### Schema registry over SSL

```
mousepad ~/cp-ssl/schema-registry-ssl.properties
```

```
# ===== REST (HTTPS) =====
listeners=https://0.0.0.0:8081
ssl.truststore.location=/home/training/cp-ssl/certs/kafka.server.truststore.jks
ssl.truststore.password=changeit
ssl.keystore.location=/home/training/cp-ssl/certs/kafka.server.keystore.jks
ssl.keystore.password=changeit
ssl.key.password=changeit
# ssl.client.auth=true

# ===== Internal calls must match your listener scheme =====
inter.instance.protocol=https
# schema.registry.inter.instance.protocol=https   <-- if you add this, put it on its own line (no trailing comment)

# ===== Kafka store over SSL =====
kafkastore.bootstrap.servers=SSL://localhost:9093
kafkastore.security.protocol=SSL
kafkastore.ssl.truststore.location=/home/training/cp-ssl/certs/kafka.server.truststore.jks
kafkastore.ssl.truststore.password=changeit
kafkastore.ssl.keystore.location=/home/training/cp-ssl/certs/kafka.server.keystore.jks
kafkastore.ssl.keystore.password=changeit
kafkastore.ssl.key.password=changeit
```

start schema registry
```
$CONFLUENT_HOME/bin/schema-registry-start ~/cp-ssl/schema-registry-ssl.properties
```

test schema registry with ssl

```
curl -k https://localhost:8081/subjects
```

### Client config properties

```
mousepad ~/cp-ssl/client.properties
```

paste below
```
security.protocol=SSL
ssl.truststore.location=/home/$USER/cp-ssl/certs/kafka.server.truststore.jks
ssl.truststore.password=changeit
# If you later enable client auth on the broker (ssl.client.auth=true), add:
# ssl.keystore.location=/home/$USER/cp-ssl/certs/kafka.server.keystore.jks
# ssl.keystore.password=changeit
# ssl.key.password=changeit
```

### Topic Creation with SSL

```
cd "$CONFLUENT_HOME"
```

Create topic
```
bin/kafka-topics --bootstrap-server localhost:9093 \
  --command-config $HOME/cp-ssl/client.properties \
  --create --topic ssl-test --partitions 1 --replication-factor 1
```

# Produce
```
bin/kafka-console-producer --broker-list localhost:9093 \
  --producer.config $HOME/cp-ssl/client.properties \
  --topic ssl-test
```
# (type a few lines, Ctrl+C to stop)

# Consume
```
bin/kafka-console-consumer --bootstrap-server localhost:9093 \
  --consumer.config $HOME/cp-ssl/client.properties \
  --topic ssl-test --from-beginning
```


A minimal setup to understand mirror maker 2. Mirror maker helps to sync data between two datacenters.

This example uses docker compose.

create a directory for docker compose and config files.

```
cd ~
mkdir mm2
cd mm2
```

```
cat > docker-compose.yml <<'YAML'
version: "3.8"

networks:
  mm2-net:

services:
  zookeeper-src:
    image: confluentinc/cp-zookeeper:6.2.15
    hostname: zookeeper-src
    container_name: zookeeper-src
    networks: [mm2-net]
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka-src:
    image: confluentinc/cp-kafka:6.2.15
    hostname: kafka-src
    container_name: kafka-src
    depends_on: [zookeeper-src]
    networks: [mm2-net]
    ports:
      - "19092:19092"     # host access to source cluster (optional)
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper-src:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,PLAINTEXT_HOST://0.0.0.0:19092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-src:9092,PLAINTEXT_HOST://localhost:19092
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"

  zookeeper-dest:
    image: confluentinc/cp-zookeeper:6.2.15
    hostname: zookeeper-dest
    container_name: zookeeper-dest
    networks: [mm2-net]
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka-dest:
    image: confluentinc/cp-kafka:6.2.15
    hostname: kafka-dest
    container_name: kafka-dest
    depends_on: [zookeeper-dest]
    networks: [mm2-net]
    ports:
      - "29092:29092"     # host access to destination cluster (optional)
    environment:
      KAFKA_BROKER_ID: 2
      KAFKA_ZOOKEEPER_CONNECT: zookeeper-dest:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9093,PLAINTEXT_HOST://0.0.0.0:29092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-dest:9093,PLAINTEXT_HOST://localhost:29092
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"

  mm2:
    image: confluentinc/cp-kafka-connect:6.2.15
    hostname: mm2
    container_name: mm2
    depends_on: [kafka-src, kafka-dest]
    networks: [mm2-net]
    ports:
      - "8083:8083"
    environment:
      # Use DEST as the Connect "home" cluster for its internal topics
      CONNECT_BOOTSTRAP_SERVERS: kafka-dest:9093
      CONNECT_REST_ADVERTISED_HOST_NAME: mm2
      CONNECT_REST_PORT: 8083
      CONNECT_GROUP_ID: mm2-connect-cluster

      CONNECT_CONFIG_STORAGE_TOPIC: mm2-configs
      CONNECT_OFFSET_STORAGE_TOPIC: mm2-offsets
      CONNECT_STATUS_STORAGE_TOPIC: mm2-status
      CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR: 1
      CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR: 1
      CONNECT_STATUS_STORAGE_REPLICATION_FACTOR: 1

      # CONNECT_KEY_CONVERTER: org.apache.kafka.connect.storage.StringConverter
      # CONNECT_VALUE_CONVERTER: org.apache.kafka.connect.storage.StringConverter

      CONNECT_KEY_CONVERTER: org.apache.kafka.connect.converters.ByteArrayConverter
      CONNECT_VALUE_CONVERTER: org.apache.kafka.connect.converters.ByteArrayConverter
      CONNECT_HEADER_CONVERTER: org.apache.kafka.connect.converters.ByteArrayConverter

      CONNECT_INTERNAL_KEY_CONVERTER: org.apache.kafka.connect.json.JsonConverter
      CONNECT_INTERNAL_VALUE_CONVERTER: org.apache.kafka.connect.json.JsonConverter
      CONNECT_LOG4J_ROOT_LOGLEVEL: INFO

      # MM2 lives inside the image; this path exposes it
      CONNECT_PLUGIN_PATH: /usr/share/java,/usr/share/confluent-hub-components
YAML
```

now config files for connectors. 

```
mkdir -p connectors
```

Mirror Maker 2 setup.

set "source.cluster.alias": "src" in the connector, so mm2-demo-orders on source becomes src.mm2-demo-orders on destination
due to "replication.policy.class": "org.apache.kafka.connect.mirror.DefaultReplicationPolicy"
(DefaultReplicationPolicy ⇒ <source-alias>.<topic>)


```
cat > connectors/src-to-dest.json <<'JSON'
{
  "name": "mm2-src-to-dest",
  "config": {
    "connector.class": "org.apache.kafka.connect.mirror.MirrorSourceConnector",
    "tasks.max": "1",

    "source.cluster.alias": "src",
    "target.cluster.alias": "dest",
    "source.cluster.bootstrap.servers": "kafka-src:9092",
    "target.cluster.bootstrap.servers": "kafka-dest:9093",

    "replication.policy.class": "org.apache.kafka.connect.mirror.DefaultReplicationPolicy",
    "replication.factor": "1",

    "topics": "mm2-demo-.*",
    "refresh.topics.interval.seconds": "10",

    "sync.topic.configs.enabled": "true",
    "sync.topic.acls.enabled": "false"
  }
}
JSON
```

Offsets + Heartbeats (good practice)

```
cat > connectors/checkpoints.json <<'JSON'
{
  "name": "mm2-checkpoints",
  "config": {
    "connector.class": "org.apache.kafka.connect.mirror.MirrorCheckpointConnector",
    "tasks.max": "1",
    "source.cluster.alias": "src",
    "target.cluster.alias": "dest",
    "source.cluster.bootstrap.servers": "kafka-src:9092",
    "target.cluster.bootstrap.servers": "kafka-dest:9093",
    "emit.checkpoints.interval.seconds": "10",
    "replication.policy.class": "org.apache.kafka.connect.mirror.DefaultReplicationPolicy",
    "groups": ".*"
  }
}
JSON
```

Heartbeats

```
cat > connectors/heartbeats.json <<'JSON'
{
  "name": "mm2-heartbeats",
  "config": {
    "connector.class": "org.apache.kafka.connect.mirror.MirrorHeartbeatConnector",
    "tasks.max": "1",
    "source.cluster.alias": "src",
    "target.cluster.alias": "dest",
    "source.cluster.bootstrap.servers": "kafka-src:9092",
    "target.cluster.bootstrap.servers": "kafka-dest:9093",
    "heartbeats.topic.replication.factor": "1"
  }
}
JSON
```

now run confluent brokers cluster

```
docker compose up
```

Open new tab

```
cd ~/mm2
```

check connector is up

```
curl -s localhost:8083/ | jq .
```

Deploy all the connectors

```
curl -s -X POST -H "Content-Type: application/json" \
  --data @connectors/src-to-dest.json \
  http://localhost:8083/connectors
```

checkpoints and heartbeats are optional

```
curl -s -X POST -H "Content-Type: application/json" \
  --data @connectors/checkpoints.json \
  http://localhost:8083/connectors
```

```
curl -s -X POST -H "Content-Type: application/json" \
  --data @connectors/heartbeats.json \
  http://localhost:8083/connectors
```

Ensure all 3 connectors are working..

```
curl -s http://localhost:8083/connectors | jq .
```

Demo: produce on SOURCE, consume the mirrored topic on DEST

```
docker exec -it kafka-src bash -lc \
  "kafka-topics --bootstrap-server kafka-src:9092 --create --topic mm2-demo-orders --partitions 1 --replication-factor 1 || true"
```

Produce a few records
``` 
docker exec -it kafka-src bash -lc \
  "bash -c 'printf \"order-1\norder-2\norder-3\n\" | kafka-console-producer --bootstrap-server kafka-src:9092 --topic mm2-demo-orders'"
```

``` 
docker exec -it kafka-src bash -lc \
  "kafka-console-producer --bootstrap-server kafka-src:9092 --topic mm2-demo-orders"
```

Consume on DEST: with the default MM2 naming policy the mirrored topic becomes src.<topic>:

```
docker exec -it kafka-dest bash -lc \
  "kafka-console-consumer --bootstrap-server kafka-dest:9093 --topic src.mm2-demo-orders --from-beginning --timeout-ms 5000"
```

```
docker exec -it kafka-dest bash -lc \
  "kafka-console-consumer --bootstrap-server kafka-dest:9093 \
   --topic src.mm2-demo-orders --from-beginning \
   --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer"
```
 
List connectors & their status:

```
curl -s http://localhost:8083/connectors | jq .
```

```
curl -s http://localhost:8083/connectors/mm2-src-to-dest/status | jq .
```

logs
```
docker compose logs -f mm2
```

Wanted to copy as is

```
curl -s -X PUT -H "Content-Type: application/json" \
  http://localhost:8083/connectors/mm2-src-to-dest-as-is/config \
  -d '{
    "connector.class": "org.apache.kafka.connect.mirror.MirrorSourceConnector",
    "tasks.max": "1",
    "source.cluster.alias": "src",
    "target.cluster.alias": "dest",
    "source.cluster.bootstrap.servers": "kafka-src:9092",
    "target.cluster.bootstrap.servers": "kafka-dest:9093",
    "replication.policy.class": "org.apache.kafka.connect.mirror.IdentityReplicationPolicy",
    "replication.factor": "1",
    "topics": "mm2-demo-.*",
    "refresh.topics.interval.seconds": "10",
    "sync.topic.configs.enabled": "true",
    "sync.topic.acls.enabled": "false"
  }'

```


Final shutdown
```
docker compose down -v
```


# DLQ, Not needed, part of s3 sink demo

DLQ means Dead Letter Queues, generally refered while   data processing, if any error with input records due to syntatical or semantical errors, consumer could not process the messages,
now those messages shall be delivered to error or DLQ topic, so that we have audits for bad/wrong messages which are not processed.


```
mkdir -p cp-6.1.15-dlq/{connectors,scripts}
```

```
cd cp-6.1.15-dlq
```

docker compose, broker, zookeeper, kafka connect

```
cat > docker-compose.yml <<'YAML'
version: "3.8"

networks:
  cp-net:

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:6.1.15
    hostname: zookeeper
    container_name: zookeeper
    networks: [cp-net]
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  broker:
    image: confluentinc/cp-kafka:6.1.15
    hostname: broker
    container_name: broker
    depends_on: [zookeeper]
    networks: [cp-net]
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      # Single listener (no port conflict)
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://broker:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"

  connect:
    image: confluentinc/cp-kafka-connect:6.1.15
    hostname: connect
    container_name: connect
    depends_on: [broker]
    networks: [cp-net]
    ports:
      - "8083:8083"
    environment:
      CONNECT_BOOTSTRAP_SERVERS: broker:9092
      CONNECT_REST_PORT: 8083
      CONNECT_GROUP_ID: "connect-cluster"
      CONNECT_CONFIG_STORAGE_TOPIC: _connect-configs
      CONNECT_OFFSET_STORAGE_TOPIC: _connect-offsets
      CONNECT_STATUS_STORAGE_TOPIC: _connect-status
      CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR: 1
      CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR: 1
      CONNECT_STATUS_STORAGE_REPLICATION_FACTOR: 1

      CONNECT_KEY_CONVERTER: "org.apache.kafka.connect.storage.StringConverter"
      CONNECT_VALUE_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_VALUE_CONVERTER_SCHEMAS_ENABLE: "false"

      CONNECT_INTERNAL_KEY_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_INTERNAL_VALUE_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"

      CONNECT_PLUGIN_PATH: "/usr/share/java,/usr/share/confluent-hub-components"
      CONNECT_REST_ADVERTISED_HOST_NAME: "connect"
      CONNECT_LOG4J_LOGGERS: "org.apache.kafka.connect.runtime.errors=DEBUG,org.apache.kafka.connect.runtime=INFO"

    volumes:
      - ./connectors:/connectors
YAML
```

```
docker compose up
```

Open new Tab

Create topics

```
cat > scripts/create-topics.sh <<'BASH'
#!/usr/bin/env bash
set -euo pipefail
docker exec broker kafka-topics --bootstrap-server broker:9092 --create --topic input-json --partitions 1 --replication-factor 1 || true
docker exec broker kafka-topics --bootstrap-server broker:9092 --create --topic dlq-json   --partitions 1 --replication-factor 1 || true
docker exec broker kafka-topics --bootstrap-server broker:9092 --create --topic sink-out   --partitions 1 --replication-factor 1 || true

echo "Topics:"
docker exec broker kafka-topics --bootstrap-server broker:9092 --list | grep -E 'input-json|dlq-json|sink-out' || true
BASH
```

permission to execute create topics, list topics 

```
chmod +x scripts/create-topics.sh
```

create topics 

```
./scripts/create-topics.sh
```


check connector running
```
curl -s http://localhost:8083/connectors | jq .
```

While in projects, connectors could be jdbc or s3 or so many others possible, here we have Mockconnector.
Mock connector useful for quick testing, especially testing, working with connector config. When mock connector throw error due to deserialization or convertors or transform errors, 
the message shall be sent to dead letter queue. else good messages are swolled, not published or not sinked.

```
cat > connectors/mock-sink-dlq.json <<'JSON'
{
  "name": "mock-sink-dlq",
  "config": {
    "connector.class": "org.apache.kafka.connect.tools.MockSinkConnector",
    "tasks.max": "1",

    "topics": "input-json",

    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",

    "errors.tolerance": "all",
    "errors.deadletterqueue.topic.name": "dlq-json",
    "errors.deadletterqueue.context.headers.enable": "true",
    "errors.log.enable": "true",
    "errors.log.include.messages": "true"
  }
}
JSON
```
Deploy mock sink connector

```
curl -s -X POST -H "Content-Type: application/json" \
  --data @connectors/mock-sink-dlq.json \
  http://localhost:8083/connectors | jq .
```

check the status

```
curl -s http://localhost:8083/connectors/mock-sink-dlq/status | jq .
```

Run the producer with good and bad jsons

```
docker exec -it broker kafka-console-producer --broker-list broker:9092 --topic input-json
```

copy line by line, note, we have bad json, that must got dead letter queue, good json are parsed by mock connector

```
{"id":1,"name":"ok-record"}
{"id":2,"name":"also-ok","active":true}
{broken_json: here]   <-- deliberately malformed, copy only  {broken_json: here] part
{"id":3,"name":"too-good-ok","active":false}
```

Check DLQ, NOTE, we must see exception stack trace, plus failed message, the exception shall be part of the headers, which we asked to print it below

```
docker exec -it broker kafka-console-consumer \
  --bootstrap-server broker:9092 \
  --topic dlq-json \
  --from-beginning \
  --property print.headers=true \
  --property print.key=true \
  --timeout-ms 5000
```

without print header, means, only wrong json now

```
docker exec -it broker kafka-console-consumer \
  --bootstrap-server broker:9092 \
  --topic dlq-json \
  --from-beginning \
  --property print.key=true \
  --timeout-ms 5000
```


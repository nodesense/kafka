# S3 Sink, DLQ

DLQ - Dead Letter Queue

Sinking data to S3, we use Minio, which is 100% compatible with S3 APIs 



S3 Sink connector

```
mkdir s3-sink-dlq
cd s3-sink-dlq
```

```
confluent-hub install --no-prompt confluentinc/kafka-connect-s3:latest
```

```
confluent local services stop
```

```
confluent local services start
```

```
cat > minio-compose.yml <<'YAML'
version: "3.8"
services:
  minio:
    image: quay.io/minio/minio:latest
    container_name: minio
    ports:
      - "9000:9000"   # S3 API
      - "9001:9001"   # Web console
    environment:
      MINIO_ROOT_USER: minio
      MINIO_ROOT_PASSWORD: minio12345
    command: server /data --console-address ":9001"
    volumes:
      - ./minio-data:/data

  minio-init:
    image: quay.io/minio/mc:latest
    container_name: minio-init
    depends_on: [minio]
    entrypoint: >
      /bin/sh -c "
      until (/usr/bin/mc alias set local http://minio:9000 minio minio12345) do sleep 1; done;
      /usr/bin/mc mb -p local/orders || true;
      /usr/bin/mc ls local;
      exit 0;
      "
YAML
```

```
docker compose minio-compose.yml up 
```

use -d if you want this containers running as background process


connector for S3 Sink, with minio

```
cat > s3-sink-orders.create.json <<'JSON'
{
  "name": "s3-sink-orders",
  "config": {
    "connector.class": "io.confluent.connect.s3.S3SinkConnector",
    "tasks.max": "1",

    "topics": "orders-json",

    "s3.bucket.name": "orders",
    "s3.region": "us-east-1",
    "store.url": "http://localhost:9000",
    "aws.access.key.id": "minio",
    "aws.secret.access.key": "minio12345",
    "s3.path.style.access": "true",

    "storage.class": "io.confluent.connect.s3.storage.S3Storage",
    "format.class": "io.confluent.connect.s3.format.json.JsonFormat",
    "flush.size": "3",

    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",

    "behavior.on.null.values": "ignore",

    "errors.tolerance": "all",
    "errors.deadletterqueue.topic.name": "dlq-orders-json",
    "errors.deadletterqueue.topic.replication.factor": "1",
    "errors.deadletterqueue.context.headers.enable": "true",
    "errors.log.enable": "true",
    "errors.log.include.messages": "true"
  }
}
JSON
```

Confluent Kafka has option to validate your connector settings before running them

note, below won't run connector, only validate, look into url

```
curl -s -X PUT \
  http://localhost:8083/connector-plugins/io.confluent.connect.s3.S3SinkConnector/config/validate \
  -H "Content-Type: application/json" \
  --data-binary @<(jq '.config' s3-sink-orders.create.json) | jq .
```

CREATE (POST /connectors) 

```
curl -s -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  --data-binary @s3-sink-orders.create.json | jq .
```

checks for plugins
```
curl -s localhost:8083/connector-plugins | jq '.[].class' | grep S3SinkConnector
```

connector status

```
curl -s localhost:8083/connectors/s3-sink-orders/status | jq .
```

send simple schemaless JSON values

```
kafka-console-producer --broker-list localhost:9092 --topic orders-json
```

send below one after another

there is two bad messages on with wrong syntax

```
{"order_id":"o-1001","price":100.0,"quantity":2}
{"order_id":"o-1002","price":149.5,"quantity":3}
{"order_id":"o-bad-1","price":"not-a-number"]
{"order_id":"o-1003","price":56.0,"quantity":4}
{"order_id":"o-bad-2}
```

Open another terminal

DLQ tail in another terminal (header will carry exception messages), print bad jsons with exceptions
```
kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic dlq-orders-json \
  --from-beginning \
  --property print.headers=true \
  --property print.key=true
```

Without header, print only bad json

```
kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic dlq-orders-json \
  --from-beginning \
  --property print.key=true
```




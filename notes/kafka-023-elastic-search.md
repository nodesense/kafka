# Elastic Search

```
sudo service elasticsearch start
```

check if that working

```
 curl 'http://localhost:9200/'
```


```
touch elasticsearch-sink.json

nano elasticsearch-sink.json
```

paste below
```
{
 "name": "elasticsearch-sink",
 "config": {
 "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
 "tasks.max": "1",
 "topics": "products",
 "name": "elasticsearch-sink",
 "connection.url": "http://localhost:9200",
 "type.name": "_doc",
  "key.converter": "org.apache.kafka.connect.storage.StringConverter",
  "value.converter": "io.confluent.connect.avro.AvroConverter",
  "value.converter.schema.registry.url": "http://localhost:8081"
     }
 }
 ```

```
confluent local load elasticsearch-sink -- -d elasticsearch-sink.json

confluent local status elasticsearch-sink
```

```
curl http://localhost:9200/products/_search | jq
```

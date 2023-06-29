# Single Record Transform Using Route Records

# Study

https://www.confluent.io/blog/kafka-connect-single-message-transformation-tutorial-with-examples/



```  
touch  mysql-invoice-transform-sink.json

nano  mysql-invoice-transform-sink.json
```


```
  {
   "name": "mysql-invoice-transform-sink",
   "config": {
     "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
     "topics": "invoices",
    
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter" : "io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url" : "http://localhost:8081",   
     
     "connection.url": "jdbc:mysql://localhost:3306/ecommerce?user=team&password=team1234",
     "auto.create": true,
     
	"transforms": "routeRecords",
	"transforms.routeRecords.type":  "org.apache.kafka.connect.transforms.RegexRouter",
	"transforms.routeRecords.regex": "(.*)",
	"transforms.routeRecords.replacement": "$1-test"
   }
 }
```
 
 
``` 
 confluent local load mysql-invoice-transform-sink -- -d  mysql-invoice-transform-sink.json

 confluent local status mysql-invoice-transform-sink
```
 
``` 
mysql -uteam -pteam1234
```

```
USE ecommerce;

select count(*) from `invoices-test`;
```



# Chained Single Record Transform Using Route Records

Tutorials [refer all series]

https://rmoff.net/2020/12/08/twelve-days-of-smt-day-1-insertfield-timestamp/

https://rmoff.net/2020/12/15/twelve-days-of-smt-day-6-insertfield-ii/



```  
touch  mysql-invoice-transform-insert-sink.json

nano  mysql-invoice-transform-insert-sink.json
```


```
  {
   "name": "mysql-invoice-transform-insert-sink",
   "config": {
     "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
     "topics": "invoices",
    
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter" : "io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url" : "http://localhost:8081",   
     
     "connection.url": "jdbc:mysql://localhost:3306/ecommerce?user=team&password=team1234",
     "auto.create": true,
     
	"transforms"                                : "routeRecords,insertStaticField1,insertPartition,insertOffset,insertTopic",
	
	"transforms.routeRecords.type":  "org.apache.kafka.connect.transforms.RegexRouter",
	"transforms.routeRecords.regex": "(.*)",
	"transforms.routeRecords.replacement": "$1-insert",
	
        "transforms.insertStaticField1.type"        : "org.apache.kafka.connect.transforms.InsertField$Value",
        "transforms.insertStaticField1.static.field": "sourceSystem",
        "transforms.insertStaticField1.static.value": "POS",

	"transforms.insertPartition.type"           : "org.apache.kafka.connect.transforms.InsertField$Value",
	"transforms.insertPartition.partition.field": "kafkaPartition",
	
	"transforms.insertOffset.type"              : "org.apache.kafka.connect.transforms.InsertField$Value",
	"transforms.insertOffset.offset.field"      : "kafkaOffset",
	
	"transforms.insertTopic.type"               : "org.apache.kafka.connect.transforms.InsertField$Value",
	"transforms.insertTopic.topic.field"        : "kafkaTopic"        
   }
 }
```
 
 
``` 
 confluent local load mysql-invoice-transform-insert-sink -- -d  mysql-invoice-transform-insert-sink.json

 confluent local status mysql-invoice-transform-insert-sink
```
 
``` 
mysql -uteam -pteam1234
```

```
USE ecommerce;

select count(*) from `invoices-insert`;
```


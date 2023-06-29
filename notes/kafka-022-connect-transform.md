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

## File Connectors, Source and Sink
 
 
 ### List avaiable connectors and status of the connectors

```
 confluent local services connect connector list
```


```
confluent local services connect connector status

```

```
curl http://localhost:8083/connectors   | jq
```

you need to replace connector name <name>, discussed later

```
curl http://localhost:8083/connectors/<name>/status | jq
```


Existing connectors list via plugin

```
curl -s http://localhost:8083/connector-plugins | jq '.[].class'
```
Existing connectors list via plugin that matches spool

```
curl -s http://localhost:8083/connector-plugins | jq '.[].class' | grep -i SpoolDir
```

Install SpoolDir connector

You need to press y (mean yes) option to accept path, follow as per instructor

```
confluent-hub  --no-prompt  install confluentinc/kafka-connect-spooldir:latest
```

```
confluent local services connect stop
```

```
confluent local services connect start
```

check those drivers installed
```
curl -s http://localhost:8083/connector-plugins | jq '.[].class' | grep -i SpoolDir
```

# Text file source connect

now we will read text files from input directories (detect new files) 

Here in is the input files, finished is output files, error for error files like parsing error
as soon as file read, published to kafka, the input files shall be moved to finished

```
mkdir -p /home/training/spool/texts/{in,finished,error}
```


```
mousepad ~/text-lines-source.json
```

paste below into mousepad, save the file

```json
{
  "name": "text-lines-source",
  "config": {
    "connector.class": "com.github.jcustenborder.kafka.connect.spooldir.SpoolDirLineDelimitedSourceConnector",
    "tasks.max": "1",

    "input.path": "/home/training/spool/texts/in",
    "input.file.pattern": ".*\\.(txt|log)",
    "finished.path": "/home/training/spool/texts/finished",
    "error.path": "/home/training/spool/texts/error",

    "topic": "text_lines",

    "value.converter": "org.apache.kafka.connect.storage.StringConverter",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
  }
}
```

```
curl -X POST -H "Content-Type: application/json" \
  --data @text-lines-source.json \
  http://localhost:8083/connectors
```

check connectors running
```
curl http://localhost:8083/connectors  | jq      
```

check status of the connector 
```
curl http://localhost:8083/connectors/text-lines-source/status | jq
```

Make sure the file exists (files shall be moved to finished or error if completed)

```
ls -l /home/training/spool/texts/in
ls -l /home/training/spool/texts/finished
ls -l /home/training/spool/texts/error
```

Consume produced records

```
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic text_lines --from-beginning --max-messages 10
```


generate file with content

```
echo -e "first line\nsecond line\nthird line" > /home/training/spool/texts/in/sample1.txt
```

```
echo -e "second file line 1" > /home/training/spool/texts/in/sample2.txt
```

# Working with CSV, output in json format

```
mkdir -p /home/training/spool/stocks/{in,finished,error}
```

Sample CSV (edit to your real data later)

```
echo -e "symbol,price,volume\nAAPL,187.65,200\nMSFT,338.50,150" > /home/training/spool/stocks/in/stocks.csv
```

csv source connnector.. 

```
mousepad stocks-csv-source.json
```

paste below into the mousepad or editor

```
{
  "name": "stocks-csv-source",
  "config": {
    "connector.class": "com.github.jcustenborder.kafka.connect.spooldir.SpoolDirCsvSourceConnector",
    "tasks.max": "1",

    "input.path": "/home/training/spool/stocks/in",
    "input.file.pattern": ".*\\.csv",
    "finished.path": "/home/training/spool/stocks/finished",
    "error.path": "/home/training/spool/stocks/error",
    "halt.on.error": "false",

    "topic": "stocks",

    "csv.first.row.as.header": "true",
 
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "true",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "true",


    "key.schema": "{\"name\":\"stocks.Key\",\"type\":\"STRUCT\",\"isOptional\":false,\"fieldSchemas\":{\"symbol\":{\"type\":\"STRING\",\"isOptional\":false}}}",
    "value.schema": "{\"name\":\"stocks.Value\",\"type\":\"STRUCT\",\"isOptional\":false,\"fieldSchemas\":{\"symbol\":{\"type\":\"STRING\",\"isOptional\":false},\"price\":{\"type\":\"FLOAT64\",\"isOptional\":false},\"volume\":{\"type\":\"INT64\",\"isOptional\":false}}}",

    "errors.tolerance": "all",
    "errors.deadletterqueue.topic.name": "stocks_dlq",
    "errors.deadletterqueue.context.headers.enable": "true",
    "errors.log.enable": "true",
    "errors.log.include.messages": "true",
    "errors.deadletterqueue.topic.replication.factor": "1"


  }
}
```

Load connector

```
curl -X POST -H "Content-Type: application/json" \
  --data @stocks-csv-source.json \
  http://localhost:8083/connectors
```


check connectors running
```
curl http://localhost:8083/connectors  | jq      
```

check status of the connector 
```
curl http://localhost:8083/connectors/stocks-csv-source/status | jq
```


Generate input file in wrong csv, missing column, let it go to error directory

```
cat > /home/training/spool/stocks/in/bad_missing_col.csv <<'CSV'
symbol,price,volume
AAPL
MSFT,338.50
CSV
```

column shift error

```
cat > /home/training/spool/stocks/in/bad_thousands.csv <<'CSV'
symbol,price,volume
AAPL,1,187.65,200
CSV
```

# work setup
Use Home Directory in Linux

open terminal 

```
cd ~
```

File Connector, File Source connector
    input file, read from teh file stocks.csv, watch the file change,
    publish to kafka topic called stocks
 
Create the file 


```
touch stocks.csv
```
 
 

Load the source connector / run the connector

```
touch stock-file-source.json

nano stock-file-source.json
```

and below content  into nano

```

{
 "name": "stock-file-source",
 "config": {
     "connector.class": "FileStreamSource",
     "tasks.max": "1",
    "file": "/home/ubuntu/stocks.csv",
    "topic": "stocks"
     }
 }
```

Ctrl + O - to save the content

if it is prompting to write content,  Hit Enter key

Ctrl + X - to quit the nano editor


Use cat command to check content

```
cat stock-file-source.json
```


```
confluent local load stock-file-source -- -d stock-file-source.json

```

Check whether connector is running or not

```
confluent local status connectors
```

check specific connector status 

```
confluent local status stock-file-source

```

start consumer on stocks topic on separate linux shell..

``` 
kafka-console-consumer --bootstrap-server localhost:9092 --topic  stocks   --from-beginning


```

Put some data into csv file

```
echo "1234,10" >> stocks.csv

echo "1235,20" >> stocks.csv

echo "1236,30" >> stocks.csv


cat stocks.csv
```


to unload kafka connector running? 

```
confluent local unload stock-file-source
```



# File Sink connector


Ensure simpleproducer.java topic should be greetings

```
public class SimpleProducer {

    public static String TOPIC = "greetings";

}
```

```
touch greetings.txt


touch greetings-file-sink.json

nano greetings-file-sink.json

```

paste below content

```
{
 "name": "greetings-file-sink",
 "config": {
     "connector.class": "FileStreamSink",
     "tasks.max": "1",
    "file": "/home/ubuntu/greetings.txt",
    "topics": "greetings",
"key.converter": "org.apache.kafka.connect.storage.StringConverter",
"value.converter": "org.apache.kafka.connect.storage.StringConverter"
     }
 }
```
 
 Ctrl + O - to save the content

if it is prompting to write content, Hit Enter key

Ctrl + X - to quit the nano editor

Use cat command to check content

```
cat greetings-file-sink.json

```
 
## Done

```
confluent local load greetings-file-sink -- -d greetings-file-sink.json
confluent local status greetings-file-sink
```

Run the SimpleProducer.java


```
cat greetings.txt
```


```
confluent local  unload greetings-file-sink

```


# Invoices to file sink



## One last example Avro and file sink

```
touch invoices.txt


touch invoices-file-sink.json

nano invoices-file-sink.json

```

paste below content



 

```
{
 "name": "invoices-file-sink",
 "config": {
     "connector.class": "FileStreamSink",
     "tasks.max": "1",
    "file": "/home/ubuntu/invoices.txt",
    "topics": "invoices",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url": "http://localhost:8081"
     }
 }
```


```
confluent local load invoices-file-sink -- -d invoices-file-sink.json

confluent local status invoices-file-sink

```


Ctrl + O - to save the content

if it is prompting to write content,  Hit Enter key

Ctrl + X - to quit the nano editor


Use cat command to check content

```
cat invoices-file-sink.json
```
 

## DONE
 

```
Now run the InvoiceProducer.java that pblish to invoices topics
```
 
 Do this command periodically
 
``` 
cat invoices.txt
```



### ensure connectors unloaded to save memory if run in constrained environment

```
confluent local  unload invoices-file-sink
```
 
 

# Stock Order to file sink



## One last example Avro and file sink

```
touch stock-orders.txt


touch stock-orders-file-sink.json

nano stock-orders-file-sink.json

```

paste below content



 

```
{
 "name": "stock-orders-file-sink",
 "config": {
     "connector.class": "FileStreamSink",
     "tasks.max": "1",
    "file": "/home/ubuntu/stock-orders.txt",
    "topics": "stock-orders",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url": "http://localhost:8081"
     }
 }
```


```
confluent local load stock-orders-file-sink -- -d stock-orders-file-sink.json

confluent local status stock-orders-file-sink

```


Ctrl + O - to save the content

if it is prompting to write content,  Hit Enter key

Ctrl + X - to quit the nano editor


Use cat command to check content

```
cat stock-orders-file-sink.json
```
 

## DONE
 

```
Now run the avro-order-producer.python that pblish to stock-orders topics
```
 
 Do this command periodically
 
``` 
cat stock-orders.txt
```



### ensure connectors unloaded to save memory if run in constrained environment

```
confluent local  unload stock-orders-file-sink
```
 

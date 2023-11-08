
open a command prompt

```
    kafka-topics  --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test
```

```
    kafka-topics --list --bootstrap-server localhost:9092
```    
   
```    
    kafka-topics --describe  --bootstrap-server localhost:9092  --topic test
```


open a command prompt and run producer

notes: enter some text and press enter key, each line is consider as one message


```
kafka-console-producer --broker-list localhost:9092 --topic test
```

note: open 4th Command Prompt

listen for the messages published/latest

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic test
```

Ctrl + C to exit 

get call the messages from beginging and then for new messages

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning
```



from a specific partition only

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic test --partition 0 --from-beginning
```

from a specific partition, from a specific offset onwards only

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic test --partition 0 --offset 4
```

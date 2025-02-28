Open command prompt to start zookeeper

```
%KAFKA_HOME%\bin\windows\zookeeper-server-start %KAFKA_HOME%\config\zookeeper.properties
```

 ZooKeeper runs on 0.0.0.0/0.0.0.0:2181


Open second command prompt, to start kafka broker


```
%KAFKA_HOME%\bin\windows\kafka-server-start %KAFKA_HOME%\config\server.properties
```

Kafka runs on  9092

----




create a file zookeeper.bat in Desktop, paste below

```
%KAFKA_HOME%\bin\windows\zookeeper-server-start %KAFKA_HOME%\etc\kafka\zookeeper.properties
```


Double click and run zookeeper.bat 


create a file broker-0.bat in Desktop, pate below

```
%KAFKA_HOME%\bin\windows\kafka-server-start %KAFKA_HOME%\etc\kafka\server.properties
```

Double click and run broker-0.bat


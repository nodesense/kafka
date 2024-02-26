### ANZ Start


start zookeeper 


```
$KAFKA_HOME/bin/zookeeper-server-start $KAFKA_HOME/etc/kafka/zookeeper.properties 
```


Start Broker 0 with default configuration as is 
```
$KAFKA_HOME/bin/kafka-server-start $KAFKA_HOME/etc/kafka/server.properties
```

patch below properties with command line override

```
broker.id=0
listeners=PLAINTEXT://:9092
log.dirs=/tmp/kafka-logs

```

Start Broker 1 with  override the properties using command line 

```
$KAFKA_HOME/bin/kafka-server-start $KAFKA_HOME/etc/kafka/server.properties \
--override broker.id=1 \
--override listeners=PLAINTEXT://:9093 \
--override log.dirs=/tmp/kafka-logs-1 \
--override confluent.metadata.server.listeners=http://0.0.0.0:8091
```

Start Broker 2 with override the properties using command line 

```
$KAFKA_HOME/bin/kafka-server-start $KAFKA_HOME/etc/kafka/server.properties \
--override broker.id=2 \
--override listeners=PLAINTEXT://:9094 \
--override log.dirs=/tmp/kafka-logs-2 \
--override confluent.metadata.server.listeners=http://0.0.0.0:8092
```


Start Broker 3 with override the properties using command line 

```
$KAFKA_HOME/bin/kafka-server-start $KAFKA_HOME/etc/kafka/server.properties \
--override broker.id=3 \
--override listeners=PLAINTEXT://:9095 \
--override log.dirs=/tmp/kafka-logs-3 \
--override confluent.metadata.server.listeners=http://0.0.0.0:8093
```

### ANZ End


start zookeeper 


```
$KAFKA_HOME/bin/zookeeper-server-start $KAFKA_HOME/etc/kafka/zookeeper.properties 
```


Start Broker 0 with default configuration as is 
```
$KAFKA_HOME/bin/kafka-server-start $KAFKA_HOME/etc/kafka/server.properties
```

patch below properties with command line override

```
broker.id=0
listeners=PLAINTEXT://:9092
log.dirs=/tmp/kafka-logs

```

Start Broker 1 with  override the properties using command line 

```
$KAFKA_HOME/bin/kafka-server-start $KAFKA_HOME/etc/kafka/server.properties \
--override broker.id=1 \
--override listeners=PLAINTEXT://:9093 \
--override log.dirs=/tmp/kafka-logs-1 \
--override confluent.metadata.server.listeners=http://0.0.0.0:8091
```

Start Broker 2 with override the properties using command line 

```
$KAFKA_HOME/bin/kafka-server-start $KAFKA_HOME/etc/kafka/server.properties \
--override broker.id=2 \
--override listeners=PLAINTEXT://:9094 \
--override log.dirs=/tmp/kafka-logs-2 \
--override confluent.metadata.server.listeners=http://0.0.0.0:8092
```


Start Broker 3 with override the properties using command line 

```
$KAFKA_HOME/bin/kafka-server-start $KAFKA_HOME/etc/kafka/server.properties \
--override broker.id=3 \
--override listeners=PLAINTEXT://:9095 \
--override log.dirs=/tmp/kafka-logs-3 \
--override confluent.metadata.server.listeners=http://0.0.0.0:8093
```


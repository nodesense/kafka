**KRaft Controller Quorum**: Kafka uses a Raft-based quorum (KRaft) where brokers communicate with each other for leader election, metadata management, and failover. In a KRaft cluster, one or more brokers are designated as controller nodes that manage metadata.

**Controller Nodes**: A subset of Kafka brokers are designated as controllers to form the Raft quorum. These controllers manage metadata (e.g., topics, partitions) and broker state.
**Bootstrap Mechanism**: Brokers are configured with the controller.quorum.voters setting, which provides the initial information to establish the Raft quorum and help brokers discover each other.
**Raft-based Leader Election**: The KRaft protocol handles leader election and ensures all brokers are aware of each other’s roles without the need for an external coordination service like ZooKeeper.


```
mkdir -p /tmp/kraft/bk1 /tmp/kraft/bk2 /tmp/kraft/bk2 
```

```
nano /tmp/kraft/bk1/broker1.properties
```

Paste, Ctrl + O, Ctrl + X 

```
# Broker ID and roles
node.id=1001
process.roles=broker,controller

# Controller quorum configuration
controller.quorum.voters=1001@localhost:9093,1002@localhost:9094,1003@localhost:9095

# Listener configuration
listeners=PLAINTEXT://localhost:9092,CONTROLLER://localhost:9093
listener.security.protocol.map=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
inter.broker.listener.name=PLAINTEXT

# Log directory for data storage
log.dirs=/tmp/kraft-combined-logs-1001

# KRaft-specific configurations
offsets.topic.replication.factor=3
transaction.state.log.replication.factor=3
transaction.state.log.min.isr=2

# Miscellaneous configurations
num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
```


```
nano /tmp/kraft/bk2/broker2.properties
```

```
node.id=1002
process.roles=broker,controller
controller.quorum.voters=1001@localhost:9093,1002@localhost:9094,1003@localhost:9095
listeners=PLAINTEXT://localhost:9096,CONTROLLER://localhost:9094
listener.security.protocol.map=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
inter.broker.listener.name=PLAINTEXT
log.dirs=/tmp/kraft-combined-logs-1002
offsets.topic.replication.factor=3
transaction.state.log.replication.factor=3
transaction.state.log.min.isr=2
num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
```


```
nano /tmp/kraft/bk3/broker3.properties
```

```
node.id=1003
process.roles=broker,controller
controller.quorum.voters=1001@localhost:9093,1002@localhost:9094,1003@localhost:9095
listeners=PLAINTEXT://localhost:9098,CONTROLLER://localhost:9095
listener.security.protocol.map=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
inter.broker.listener.name=PLAINTEXT
log.dirs=/tmp/kraft-combined-logs-1003
offsets.topic.replication.factor=3
transaction.state.log.replication.factor=3
transaction.state.log.min.isr=2
num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
```

# Start Broker 1
```
$KAFKA_HOME/bin/kafka-server-start /tmp/kraft/bk1/broker1.properties
```

# Start Broker 2
```
$KAFKA_HOME/bin/kafka-server-start /tmp/kraft/bk2/broker2.properties
```

# Start Broker 3
```
$KAFKA_HOME/bin/kafka-server-start /tmp/kraft/bk3/broker3.properties
```

create a topic

```
$KAFKA_HOME/bin/kafka-topics --create --topic messages --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092
```

producer

```
$KAFKA_HOME/bin/kafka-console-producer --topic messages --bootstrap-server localhost:9092

```

consumer

```
$KAFKA_HOME/bin/kafka-console-consumer --topic messages --bootstrap-server localhost:9092 --from-beginning

```

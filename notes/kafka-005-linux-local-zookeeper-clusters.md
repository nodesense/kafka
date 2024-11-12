# Zoo Keeper

 ZooKeeper cluster operates in a leader-follower architecture. In a ZooKeeper ensemble (cluster), there is only one leader at any given time, while the other nodes act as followers.

 ```
*Single Leader*: Out of the three nodes, only one node is elected as the leader. The leader handles all write requests to ensure strong consistency. Followers replicate data from the leader and handle read requests.

*Follower Nodes*: The remaining two nodes are followers. They sync with the leader to keep a consistent view of the data.

*Quorum*: ZooKeeper requires a quorum (majority) of nodes to agree on updates. In a 3-node cluster, a quorum of 2 nodes is needed to make changes. This means the cluster can tolerate the failure of one node while still functioning.

*Leader Election*: If the leader fails, the followers hold an election to determine a new leader. This ensures that the ZooKeeper cluster remains available as long as a quorum is maintained.
```

```
ZooKeeper ensemble, the leader node is responsible for handling all write requests and coordinating updates across the cluster,
while followers act as replicas and can serve read requests

```

create data directory and myid directory for each zookeeper instance. we create 3 instance in same host, the path are different

```
mkdir -p /tmp/zkc/zk1/data /tmp/zkc/zk2/data /tmp/zkc/zk3/data

echo "1" > /tmp/zkc/zk1/myid
echo "2" > /tmp/zkc/zk2/myid
echo "3" > /tmp/zkc/zk3/myid
```

## Node 1 (ZooKeeper ID 1)

2xxx/2888 ports used for internal communication between nodes (followers connting to leader)

3xxx/3888 ports used for leader election

```
nano /tmp/zkc/zk1/zookeeper.properties
```
```
dataDir=/tmp/zkc/zk1/data
dataLogDir=/tmp/zkc/zk1/log
clientPort=2181
server.1=127.0.0.1:2888:3888
server.2=127.0.0.1:2889:3889
server.3=127.0.0.1:2890:3890
4lw.commands.whitelist=*
initLimit=5
syncLimit=2
```

```
nano /tmp/zkc/zk2/zookeeper.properties
```

```
dataDir=/tmp/zkc/zk2/data
dataLogDir=/tmp/zkc/zk2/log
clientPort=2182
server.1=127.0.0.1:2888:3888
server.2=127.0.0.1:2889:3889
server.3=127.0.0.1:2890:3890
4lw.commands.whitelist=*
initLimit=5
syncLimit=2
```

```
nano /tmp/zkc/zk3/zookeeper.properties
```

```
dataDir=/tmp/zkc/zk3/data
dataLogDir=/tmp/zkc/zk3/log
clientPort=2183
server.1=127.0.0.1:2888:3888
server.2=127.0.0.1:2889:3889
server.3=127.0.0.1:2890:3890
4lw.commands.whitelist=*
initLimit=5
syncLimit=2
```

```
echo "1" > /tmp/zkc/zk1/data/myid
echo "2" > /tmp/zkc/zk2/data/myid
echo "3" > /tmp/zkc/zk3/data/myid
```

open a tab
```
$KAFKA_HOME/bin/zookeeper-server-start /tmp/zkc/zk1/zookeeper.properties
```

second tab
```
$KAFKA_HOME/bin/zookeeper-server-start /tmp/zkc/zk2/zookeeper.properties
```

third tab
```
$KAFKA_HOME/bin/zookeeper-server-start /tmp/zkc/zk3/zookeeper.properties
```

# Health check
```
echo "ruok" | nc 127.0.0.1 2181
echo "ruok" | nc 127.0.0.1 2182
echo "ruok" | nc 127.0.0.1 2183
```

# Metrics
```
echo "mntr" |  nc 127.0.0.1 2181
echo "mntr" |  nc 127.0.0.1 2182
echo "mntr" |  nc 127.0.0.1 2183
```

# Server statistics
```
echo "stat" |  nc 127.0.0.1 2181
echo "stat" |  nc 127.0.0.1 2182
echo "stat" |  nc 127.0.0.1 2183
```

# Configuration details
```
echo "conf" |  nc 127.0.0.1 2181
echo "conf" |  nc 127.0.0.1 2182
echo "conf" |  nc 127.0.0.1 2183  
```

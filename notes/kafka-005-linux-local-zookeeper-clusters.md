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
$KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/etc/kafka/zookeeper.properties \
  --override dataDir=/tmp/zkc/zk1/data \
  --override dataLogDir=/tmp/zkc/zk1/log \
  --override clientPort=2181 \
  --override server.1=127.0.0.1:2888:3888 \
  --override server.2=127.0.0.1:2889:3889 \
  --override server.3=127.0.0.1:2890:3890 \
  --override 4lw.commands.whitelist=* \
  --override myid=1

```

## Node 2 (ZooKeeper ID 2)
 
```
$KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/etc/kafka/zookeeper.properties \
  --override dataDir=/tmp/zkc/zk2/data \
  --override dataLogDir=/tmp/zkc/zk2/log \
  --override clientPort=2182 \
  --override server.1=127.0.0.1:2888:3888 \
  --override server.2=127.0.0.1:2889:3889 \
  --override server.3=127.0.0.1:2890:3890 \
  --override 4lw.commands.whitelist=* \
  --override myid=2
```

## Node 3 (ZooKeeper ID 3)
```
$KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/etc/kafka/zookeeper.properties \
  --override dataDir=/tmp/zkc/zk3/data \
  --override dataLogDir=/tmp/zkc/zk3/log \
  --override clientPort=2183 \
  --override server.1=127.0.0.1:2888:3888 \
  --override server.2=127.0.0.1:2889:3889 \
  --override server.3=127.0.0.1:2890:3890 \
  --override 4lw.commands.whitelist=* \
  --override myid=3
```


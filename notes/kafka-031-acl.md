# KAFKA ACL

Granting permission to user with Read/Write permissions

You need to work with multiple terminal, all the terminal must start with BASE initializaiton, as we have few properties files there

Always ensure that you set this properties on every terminal during hands-on
```
BASE="$HOME/cp-acl-sasl"
```

```
mkdir -p "$BASE/configs" "$BASE/logs"
```

JAAS for the broker (users live here)

```
cat > "$BASE/configs/server-jaas.conf" <<'EOF'
KafkaServer {
  org.apache.kafka.common.security.plain.PlainLoginModule required
  username="admin"
  password="admin-secret"
  user_admin="admin-secret"
  user_alice="alice-secret"
  user_bob="bob-secret";
};
EOF
```

Broker server.properties (ZK + SASL/PLAIN + ACLs)

AclAuthorizer is the ZK-mode authorizer for CP 6.
super.users=User:admin makes admin all-powerful (needed so the broker and your admin CLI can manage the cluster).
allow.everyone.if.no.acl.found=false = deny by default (recommended).

```
cat > "$BASE/configs/server.properties" <<EOF

# --- Basics (ZooKeeper mode) ---
broker.id=1
listeners=SASL_PLAINTEXT://:9092
advertised.listeners=SASL_PLAINTEXT://localhost:9092
log.dirs=/home/training/cp-acl-sasl/logs/kafka
zookeeper.connect=localhost:2181
default.replication.factor=1
min.insync.replicas=1

confluent.topic.replication.factor=1
confluent.balancer.topic.replication.factor=1
confluent.durability.topic.replication.factor=1
confluent.license.topic.replication.factor=1
confluent.tier.metadata.replication.factor=1
transaction.state.log.replication.factor=1
offsets.topic.replication.factor=1

num.partitions=1
  
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1

# --- SASL/PLAIN (brokers talk to each other and clients via SASL/PLAIN) ---
security.inter.broker.protocol=SASL_PLAINTEXT
sasl.enabled.mechanisms=PLAIN
sasl.mechanism.inter.broker.protocol=PLAIN

# --- Authorization (ZooKeeper AclAuthorizer) ---
authorizer.class.name=kafka.security.authorizer.AclAuthorizer
super.users=User:admin
allow.everyone.if.no.acl.found=false

 
EOF
```

Run Zookeeper (Open new terminal)

```
BASE="$HOME/cp-acl-sasl"
```

```
$CONFLUENT_HOME/bin/zookeeper-server-start \
  $CONFLUENT_HOME/etc/kafka/zookeeper.properties
```

Run Broker (Open New terminal)

```
BASE="$HOME/cp-acl-sasl"
```

```
export KAFKA_OPTS="-Djava.security.auth.login.config=$BASE/configs/server-jaas.conf"
$CONFLUENT_HOME/bin/kafka-server-start "$BASE/configs/server.properties"
```

Admin client Config (open new Terminal, for kafka-acls command)

```
BASE="$HOME/cp-acl-sasl"
```

```
cat > "$BASE/configs/admin-client.properties" <<'EOF'
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
  username="admin" password="admin-secret";
EOF
```


Create a demo topic (using admin)

```
$CONFLUENT_HOME/bin/kafka-topics --bootstrap-server localhost:9092 \
  --create --topic demo-acl --partitions 1 --replication-factor 1 \
  --command-config "$BASE/configs/admin-client.properties"
```

Grant minimal ACLs

-- Producer alice: WRITE + DESCRIBE on the topic (+ optional IdempotentWrite at cluster).

-- Consumer bob: READ on the topic and READ on the consumer group cg-app. (Both are required to consume.)

Producer ACLs (alice)

```
$CONFLUENT_HOME/bin/kafka-acls --bootstrap-server localhost:9092 \
  --command-config "$BASE/configs/admin-client.properties" \
  --add --allow-principal User:alice \
  --operation Write --operation Describe \
  --topic demo-acl
```

(Optional) Idempotent producers need cluster-level permission

```
$CONFLUENT_HOME/bin/kafka-acls --bootstrap-server localhost:9092 \
  --command-config "$BASE/configs/admin-client.properties" \
  --add --allow-principal User:alice \
  --cluster --operation IdempotentWrite
```

Consumer ACLs (bob): Topic READ + Group READ

```
$CONFLUENT_HOME/bin/kafka-acls --bootstrap-server localhost:9092 \
  --command-config "$BASE/configs/admin-client.properties" \
  --add --allow-principal User:bob \
  --operation Read --topic demo-acl
```

```
$CONFLUENT_HOME/bin/kafka-acls --bootstrap-server localhost:9092 \
  --command-config "$BASE/configs/admin-client.properties" \
  --add --allow-principal User:bob \
  --operation Read --group cg-app
```

Client settings files used for users.

user alice

```
cat > "$BASE/configs/client-alice.properties" <<'EOF'
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
  username="alice" password="alice-secret";
EOF
```

Run the producer with alice

```

$CONFLUENT_HOME/bin/kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic demo-acl \
  --producer.config "$BASE/configs/client-alice.properties"
```

Open new Tab for consumer


```
BASE="$HOME/cp-acl-sasl"
```

create consumer setting file for user bob to read

```
cat > "$BASE/configs/client-bob.properties" <<'EOF'
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
  username="bob" password="bob-secret";
EOF
```

Run consumer

```
$CONFLUENT_HOME/bin/kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic demo-acl --group cg-app --from-beginning \
  --consumer.config "$BASE/configs/client-bob.properties"
```

Inspect / rollback ACLs (do as per instructions)

open new tab


```
BASE="$HOME/cp-acl-sasl"
```

list all acls for a topic

```
$CONFLUENT_HOME/bin/kafka-acls --bootstrap-server localhost:9092 \
  --command-config "$BASE/configs/admin-client.properties" \
  --list --topic demo-acl
```

list all acls for group
```
$CONFLUENT_HOME/bin/kafka-acls --bootstrap-server localhost:9092 \
  --command-config "$BASE/configs/admin-client.properties" \
  --list --group cg-app
 ```

remove alice
```
$CONFLUENT_HOME/bin/kafka-acls --bootstrap-server localhost:9092 \
  --command-config "$BASE/configs/admin-client.properties" \
  --remove --allow-principal User:alice \
  --operation Write --operation Describe --topic demo-acl
```

remove bob
```
$CONFLUENT_HOME/bin/kafka-acls --bootstrap-server localhost:9092 \
  --command-config "$BASE/configs/admin-client.properties" \
  --remove --allow-principal User:bob \
  --operation Read --topic demo-acl
```

remove user from group

```
$CONFLUENT_HOME/bin/kafka-acls --bootstrap-server localhost:9092 \
  --command-config "$BASE/configs/admin-client.properties" \
  --remove --allow-principal User:bob \
  --operation Read --group cg-app
```




# Confluent dev env version 6 and 7. 

use confluent local current to know  directory
```
confluent local current
```

```
confluent local services status
confluent local services start
confluent local services stop
confluent local services restart
```

or 

```
confluent local services <service> start
confluent local services <service> stop
confluent local services <service> restart
confluent local services <service> status
```


*Service Names*

The available <service> values are:

zookeeper

kafka

schema-registry

ksql-server (sometimes just ksql)

connect

control-center

rest-proxy

```
confluent local services zookeeper start
confluent local services zookeeper status
confluent local services zookeeper restart
confluent local services zookeeper stop
```

# confluent development env version 5.x

start all services, works only on mac/unix/linux

```
confluent local start
```

if there is failure in above command, stop the services, delete the data, topics etc
```
confluent local destroy
```

then start

```
confluent local start
```

to stop all services without deleting
```
confluent local stop
```

to know where the data for zookeeper, brokers are stored,

```
confluent local current
```

to know whether kafka and other services are  running or not

```
confluent local status
```

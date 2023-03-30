# Docker Setup

```
sudo nano /etc/hosts
```

paste below

```
127.0.0.1 zookeeper
127.0.0.1 kafka-broker-0
127.0.0.1 kafka-broker-1
127.0.0.1 kafka-broker-2
127.0.0.1 kafka-broker-3
127.0.0.1 schema-registry
```

Ctrl + O then Enter to save file

Ctrl + X to exit


```

cd ~

mkdir docker

cd docker

wget https://raw.githubusercontent.com/nodesense/kafka/main/docker/.env

wget https://raw.githubusercontent.com/nodesense/kafka/main/docker/common.yml

wget https://raw.githubusercontent.com/nodesense/kafka/main/docker/mysql.yml


wget https://raw.githubusercontent.com/nodesense/kafka/main/docker/kafka_cluster.yml

wget https://raw.githubusercontent.com/nodesense/kafka/main/docker/postgres.yml
```

```

docker compose  -f common.yml -f mysql.yml up

docker compose  -f common.yml -f kafka_cluster.yml up

docker compose  -f common.yml -f postgres.yml up
```


## Docker  UI 

Open in browser

https://localhost:9443/#!/auth

username: admin

password: admin@123


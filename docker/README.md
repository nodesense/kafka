# Docker Setup


```

mkdir docker

cd docker

wget https://raw.githubusercontent.com/nodesense/kafka/main/docker/.env

wget https://raw.githubusercontent.com/nodesense/kafka/main/docker/common.yml

wget https://raw.githubusercontent.com/nodesense/kafka/main/docker/mysql.yml
```

```

docker compose up -f common.yml -f mysql.yml

```

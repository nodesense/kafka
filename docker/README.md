# Docker Setup


```

cd ~

mkdir docker

cd docker

wget https://raw.githubusercontent.com/nodesense/kafka/main/docker/.env

wget https://raw.githubusercontent.com/nodesense/kafka/main/docker/common.yml

wget https://raw.githubusercontent.com/nodesense/kafka/main/docker/mysql.yml
```

```

docker compose  -f common.yml -f mysql.yml up

```


## Docker  UI 

Open in browser

https://localhost:9443/#!/auth

username: admin

password: admin@123


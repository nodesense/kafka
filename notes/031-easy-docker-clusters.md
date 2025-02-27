# Install Miniconda

```
conda create -n kafkalab python=3.10.15  
```

```
conda activate kafkalab
```


# docker cluster with docker compose, grafana, prometheis using telemetry

reference article https://www.confluent.io/blog/how-to-use-kafka-docker-composer/

github https://github.com/sknop/kafka-docker-composer

below setup make it easy to create docker compose playground for confluent kafka

https://opentelemetry.io/

for good reference https://www.codesee.io/learning-center/opentelemetry-architecture


```
sudo usermod -aG docker $USER
newgrp docker
```

dont forgot newgrp docker

 ```
pip install jinja2
```
```
git clone https://github.com/sknop/kafka-docker-composer
cd kafka-docker-composer
```

pwd to know current directory

```
pwd
```

windows cmd, use setx for powershell
```
SET PWD=C:\lab\kafka-docker-composer
```

```
export PWD=yourdirector
```



```
python kafka_docker_composer.py --zookeepers 1 --brokers 3 --schema-registries 1   --prometheus

or with connect..

python kafka_docker_composer.py --zookeepers 1 --brokers 3 --schema-registries 1 --connect 1  --prometheus

```

```
docker compose -f docker-compose.yml -f postgres.yaml up
```

access grafana 

http://localhost:3000

username: admin

password: adminpass


access prometheus

http://localhost:9090


to be added into docker compose later

```

    kafka_manager:
        image: hlebalbau/kafka-manager:stable
        container_name: kakfa-manager
        restart: always
        ports:
        - "9000:9000"
        depends_on:
            - zookeeper-1
        environment:
            ZK_HOSTS: "zookeeper-1:2181"
            APPLICATION_SECRET: "random-secret"
            command: -Dpidfile.path=/dev/null
```

open kafka manager http://localhost:9000


# Portainer


https://docs.portainer.io/start/install-ce/server/docker/linux

```
docker volume create portainer_data
docker run -d -p 8000:8000 -p 9443:9443 --name portainer --restart=always -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer-ce:lts

```

check on browser  
https://localhost:9443

create user 


username: admin

password: admin@123456


```
Edit /etc/hosts file for development as kafka runs inside docker network 
```

```
sudo nano /etc/hosts
```

add below without modifing other settings..

```

```

# KAFKA Connect

```
cd kafka-docker-composer

wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz

tar xf mysql-connector-java-5.1.47.tar.gz
cp mysql-connector-java-5.1.47/*.jar ./volumes/connect-plugin-jars/confluentinc-kafka-connect-jdbc-10.7.3/lib
```

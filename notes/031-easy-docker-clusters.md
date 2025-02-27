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

```
python kafka_docker_composer.py --zookeepers 1 --brokers 3 --schema-registries 1   --prometheus
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

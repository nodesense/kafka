```
wget http://packages.confluent.io/archive/5.5/confluent-5.5.5-2.12.tar.gz
tar xf confluent-5.5.5-2.12.tar.gz

sudo mv confluent-5.5.5 /opt

chmod 777 /opt/confluent-5.5.5


wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz

tar xf mysql-connector-java-5.1.47.tar.gz
cp mysql-connector-java-5.1.47/*.jar /opt/confluent-5.5.5/share/java/kafka-connect-jdbc
```

```
echo "export KAFKA_HOME=/opt/confluent-5.5.5" >> ~/.bashrc
echo "export PATH=\$PATH:\$KAFKA_HOME/bin" >>  ~/.bashrc
```

```
mousepad .bashrc
```

comment out below lines

```

# export CONFLUENT_HOME=/home/lab-user/confluent-7.4.0
# export PATH=$PATH:$CONFLUENT_HOME/bin
```

Save the file

remove existing sim link
```
sudo unlink /bin/confluent
```


close and reopen the terminal

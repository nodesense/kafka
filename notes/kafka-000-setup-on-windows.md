




0. Download and install JDK 1.8, while installing ensure to check JAVA_HOME path [Adaptive JDK]  https://adoptium.net/temurin/archive/?version=8  Download x64 version, ensure to enable JAVA_HOME while installing.
1. Download  https://packages.confluent.io/archive/5.5/confluent-5.5.5-2.12.zip
2. Extract using 7zip/WinRar
3. Copy the confluent-5.5.5 folder to C:\confluent-5.5.5   [where you could see bin/etc on sub folders]
5. Ensure you have JAVA_HOME in environment variables
6. Ensure you have KAFKA_HOME set to C:\confluent-5.5.5
7. [optional] Ensure add C:\confluent-5.5.5\bin\windows to PATH env variables


open C:\confluent-5.5.5\bin\windows\kafka-run-class.bat in notepad++ (right click, edit with notepad++)

paste below line around line 45

```
rem class path patch for kafka on windows
if exist %BASE_DIR%\share\java\kafka\* (
call:concat %BASE_DIR%\share\java\kafka\*
)
```


Setup Batch files to start schmea-registry [I will rename the directory name later]

https://github.com/nodesense/kafka/tree/main/confluent-5.5.1/bin/windows


Setup Desktop Short cuts


https://github.com/nodesense/kafka/blob/main/Desktop/README.md

Then download zookeeper.bat, broker-0.bat files to desktop to start kafka..

### for Kafka Stream rockdb dll issue for Windows

Download C++ Redistributabe 2015 and install it

https://www.microsoft.com/en-us/download/details.aspx?id=48145

Download vc_redist.x64.exe and install it


and in the pom.xml,

```
 <dependency>
            <groupId>org.rocksdb</groupId>
            <artifactId>rocksdbjni</artifactId>
            <version>5.18.4</version>
        </dependency>
```


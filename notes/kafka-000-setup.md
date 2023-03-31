0. Download and install JDK 1.8, while installing ensure to check JAVA_HOME path [Adaptive JDK]  https://adoptopenjdk.net/
1. Download  http://packages.confluent.io/archive/5.5/confluent-5.5.5-2.12.tar.gz
2. Extract using 7zip/WinRar
3. Copy the confluent-5.5.5 folder to C:\confluent-5.5.5   [where you could see bin/etc on sub folders]
5. Ensure you have JAVA_HOME in environment variables
6. Ensure you have KAFKA_HOME set to C:\confluent-5.5.5
7. [optional] Ensure add C:\confluent-5.5.5\bin\windows to PATH env variables


open C:\confluent-5.5.1\bin\windows\kafka-run-class.bat in notepad++ (right click, edit with notepad++)

paste below line around line 45

```
rem class path patch for kafka on windows
if exist %BASE_DIR%\share\java\kafka\* (
call:concat %BASE_DIR%\share\java\kafka\*
)
```

Then download zookeeper, broker-0.bat files to desktop to start kafka..

# RabbitMQ vs Kafka

### Further Reading

   -- https://www.cloudamqp.com/blog/when-to-use-rabbitmq-or-apache-kafka.html
   
   -- https://www.openlogic.com/blog/kafka-vs-rabbitmq

###  Message handling (message replay)

#### Kafka

   --  The message queue in Kafka is persistent. 
   
   --  The data sent is stored until a specified retention period has passed either a period of time or a size limit.
   
   --  the message is not removed once it’s consumed. 
   
   --  the message can be replayed or consumed multiple times

#### RabbitMQ


-- messages are stored until a receiving application connects and receives a message off the queue

-- The client can either ack (acknowledge) the message when it receives it 

-- Ack or when the client has completely processed the message. 

-- once the message is acked, it’s removed from the queue.


#### Considerations

-- In Producer to Consumer ack is not possible in Kafka, This cannot be solved

-- Messages can be reprocessed at consumer more than once. Enable Exactly once

-- Enable Producer Transaction in Kafka for transactional message

-- Use Saga/Feedback topics to confirm message received/processed.

-- Use Outbox pattern to store the messages sent to Kafka in DB, mark the message deliveries, status update.

-- Ordered delivery, ensure to use partition to send transactional messages to same partition

-- Backpresser problems faced in RabbitMQ can be automatically solved in Kafka,
   as Kafka consumers uses Pull mechanism, where as RabbitMQ push messages to Consumer, can blow out consumer

-- Message routing features of RabbitMq cannot be solved in Kafka, instead use Tools like Apache NIFI, Apache Camel or Kafka Stream to stream data to specific topics based on filters. Example, create multiple topics for routing, use nifi/camel/kafka stream to route the messages, let consumers subscribe for topics.

-- Message priority features in RabbitMq cannot be solved in Kafka, workaround may be create multiple topics based on importants, put messages based on priority
     like  tasks_high_priority, tasks_low_priority

#### Performance/Scaling
    
--    More messages - add more brokers, more partitions, more consumers in consumer group
--    Fault Tolerance - replications 

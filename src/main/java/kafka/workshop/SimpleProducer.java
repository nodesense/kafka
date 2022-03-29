package kafka.workshop;

// SimpleProducer.java
// kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic greetings
// kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings  --from-beginning --property print.key=true --property print.timestamp=true


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import java.util.Properties;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

public class SimpleProducer {

    public static String TOPIC = "greetings";


    public static String[] greetingMessages = new String[] {
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",

    };


    public static void main(String[] args) throws  Exception {
        Properties props = new Properties();

        props.put(BOOTSTRAP_SERVERS_CONFIG, Settings.BOOTSTRAP_SERVERS); // broker address

        // acks = 0 - broker receive the message, message still in memory, not persisted, ack back to producer
            // FAST
            // Risk: if the broker fails writing to disk, message shall be lost
        // acks = 1 - broker receive the message, written to disk [persisted], ack back to producer
            // Medium, since Disk IO involved
            // Good: Message is received ,stored in HDD, even if the broker crash, still have message in HDD
            // Risk: If the system/hdd itself fails, no network, we will lose messages
        // acks = all - broker receive the message, write to disk [persisted], update replicas [persisted], ack back to producer
            // SLOW, since Disk IO on all replicas and network traffic between lead broker and replicas
            // Good: even if one system fails, other system has data, no data loss
        props.put(ACKS_CONFIG, "all"); // acknowledge level "0", "1", "all"

        // when producer send message to broker, if any failures, whether producer should retry or not
        props.put(RETRIES_CONFIG, 2); // how many retry when msg failed to send

        // producer.send(message);  // main thread, internally kafka sdk at producer collect the messages, won't send to broker immediate
        // Main Thread
        // Worker threads, is used to send message to broker, they collect the messages based of size/time

        // whatever first condition reached,
        // collect messages by max byte size, 16 KB, dispatch when it reaches 16 KB
        props.put(BATCH_SIZE_CONFIG, 16000); // bytes

        // collect the messages by max wait time, when 100 ms reached, dispatch the message
        props.put(LINGER_MS_CONFIG, 100); // milli second

        // Reserved memory, pre-alloted in bytes
        props.put(BUFFER_MEMORY_CONFIG, 33554432);

        // Key/Value
        // Key is string, converted to byte array [serialized data]
        // Value is string, converted to byte array [serialized data]
        props.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        // props.put("partitioner.class", CustomPartitioner.class);


        System.out.println("PRoducer Setup ");
        // Key as string, value as string
        Producer<String, String> producer = new KafkaProducer<>(props);


        int counter = 0;
        for (int i = 0 ; i < 5000; i++) {
            for (String message:greetingMessages) {
                // producer record, topic, key (null), value (message)
                // send message, not waiting for ack
                String key = "Message" + counter ;
                String value = counter + " " + message;

                ProducerRecord record = new ProducerRecord<>(TOPIC, key, value);

                // producer.send(record); // async, non-blocking, it won't wait for broker ack

                RecordMetadata metadata = (RecordMetadata) producer.send(record).get(); // sync, blocking

                System.out.printf("Greeting %d - %s sent\n", counter, message);
                System.out.println("Ack offset " + metadata.offset() + " partition " + metadata.partition());

                Thread.sleep(5000); // Demo only,
                counter++;
            }
        }

        producer.close();
    }

}

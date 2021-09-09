
package kafka.workshop.invoice;

import kafka.workshop.Settings;
import kafka.workshop.models.Invoice;
import kafka.workshop.models.Product;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

public class ProductConsumer {
    public static String TOPIC = "db_gk_products";

    public static void main(String[] args) {


        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, Settings.BOOTSTRAP_SERVERS);
        props.put(GROUP_ID_CONFIG, "db_gk_products-specific-consumer-example"); // offset, etc, TODO
        props.put(ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(SESSION_TIMEOUT_MS_CONFIG, "30000");

        //props.put(GROUP_ID_CONFIG, UUID.randomUUID().toString());
        // props.put(AUTO_OFFSET_RESET_CONFIG, "earliest");


        //FIXME: Issue with DateTime Instance while converting to Long to Instant, fix with speifiic datum writter
        
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("schema.registry.url", Settings.SCHEMA_REGISTRY);

        // <Key as string, Value as string>
        KafkaConsumer<String, Product> consumer = new KafkaConsumer<>(props);

        // Subscribe for topic(s)
        consumer.subscribe(singletonList(TOPIC));

        System.out.println("Consumer Starting!");

        while (true) {
            // poll (timeout value), wait for 1 second, get all the messages
            // <Key, Value>
            ConsumerRecords<String, Product> records = consumer.poll(ofSeconds(1));
            // if no messages
            if (records.count() == 0)
                continue;


            // Iterating over each record
            for (ConsumerRecord<String, Product> record : records) {

                System.out.printf("partition= %d, offset= %d, key= %s, value= %s\n",
                        record.partition(),
                        record.offset(),
                        record.key(),
                        record.value());


                GenericRecord genericRecord = record.value();

                Product product = (Product) SpecificData.get().deepCopy(Product.SCHEMA$, genericRecord);


                System.out.println("product price  " + product.getPrice());


            }
        }
    }
}

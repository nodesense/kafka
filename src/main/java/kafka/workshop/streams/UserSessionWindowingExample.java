package kafka.workshop.stream;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;

import java.io.IOException;
import java.util.Map;

// user-clicks
// kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic user-clicks
// kafka-console-producer --broker-list localhost:9092 --topic user-clicks --property "parse.key=true" --property "key.separator=;"
// JSON payloads
// user123;{"userId":"user123", "pageId":"page47", "regionId":"region79", "timestamp":1627800000000}

// Wrong payload: // user123;{"userId":"user123", "pageId":"page47", "regionId":"region79", "timestamp":1627800000000
// user123;{"userId":"user123", "pageId":"page48", "regionId":"region79", "timestamp":1627800000000}

class JsonSerde implements Serde<JsonNode> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No additional configuration needed
    }

    @Override
    public void close() {
        // No resources to close
    }

    @Override
    public Serializer<JsonNode> serializer() {
        return new JsonSerializer();
    }

    @Override
    public Deserializer<JsonNode> deserializer() {
        return new JsonDeserializer();
    }

    private class JsonSerializer implements Serializer<JsonNode> {

        @Override
        public byte[] serialize(String topic, JsonNode data) {
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (IOException e) {
                throw new RuntimeException("Error serializing JSON", e);
            }
        }
    }

    private class JsonDeserializer implements Deserializer<JsonNode> {

        @Override
        public JsonNode deserialize(String topic, byte[] data) {
            try {
                return objectMapper.readTree(data);
            } catch (IOException e) {
                throw new RuntimeException("Error deserializing JSON", e);
            }
        }
    }
}

public class UserSessionWindowingExample {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "user-session-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                LogAndContinueExceptionHandler.class);

        // Configure JSON serde
        final Serde<String> stringSerde = Serdes.String();
        final Serde<JsonNode> jsonSerde = new JsonSerde();

        StreamsBuilder builder = new StreamsBuilder();

        // Add exception handler
        //DeserializationExceptionHandler deserializationExceptionHandler = new LogAndContinueExceptionHandler();



        // Deserialize JSON payload
        KStream<String, JsonNode> inputStream = builder.stream("user-clicks",
                Consumed.with(stringSerde, jsonSerde));


        // Define session window
        KGroupedStream<String, JsonNode> groupedStream = inputStream.groupByKey();
        TimeWindows timeWindows = TimeWindows.of(5 * 60 * 1000); // 5-minute session window
        SessionWindows sessionWindows = SessionWindows.with(10 * 60 * 1000); // 10-minute inactivity gap
        KTable<Windowed<String>, Long> sessionCounts = groupedStream.windowedBy(sessionWindows)
                .count();

        // Print session counts
        sessionCounts.toStream().foreach((windowedUserId, count) ->
                System.out.println("User " + windowedUserId.key() +
                        ", session start: " + windowedUserId.window().start() +
                        ", session end: " + windowedUserId.window().end() +
                        ", clicks count: " + count)
        );

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}

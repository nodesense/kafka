package kafka.workshop;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

/*
kafka-topics --bootstrap-server localhost:9092 \
        --create --topic without-transactions --partitions 3 --replication-factor 1
*/

/*
kafka-topics --bootstrap-server localhost:9092 \
        --create --topic with-transactions --partitions 3 --replication-factor 1
*/

// Transactions uses __transaction_state topic internally


/*

Idempotence ensures the broker deduplicates retries from the same producer session
(same internal ProducerId/sequence). It does not dedupe across different producers or
across restarts unless you implement your own logic
(e.g., keyed upserts, compaction, or application-level dedup).

With enable.idempotence=true, Kafka enforces acks=all and retries>0;
we also set max.in.flight.requests.per.connection=5 to preserve ordering with retries.
 */

/*
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic without-transactions --from-beginning \
  --property print.key=true --property print.timestamp=true
 */

// Read below, we use isolation-level as read_commited, this is important
/*
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic with-transactions --from-beginning \
  --isolation-level read_committed \
  --property print.key=true --property print.timestamp=true

 */

// Listen, we have --isolation-level read_uncommitted , we can see aborted messages, not safe
/*
kafka-console-consumer --bootstrap-server localhost:9092 \
        --topic with-transactions --from-beginning \
        --isolation-level read_uncommitted \
        --property print.key=true --property print.timestamp=true
*/

// If -isolation-level, not specificed, which is default, isolation.level = read_uncommitted is default

public class TxIdempotentDemo {

    // Topics
    private static final String TOPIC_NO_TX = "without-transactions";
    private static final String TOPIC_TX    = "with-transactions";

    private static final String BOOTSTRAP = Settings.BOOTSTRAP_SERVERS;

    public static void main(String[] args) throws Exception {
        // Args: start counter, total messages, tx batch size, crash % (0-100)
        final int start = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        final int total = args.length > 1 ? Integer.parseInt(args[1]) : 30;
        final int txBatchSize = args.length > 2 ? Integer.parseInt(args[2]) : 5;
        final int crashPercent = args.length > 3 ? Integer.parseInt(args[3]) : 25;

        // Keep tx.id stable across runs to demonstrate recovery
        final String txId = System.getProperty("txid", "tx-demo-1");

        // ----- Common producer config (idempotent & safe)
        Properties common = new Properties();
        common.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP);
        common.put(ACKS_CONFIG, "all");
        common.put(ENABLE_IDEMPOTENCE_CONFIG, "true");
        common.put(RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        common.put(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5"); // keep <=5 with idempotence
        common.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        common.put(VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        // ----- Non-transactional (still idempotent)
        Properties plainProps = new Properties();
        plainProps.putAll(common);
        Producer<String, String> plain = new KafkaProducer<>(plainProps);

        // ----- Transactional (idempotence implied but we set it anyway)
        Properties txProps = new Properties();
        txProps.putAll(common);
        txProps.put(TRANSACTIONAL_ID_CONFIG, txId);
        KafkaProducer<String, String> tx = new KafkaProducer<>(txProps);

        System.out.println("Initializing transactions for tx.id = " + txId);
        tx.initTransactions();

        printTxnMetrics(tx);

        int sentInCurrentTxn = 0;
        boolean inTxn = false;

        try {
            for (int i = start; i < start + total; i++) {
                // 1) Send to NON-TRANSACTIONAL topic (sync)
                String noTxValue = "without-transaction-" + i;
                RecordMetadata m1 = plain.send(new ProducerRecord<>(TOPIC_NO_TX, null, noTxValue)).get();
                System.out.printf("[NO-TX] sent value=%s -> %s-%d@%d%n",
                        noTxValue, TOPIC_NO_TX, m1.partition(), m1.offset());

                // 2) Send to TRANSACTIONAL topic in batches
                if (!inTxn) {
                    tx.beginTransaction();
                    inTxn = true;
                    sentInCurrentTxn = 0;
                    System.out.println(">>> beginTransaction()");
                }

                String txValue = "with-transaction-" + i;
                RecordMetadata m2 = tx.send(new ProducerRecord<>(TOPIC_TX, null, txValue)).get();
                sentInCurrentTxn++;
                System.out.printf("[  TX ] staged value=%s -> %s-%d@%d (not visible until commit)%n",
                        txValue, TOPIC_TX, m2.partition(), m2.offset());

                // Randomly "crash" JUST BEFORE commit of a batch
                boolean batchEnd = (sentInCurrentTxn >= txBatchSize);
                if (batchEnd) {
                    if (ThreadLocalRandom.current().nextInt(100) < crashPercent) {
                        System.err.println("!!! Simulating crash BEFORE commit (in-flight txn will be aborted on next start).");
                        // hard crash: no finally/close hooks
                        Runtime.getRuntime().halt(1);
                    }
                    // Commit this batch
                    tx.commitTransaction();
                    inTxn = false;
                    System.out.println("<<< commitTransaction()");
                }

                Thread.sleep(5000);
            }

            // Commit any remainder if we finished cleanly
            if (inTxn) {
                tx.commitTransaction();
                System.out.println("<<< commitTransaction() [final]");
            }
        } catch (Exception e) {
            System.err.println("Exception occurred, aborting transaction: " + e);
            try {
                tx.abortTransaction();
                System.err.println("xxx abortTransaction()");
            } catch (Exception ignored) {}
            throw e;
        } finally {
            // If we hard-crashed via halt(1), these won't run (by design)
            try { plain.close(); } catch (Exception ignored) {}
            try { tx.close(); } catch (Exception ignored) {}
        }
    }

    private static void printTxnMetrics(KafkaProducer<String, String> tx) {
        System.out.println("--- transactional metrics (subset) ---");
        for (Map.Entry<MetricName, ? extends Metric> e : tx.metrics().entrySet()) {
            String name = e.getKey().name().toLowerCase();
            if (name.contains("transaction")) {
                System.out.printf("%s = %s%n", e.getKey(), e.getValue().metricValue());
            }
        }
        System.out.println("(Note: Kafka's Java API does NOT expose ProducerId/Epoch directly; you can log the transactional.id above.)");
        System.out.println("--------------------------------------");
    }
}

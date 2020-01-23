package ms.kafkatest.utils;

import java.time.Duration;
import java.util.*;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * <p>
 * An {@link org.springframework.batch.item.ItemReader} implementation for Apache Kafka.
 * Uses a {@link KafkaConsumer} to read records from a given topic.
 * Multiple partitions within the same topic can be assigned to this reader.
 * </p>
 *
 * <p>
 * Since {@link KafkaConsumer} is not thread-safe, this reader is not thead-safe.
 * </p>
 *
 * @author Mathieu Ouellet
 * @author Mahmoud Ben Hassine
 * @author Christian Allard
 * @author Jean-Francois Larouche
 * @author Maxime Gagne
 * @since 4.2
 */
public class KafkaRecordItemReader<T extends ConsumerRecord<?, ?>> extends AbstractItemStreamItemReader<T> {

    private static final String TOPIC_PARTITION_OFFSETS = "topic.partition.offsets";

    private static final long DEFAULT_POLL_TIMEOUT = 30L;

    private List<TopicPartition> topicPartitions;

    private Map<TopicPartition, Long> partitionOffsets;

    private KafkaConsumer kafkaConsumer;

    private Properties consumerProperties;

    private Iterator<T> consumerRecords;

    private Duration pollTimeout = Duration.ofSeconds(DEFAULT_POLL_TIMEOUT);

    private boolean saveState = true;

    private Deserializer<?> keyDeserializer;

    private Deserializer<?> valueDeserializer;

    /**
     * Create a new {@link KafkaRecordItemReader}.
     * <p><strong>{@code consumerProperties} must contain the following keys:
     * 'bootstrap.servers', 'group.id', 'key.deserializer' and 'value.deserializer' </strong></p>.
     *
     * @param consumerProperties properties of the consumer
     * @param topicName          name of the topic to read data from
     * @param partitions         list of partitions to read data from
     */
    public KafkaRecordItemReader(Properties consumerProperties, String topicName, Integer... partitions) {
        this(consumerProperties, topicName, Arrays.asList(partitions));
    }

    /**
     * Create a new {@link KafkaRecordItemReader}.
     * <p><strong>{@code consumerProperties} must contain the following keys:
     * 'bootstrap.servers', 'group.id', 'key.deserializer' and 'value.deserializer' </strong></p>.
     *
     * @param consumerProperties properties of the consumer
     * @param topicName          name of the topic to read data from
     * @param partitions         list of partitions to read data from
     */
    public KafkaRecordItemReader(Properties consumerProperties, String topicName, List<Integer> partitions) {
        Assert.notNull(consumerProperties, "Consumer properties must not be null");
        Assert.isTrue(consumerProperties.containsKey(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG),
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG + " property must be provided");
        Assert.isTrue(consumerProperties.containsKey(ConsumerConfig.GROUP_ID_CONFIG),
                ConsumerConfig.GROUP_ID_CONFIG + " property must be provided");
        Assert.isTrue(consumerProperties.containsKey(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG + " property must be provided");
        Assert.isTrue(consumerProperties.containsKey(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + " property must be provided");
        this.consumerProperties = consumerProperties;
        Assert.hasLength(topicName, "Topic name must not be null or empty");
        Assert.isTrue(!partitions.isEmpty(), "At least one partition must be provided");
        this.topicPartitions = new ArrayList<>();
        for (Integer partition : partitions) {
            this.topicPartitions.add(new TopicPartition(topicName, partition));
        }
    }

    /**
     * Set a timeout for the consumer topic polling duration. Default to 30 seconds.
     *
     * @param pollTimeout for the consumer poll operation
     */
    public void setPollTimeout(Duration pollTimeout) {
        Assert.notNull(pollTimeout, "pollTimeout must not be null");
        Assert.isTrue(!pollTimeout.isZero(), "pollTimeout must not be zero");
        Assert.isTrue(!pollTimeout.isNegative(), "pollTimeout must not be negative");
        this.pollTimeout = pollTimeout;
    }

    /**
     * Set the flag that determines whether to save internal data for
     * {@link ExecutionContext}. Only switch this to false if you don't want to
     * save any state from this stream, and you don't need it to be restartable.
     * Always set it to false if the reader is being used in a concurrent
     * environment.
     *
     * @param saveState flag value (default true).
     */
    public void setSaveState(boolean saveState) {
        this.saveState = saveState;
    }

    /**
     * The flag that determines whether to save internal state for restarts.
     *
     * @return true if the flag was set
     */
    public boolean isSaveState() {
        return this.saveState;
    }

    /**
     * Set a custom key deserializer.
     *
     * @param keyDeserializer custom key deserializer
     */
    public void setKeyDeserializer(Deserializer<?> keyDeserializer) {
        this.keyDeserializer = keyDeserializer;
    }

    /**
     * Set a custom value deserializer.
     *
     * @param valueDeserializer custom value deserializer
     */
    public void setValueDeserializer(Deserializer<?> valueDeserializer) {
        this.valueDeserializer = valueDeserializer;
    }

    @Override
    public void open(ExecutionContext executionContext) {
        this.kafkaConsumer = new KafkaConsumer<>(this.consumerProperties, keyDeserializer, valueDeserializer);
        this.partitionOffsets = new HashMap<>();
        if (this.saveState && executionContext.containsKey(TOPIC_PARTITION_OFFSETS)) {
            Map<TopicPartition, Long> offsets = defaultIfNull((Map<TopicPartition, Long>) executionContext.get(TOPIC_PARTITION_OFFSETS), emptyMap());
            for (Map.Entry<TopicPartition, Long> entry : offsets.entrySet()) {
                this.partitionOffsets.put(entry.getKey(), entry.getValue() == 0 ? 0 : entry.getValue() + 1);
            }
        }
        this.kafkaConsumer.assign(this.topicPartitions);
        this.partitionOffsets.forEach(this.kafkaConsumer::seek);
    }

    @Nullable
    @Override
    public T read() {
        if (this.consumerRecords == null || !this.consumerRecords.hasNext()) {
            this.consumerRecords = this.kafkaConsumer.poll(this.pollTimeout).iterator();
        }
        if (this.consumerRecords.hasNext()) {
            T record = this.consumerRecords.next();
            this.partitionOffsets.put(new TopicPartition(record.topic(), record.partition()), record.offset());
            return record;
        } else {
            return null;
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        if (this.saveState) {
            executionContext.put(TOPIC_PARTITION_OFFSETS, new HashMap<>(this.partitionOffsets));
        }
        this.kafkaConsumer.commitSync();
    }

    @Override
    public void close() {
        if (this.kafkaConsumer != null) {
            this.kafkaConsumer.close();
        }
    }
}

package ms.kafkatest.utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.util.Assert;

/**
 * A builder implementation for the {@link KafkaRecordItemReader}.
 *
 * @author Mathieu Ouellet
 * @author Mahmoud Ben Hassine
 * @author Jean-Francois Larouche
 * @author Maxime Gagne
 * @see KafkaRecordItemReader
 * @since 4.2
 */
public class KafkaRecordItemReaderBuilder<T extends ConsumerRecord<?, ?>> {

    private Properties consumerProperties;

    private String topic;

    private List<Integer> partitions = new ArrayList<>();

    private Duration pollTimeout = Duration.ofSeconds(30L);

    private boolean saveState = true;

    private String name;

    private Deserializer<?> keyDeserializer;

    private Deserializer<?> valueDeserializer;

    /**
     * Configure if the state of the {@link org.springframework.batch.item.ItemStreamSupport}
     * should be persisted within the {@link org.springframework.batch.item.ExecutionContext}
     * for restart purposes.
     *
     * @param saveState defaults to true
     * @return The current instance of the builder.
     */
    public KafkaRecordItemReaderBuilder<T> saveState(boolean saveState) {
        this.saveState = saveState;
        return this;
    }

    /**
     * The name used to calculate the key within the {@link org.springframework.batch.item.ExecutionContext}.
     * Required if {@link #saveState(boolean)} is set to true.
     *
     * @param name name of the reader instance
     * @return The current instance of the builder.
     * @see org.springframework.batch.item.ItemStreamSupport#setName(String)
     */
    public KafkaRecordItemReaderBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Configure the underlying consumer properties.
     * <p><strong>{@code consumerProperties} must contain the following keys:
     * 'bootstrap.servers', 'group.id', 'key.deserializer' and 'value.deserializer' </strong></p>.
     *
     * @param consumerProperties properties of the consumer
     * @return The current instance of the builder.
     */
    public KafkaRecordItemReaderBuilder<T> consumerProperties(Properties consumerProperties) {
        this.consumerProperties = consumerProperties;
        return this;
    }

    /**
     * A list of partitions to manually assign to the consumer.
     *
     * @param partitions list of partitions to assign to the consumer
     * @return The current instance of the builder.
     */
    public KafkaRecordItemReaderBuilder<T> partitions(Integer... partitions) {
        return partitions(Arrays.asList(partitions));
    }

    /**
     * A list of partitions to manually assign to the consumer.
     *
     * @param partitions list of partitions to assign to the consumer
     * @return The current instance of the builder.
     */
    public KafkaRecordItemReaderBuilder<T> partitions(List<Integer> partitions) {
        this.partitions = partitions;
        return this;
    }

    /**
     * A topic name to manually assign to the consumer.
     *
     * @param topic name to assign to the consumer
     * @return The current instance of the builder.
     */
    public KafkaRecordItemReaderBuilder<T> topic(String topic) {
        this.topic = topic;
        return this;
    }

    /**
     * Set the pollTimeout for the poll() operations. Default to 30 seconds.
     *
     * @param pollTimeout timeout for the poll operation
     * @return The current instance of the builder.
     * @see KafkaRecordItemReader#setPollTimeout(Duration)
     */
    public KafkaRecordItemReaderBuilder<T> pollTimeout(Duration pollTimeout) {
        this.pollTimeout = pollTimeout;
        return this;
    }

    /**
     * Set a custom key deserializer. Default null.
     *
     * @param keyDeserializer custom key deserializer
     * @return The current instance of the builder.
     */
    public KafkaRecordItemReaderBuilder<T> keyDeserializer(Deserializer<?> keyDeserializer) {
        this.keyDeserializer = keyDeserializer;
        return this;
    }

    /**
     * Set a custom value deserializer. Default null.
     *
     * @param valueDeserializer custom value deserializer
     * @return The current instance of the builder.
     */
    public KafkaRecordItemReaderBuilder<T> valueDeserializer(Deserializer<?> valueDeserializer) {
        this.valueDeserializer = valueDeserializer;
        return this;
    }

    public KafkaRecordItemReader<T> build() {
        if (this.saveState) {
            Assert.hasText(this.name, "A name is required when saveState is set to true");
        }
        Assert.notNull(consumerProperties, "Consumer properties must not be null");
        Assert.isTrue(consumerProperties.containsKey(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG),
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG + " property must be provided");
        Assert.isTrue(consumerProperties.containsKey(ConsumerConfig.GROUP_ID_CONFIG),
                ConsumerConfig.GROUP_ID_CONFIG + " property must be provided");
        Assert.isTrue(consumerProperties.containsKey(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG + " property must be provided");
        Assert.isTrue(consumerProperties.containsKey(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + " property must be provided");
        Assert.hasLength(topic, "Topic name must not be null or empty");
        Assert.notNull(pollTimeout, "pollTimeout must not be null");
        Assert.isTrue(!pollTimeout.isZero(), "pollTimeout must not be zero");
        Assert.isTrue(!pollTimeout.isNegative(), "pollTimeout must not be negative");
        Assert.isTrue(!partitions.isEmpty(), "At least one partition must be provided");

        KafkaRecordItemReader<T> reader = new KafkaRecordItemReader<T>(this.consumerProperties, this.topic, this.partitions);
        reader.setPollTimeout(this.pollTimeout);
        reader.setSaveState(this.saveState);
        reader.setName(this.name);
        reader.setKeyDeserializer(this.keyDeserializer);
        reader.setValueDeserializer(this.valueDeserializer);
        return reader;
    }
}

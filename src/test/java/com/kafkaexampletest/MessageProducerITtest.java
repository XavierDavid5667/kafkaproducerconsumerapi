package com.kafkaexampletest;


import com.kafkaexample.MessageProducer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class MessageProducerITtest {
    @Container
    private static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @BeforeEach
    void setup() {
        // No setup required for this test
    }

    @Test
    void givenProducerRecord_whenSendMessage_thenMessageIsSent() {
        // Create producer properties
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties);

        // Create and send producer record
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("test-topic", "hello world");

        producer.send(producerRecord, (recordMetadata, e) -> {
            if (e != null) {
                e.printStackTrace();
            } else {
                System.out.println("Sent message to topic: " + recordMetadata.topic() + ", partition: " + recordMetadata.partition() + ", offset: " + recordMetadata.offset());
            }
        });

        producer.flush();
        producer.close();

        // Create consumer properties
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Create consumer and subscribe to topic
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
        consumer.subscribe(Arrays.asList("test-topic"));

        // Poll for new data
        ConsumerRecord<String, String> consumedRecord = null;
        for (int i = 0; i < 10; i++) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            if (!records.isEmpty()) {
                consumedRecord = records.iterator().next();
                break;  // Exit loop if message is found
            }
        }

        // Validate the consumed record
        assertThat(consumedRecord).isNotNull();
        assertThat(consumedRecord.value()).isEqualTo("hello world");

        consumer.close();
    }
}

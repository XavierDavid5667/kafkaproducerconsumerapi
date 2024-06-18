package com.kafkaexample;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;

import java.util.Collections;
import java.util.Properties;

public class KafkaAdminAPI {

    private final Properties properties;

    public KafkaAdminAPI(Properties properties) {
        this.properties = properties;
    }
    public void createTopic(String topicName) throws Exception {
        try (Admin admin = Admin.create(properties)) {
            int partitions = 1;
            short replicationFactor = 1;

            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
            CreateTopicsResult result = admin.createTopics(Collections.singleton(newTopic));

            // get the async result for the new topic creation
            KafkaFuture<Void> future = result.values()
                    .get(topicName);

            // call get() to block until topic creation has completed or failed
            future.get();
        }
    }
}

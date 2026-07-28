package com.foodplatform.notification.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.foodplatform.notification.infrastructure.messaging.event.DeliveryEvent;
import com.foodplatform.notification.infrastructure.messaging.event.OrderEvent;
import com.foodplatform.notification.infrastructure.messaging.event.PaymentEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Bean
    ObjectMapper kafkaObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, PaymentEvent> paymentEventKafkaListenerContainerFactory(
            ObjectMapper kafkaObjectMapper
    ) {
        return listenerContainerFactory(PaymentEvent.class, kafkaObjectMapper);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, OrderEvent> orderEventKafkaListenerContainerFactory(
            ObjectMapper kafkaObjectMapper
    ) {
        return listenerContainerFactory(OrderEvent.class, kafkaObjectMapper);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, DeliveryEvent> deliveryEventKafkaListenerContainerFactory(
            ObjectMapper kafkaObjectMapper
    ) {
        return listenerContainerFactory(DeliveryEvent.class, kafkaObjectMapper);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> listenerContainerFactory(
            Class<T> eventType,
            ObjectMapper kafkaObjectMapper
    ) {
        JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(eventType, kafkaObjectMapper);
        jsonDeserializer.addTrustedPackages("com.foodplatform.notification.infrastructure.messaging.event");
        jsonDeserializer.setUseTypeHeaders(false);

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        ConsumerFactory<String, T> consumerFactory =
                new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), jsonDeserializer);

        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}

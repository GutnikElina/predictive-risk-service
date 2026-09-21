package com.innowise.predictiveriskservice.kafka.producer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaPublisher kafkaPublisher;

    @BeforeEach
    void setUp() {
        kafkaPublisher = new KafkaPublisher(kafkaTemplate);
    }

    @Test
    void sendEvent_delegatesToKafkaTemplate_withGivenTopicAndPayload() {
        CompletableFuture<SendResult<String, Object>> expected = new CompletableFuture<>();
        when(kafkaTemplate.send("risk.level.escalated", "payload")).thenReturn(expected);

        CompletableFuture<SendResult<String, Object>> result =
                kafkaPublisher.sendEvent("risk.level.escalated", "payload");

        assertThat(result).isSameAs(expected);
        verify(kafkaTemplate).send("risk.level.escalated", "payload");
    }
}
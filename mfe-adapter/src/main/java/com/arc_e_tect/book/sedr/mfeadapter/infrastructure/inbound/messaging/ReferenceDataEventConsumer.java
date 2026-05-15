package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.messaging;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ReferenceDataEntry;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.inbound.ProcessReferenceDataEventUseCase;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.messaging.dto.ReferenceDataEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer that listens on all topics matching the configured
 * reference-data topic pattern and routes events to the application service.
 *
 * <p>Each MFA instance maintains its own consumer-group so that it receives
 * all events independently and can build a complete local cache.
 */
@Component
public class ReferenceDataEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReferenceDataEventConsumer.class);

    private final ProcessReferenceDataEventUseCase processReferenceDataEvent;
    private final ObjectMapper objectMapper;

    public ReferenceDataEventConsumer(ProcessReferenceDataEventUseCase processReferenceDataEvent,
                                      ObjectMapper objectMapper) {
        this.processReferenceDataEvent = processReferenceDataEvent;
        this.objectMapper = objectMapper;
    }

    /**
     * Consume a reference-data event from Kafka.
     *
     * <p>The topic pattern is resolved at startup from
     * {@code mfe-adapter.kafka.reference-data.topic-pattern}.
     *
     * @param payload the raw JSON string from Kafka
     */
    @KafkaListener(
            topicPattern = "${mfe-adapter.kafka.reference-data.topic-pattern}",
            groupId = "${mfe-adapter.kafka.consumer.group-id}",
            containerFactory = "referenceDataKafkaListenerContainerFactory")
    public void consume(String payload) {
        if (payload == null || payload.isBlank()) {
            log.warn("Received blank reference-data payload – skipping");
            return;
        }

        ReferenceDataEvent event;
        try {
            event = objectMapper.readValue(payload, ReferenceDataEvent.class);
        } catch (Exception e) {
            log.error("Failed to deserialize reference-data event: {}", payload, e);
            return;
        }

        log.debug("Received reference-data event: type={}, dataType={}, code={}",
                event.eventType(), event.dataType(), event.code());

        ReferenceDataEntry entry = new ReferenceDataEntry(
                event.dataType(),
                event.code(),
                event.name(),
                event.attributes() != null ? event.attributes() : Map.of());

        processReferenceDataEvent.process(event.eventType(), entry);
    }
}

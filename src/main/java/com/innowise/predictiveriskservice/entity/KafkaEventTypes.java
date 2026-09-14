package com.innowise.predictiveriskservice.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum KafkaEventTypes {
    RISK_LEVEL_ESCALATED_EVENT("risk.level.escalated"),
    CASCADE_IMPACT_DETECTED_EVENT("cascade.impact.detected");

    public final String kafkaTopic;
}

package com.innowise.predictiveriskservice.outbox;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    PUBLISH_FAILED,
    QUIT_PUBLISHING
}

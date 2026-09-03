package com.innowise.predictiveriskservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter @Setter
@ConfigurationProperties(prefix = "risk.weight")
public class RiskWeightConfig {
    private double delay;
    private double weather;
    private double vendorHistory;
    private double documentErrors;
}

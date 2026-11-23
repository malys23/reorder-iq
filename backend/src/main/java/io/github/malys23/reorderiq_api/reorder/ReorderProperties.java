package io.github.malys23.reorderiq_api.reorder;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "reorderiq.reorder")
public record ReorderProperties(int windowWeeks, Map<String, Double> zValues) {
}

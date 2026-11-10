package io.github.malys23.reorderiq_api.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix="reorderiq.seed")
public record SeedProperties(String dataDir, List<DemoUser> users) {
    public record DemoUser(Integer id, String username, String password, String role){}
}

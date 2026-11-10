package io.github.malys23.reorderiq_api.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Profile("dev")
public class DevDataLoader implements ApplicationRunner{
    private static final Logger log = LoggerFactory.getLogger(DevDataLoader.class);

    private final String dataDir;
    public DevDataLoader( @Value("${reorderiq.seed.data-dir}") String dataDir){
        this.dataDir = dataDir;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path path = Path.of(dataDir);
        log.info("Seed folder {} exists: {}", path.toAbsolutePath(), Files.exists(path));
    }
}

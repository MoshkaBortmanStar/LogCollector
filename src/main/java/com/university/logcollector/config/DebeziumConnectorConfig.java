package com.university.logcollector.config;

import com.university.logcollector.config.properties.DebeziumProperties;
import com.university.logcollector.config.properties.DebeziumPublicProperties;
import com.university.logcollector.service.impl.ElasticChangeConsumerService;
import io.debezium.embedded.EmbeddedEngine;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({
        DebeziumPublicProperties.class})
public class DebeziumConnectorConfig {

    private final DebeziumPublicProperties debeziumPublicProperties;
    private final ElasticChangeConsumerService elasticChangeConsumerService;

    private final Executor executor = Executors.newSingleThreadExecutor();

    private static EmbeddedEngine pgEngine = null;

    @PostConstruct
    public void initialStart() {
        pgEngine = this.pgEngine();
        this.executor.execute(pgEngine);

    }

    public void start() {
        if (pgEngine == null) {
            pgEngine = this.pgEngine();
        }
        this.executor.execute(pgEngine);
    }

    @PreDestroy
    public void stop() {
        if (pgEngine != null) {
            pgEngine.stop();
            pgEngine = null;
        }
    }



    private EmbeddedEngine pgEngine() {
        return EmbeddedEngine
                .create()
                .using(getConfiguration(debeziumPublicProperties))
                .notifying(elasticChangeConsumerService)
                .build();
    }



    private io.debezium.config.Configuration getConfiguration(DebeziumProperties debeziumProperties) {
        io.debezium.config.Configuration.Builder debeziumConfBuilder = io.debezium.config.Configuration.create();
        debeziumConfBuilder.with("table.include.list", debeziumProperties.getTableIncludeList());
        debeziumConfBuilder.with("database.hostname", debeziumProperties.getDataBaseHost());
        debeziumConfBuilder.with("database.port", debeziumProperties.getDataBasePort());
        debeziumConfBuilder.with("database.user", debeziumProperties.getDataBaseUserName());
        debeziumConfBuilder.with("database.password", debeziumProperties.getDataBasePassword());
        debeziumConfBuilder.with("database.dbname", debeziumProperties.getDataBaseName());
        debeziumConfBuilder.with("name", debeziumProperties.getName());
        debeziumConfBuilder.with("slot.name", debeziumProperties.getSlotName());
        debeziumConfBuilder.with("publication.name", debeziumProperties.getName());
        debeziumConfBuilder.with("database.server.name", debeziumProperties.getDataBaseServerName());
        debeziumConfBuilder.with("offset.storage.file.filename", debeziumProperties.getDebeziumOffsetFile());
        debeziumConfBuilder.with("topic.prefix", debeziumProperties.getTopicPrefix());
        debeziumConfBuilder.with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        debeziumConfBuilder.with("connector.class", "io.debezium.connector.postgresql.PostgresConnector");
        debeziumConfBuilder.with("offset.flush.interval.ms", 60000);
        debeziumConfBuilder.with("plugin.name", "pgoutput");
        //properties.put("flush.lsn.source", "false");
        //properties.put("slot.drop.on.stop", "true");
        debeziumConfBuilder.with("decimal.handling.mode", "string");
        debeziumConfBuilder.with("include.schema.changes", "true");
        debeziumConfBuilder.with("snapshot.mode", "never");
        debeziumConfBuilder.with("snapshot.locking.mode", "none");
        debeziumConfBuilder.with("tombstones.on.delete", "false");
        debeziumConfBuilder.with("publication.autocreate.mode", "filtered");
        debeziumConfBuilder.with("time.precision.mode", "connect");
        debeziumConfBuilder.with("event.processing.failure.handling.mode", "warn");

        return debeziumConfBuilder.build();
    }

}

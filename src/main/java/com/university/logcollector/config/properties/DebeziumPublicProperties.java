package com.university.logcollector.config.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "debezium")
@EqualsAndHashCode(callSuper = true)
@Data
public class DebeziumPublicProperties extends DebeziumProperties {

}

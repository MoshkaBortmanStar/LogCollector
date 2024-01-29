package com.university.logcollector.config.properties;

import lombok.Data;

@Data
public class DebeziumProperties {

    private String dataBaseHost;

    private String dataBaseName;

    private String dataBasePort;

    private String dataBaseSchema;

    private String dataBaseUserName;

    private String dataBasePassword;

    private String tableIncludeList;

    private String debeziumOffsetFile;

    private String slotName;

    private String name;

    private String dataBaseServerName;

    private String topicPrefix;

    private String heartbeatIntervalMs;
}

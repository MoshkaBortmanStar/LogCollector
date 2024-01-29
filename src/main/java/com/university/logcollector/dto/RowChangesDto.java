package com.university.logcollector.dto;

import io.debezium.data.Envelope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RowChangesDto implements Serializable {

    private String tableName;

    private Envelope.Operation operation;

    private Map<String, Object> data;

}

package com.university.logcollector.service.impl;

import com.university.logcollector.service.KafkaProducerService;
import com.university.logcollector.service.mapper.RowChangesMapper;
import io.debezium.data.Envelope.Operation;
import io.debezium.engine.DebeziumEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.university.logcollector.util.DebeziumUtils.TABLE;
import static com.university.logcollector.util.DebeziumUtils.getRowPayload;
import static io.debezium.data.Envelope.FieldName.AFTER;
import static io.debezium.data.Envelope.FieldName.BEFORE;
import static io.debezium.data.Envelope.FieldName.OPERATION;
import static io.debezium.data.Envelope.FieldName.SOURCE;

/** Represents abstract consumer of change data events from a Debezium engine. Using for Elastic migration.
 * @author Mikhail Butorin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticChangeConsumerService implements DebeziumEngine.ChangeConsumer<SourceRecord> {

    private final RowChangesMapper rowChangesMapper;
    private final KafkaProducerService kafkaProducerService;

    @Value(value = "${spring.kafka.topic-name}")
    private String topicName;

    /**
     * This method is invoked when a transactional action is performed on any of the tables that were configured.
     *
     * @param records data change events, committer - commit offset
     */
    public void handleBatch(List<SourceRecord> records, DebeziumEngine.RecordCommitter<SourceRecord> committer) {
        try {
            records.forEach(sourceRecord -> handleSourceRecord(sourceRecord, committer));
            committer.markBatchFinished();
        } catch (InterruptedException e) {
            log.error("Error commit batch");
        }
    }


    private void handleSourceRecord(SourceRecord sourceRecord, DebeziumEngine.RecordCommitter<SourceRecord> committer) {
        Struct sourceRecordChangeValue = (Struct) sourceRecord.value();

        if (sourceRecordChangeValue != null) {
            Operation operation = Operation.forCode((String) sourceRecordChangeValue.get(OPERATION));

            if (operation != Operation.READ) {
                String operationRecord = (operation == Operation.DELETE || operation ==  Operation.TRUNCATE)
                        ? BEFORE
                        : AFTER;
                var payload = getRowPayload(sourceRecordChangeValue, operationRecord, operation);
                var tableName = ((Struct) sourceRecordChangeValue.get(SOURCE)).get(TABLE).toString();

                var rowChangesDto = rowChangesMapper.toRowChangesDto(tableName, operation, payload);
                log.info("Row changes Dto {}", rowChangesDto);
                kafkaProducerService.sendMessage(rowChangesDto,topicName);
            }
        }

        commitOffset(committer, sourceRecord);
    }

    private void commitOffset(DebeziumEngine.RecordCommitter<SourceRecord> committer, SourceRecord sourceRecord) {
        try {
            committer.markProcessed(sourceRecord);
        } catch (InterruptedException e) {
            log.error("Error commit record");

            throw new IllegalArgumentException(e.getMessage());
        }
    }

}

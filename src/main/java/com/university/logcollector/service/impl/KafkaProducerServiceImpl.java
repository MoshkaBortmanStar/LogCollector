package com.university.logcollector.service.impl;


import com.university.logcollector.dto.RowChangesDto;
import com.university.logcollector.service.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final   KafkaTemplate<String, RowChangesDto> kafkaTemplate;

    public KafkaProducerServiceImpl(@Qualifier("kafkaTemplateDebezium") KafkaTemplate<String, RowChangesDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendMessage(RowChangesDto rowChangesDto, String topicName) {
        log.info("sending rowChangesDto='{}' to topic='{}'", rowChangesDto, topicName);
        var key = rowChangesDto.getTableName();

        kafkaTemplate.send(topicName, key, rowChangesDto);
    }
}

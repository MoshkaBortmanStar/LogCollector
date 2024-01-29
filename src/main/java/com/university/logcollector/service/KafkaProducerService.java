package com.university.logcollector.service;


import com.university.logcollector.dto.RowChangesDto;

public interface KafkaProducerService {

    void sendMessage(RowChangesDto rowChangesDto, String topicName);

}

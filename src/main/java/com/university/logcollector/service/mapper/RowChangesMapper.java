package com.university.logcollector.service.mapper;


import com.university.logcollector.dto.RowChangesDto;
import io.debezium.data.Envelope;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Map;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RowChangesMapper {

//    @Mapping(source = "operation", target = "operation")
//    @Mapping(source = "tableName", target = "tableName")
//    @Mapping(source = "payload", target = "data")
    RowChangesDto toRowChangesDto(String tableName,
                                  Envelope.Operation operation,
                                  Map<String, Object> payload);

}

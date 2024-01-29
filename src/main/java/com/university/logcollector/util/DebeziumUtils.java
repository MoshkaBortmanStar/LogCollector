package com.university.logcollector.util;

import io.debezium.data.Envelope.Operation;
import io.debezium.data.VariableScaleDecimal;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.BiFunction;

//TODO: migrate to DebeziumService
public class DebeziumUtils {

    public static final String VARIABLE_SCALE_DECIMAL = "io.debezium.data.VariableScaleDecimal";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss");
    public static final String TABLE = "table";

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @SneakyThrows
    public static Object formatValue(Object value) {
        if (value instanceof Date) {
            String format = DATE_FORMAT.format(value);
            return DATE_FORMAT.parse(format).getTime();
        }
        return value;
    }

    /**
     * This method is used to get the row value from the debezium source wal record.
     *
     * @param value  debezium source wal record = {@link Struct}
     * @param schema debezium source wal record schema = {@link Schema}
     * @return row value
     */
    @SneakyThrows
    public static Object getDebeziumRowValue(Object value, Schema schema) {
        if (value == null) {
            return null;
        } else if (schema.type() == Schema.Type.STRUCT) {
            if (StringUtils.equals(schema.name(), VARIABLE_SCALE_DECIMAL)) {
                return VariableScaleDecimal.toLogical((Struct) value).getDecimalValue().orElse(null);
            }
            return ((Struct) value).get("value");
        } else if (value instanceof Date) {
            return DATE_FORMAT.parse(DATE_FORMAT.format(value)).getTime();
        } else {
            return value;
        }
    }

    public static Map<String, Object> getRowPayload(Struct rowStruct, String state, Operation operation) {
        return operation == Operation.TRUNCATE
                ? getRowPayload(rowStruct.schema().field(state).schema(), (fieldName, schema) -> null)
                : getRowPayload(((Struct) rowStruct.get(state)).schema(),
                (fieldName, schema) -> getDebeziumRowValue(((Struct) rowStruct.get(state)).get(fieldName), schema));
    }

    public static Map<String, Object> getRowPayload(Schema schema, BiFunction<String, Schema, Object> biFunction) {
        Map<String, Object> map = new HashMap<>();
        schema.fields().forEach(field -> map.put(field.name(), biFunction.apply(field.name(), field.schema())));

        return map;
    }
}

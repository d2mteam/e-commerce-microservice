package com.project.orderservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.orderservice.domain.OrderAggregate;
import com.project.orderservice.event_sourcing_core.domain.Aggregate;
import com.project.orderservice.event_sourcing_core.domain.event.Event;
import com.project.orderservice.event_sourcing_core.domain.event.EventWithId;
import com.project.orderservice.event_sourcing_core.service.event.SyncEventHandler;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class OrderSyncEventHandler implements SyncEventHandler {
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public void handleEvents(List<EventWithId<Event>> events, Aggregate aggregate) {
        for (EventWithId<Event> eventWithId : events) {
            Event event = eventWithId.event();
            PGobject jsonbPayload = new PGobject();
            jsonbPayload.setType("jsonb");
            jsonbPayload.setValue(objectMapper.writeValueAsString(event));

            String sql = """
                        INSERT INTO outbox_event (
                            aggregate_id, aggregate_type, event_type, event_version, payload
                        ) VALUES (
                            :aggregateId, :aggregateType, :eventType, :version, :payload
                        )
                    """;

            var params = new MapSqlParameterSource()
                    .addValue("aggregateId", event.getAggregateId())
                    .addValue("aggregateType", aggregate.getAggregateType())
                    .addValue("eventType", event.getEventType())
                    .addValue("version", event.getVersion())
                    .addValue("payload", jsonbPayload);

            jdbc.update(sql, params);
        }
    }

    @Nonnull
    @Override
    public String getAggregateType() {
        return OrderAggregate.class.getSimpleName();
    }
}

package com.project.orderservice.repository;

import com.project.orderservice.domain.outbox.OutboxEvent;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class OutboxEventRepository {

    private final NamedParameterJdbcTemplate jdbc;


    private final RowMapper<OutboxEvent> mapper = (rs, rowNum) -> {
        OutboxEvent e = new OutboxEvent();
        e.setId(rs.getLong("id"));
        e.setAggregateId((UUID) rs.getObject("aggregate_id"));
        e.setAggregateType(rs.getString("aggregate_type"));
        e.setEventType(rs.getString("event_type"));
        e.setEventVersion(rs.getInt("event_version"));
        e.setPayload(rs.getString("payload"));
        e.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        e.setSentAt(rs.getTimestamp("sent_at") != null ? rs.getTimestamp("sent_at").toInstant() : null);
        e.setRetryCount(rs.getInt("retry_count"));
        e.setStatus(rs.getString("status"));
        e.setLastError(rs.getString("last_error"));
        return e;
    };

    /**
     * Lấy event kế tiếp của một aggregate_id (theo version thấp nhất),
     * chỉ lấy event có status = 'PENDING' hoặc 'RETRYING'.
     */
    public Optional<OutboxEvent> findNextEventForAggregate(UUID aggregateId) {
        String sql = """
            SELECT * FROM outbox_event
            WHERE aggregate_id = :aggregateId
              AND status IN ('PENDING', 'RETRYING')
            ORDER BY event_version ASC
            LIMIT 1
        """;
        var params = Map.of("aggregateId", aggregateId);
        List<OutboxEvent> list = jdbc.query(sql, params, mapper);
        return list.stream().findFirst();
    }

    public List<UUID> findAggregateIdsWithPendingEvents() {
        String sql = """
            SELECT DISTINCT aggregate_id
            FROM outbox_event
            WHERE status IN ('PENDING', 'RETRYING')
            ORDER BY aggregate_id
        """;
        return jdbc.query(sql, (rs, rowNum) -> (UUID) rs.getObject("aggregate_id"));
    }

    public void markSent(Long id) {
        String sql = """
            UPDATE outbox_event
            SET status = 'SENT',
                sent_at = now()
            WHERE id = :id
        """;
        jdbc.update(sql, Map.of("id", id));
    }

    public void markFailed(Long id, String errorMessage, int retryCount, int maxRetry) {
        String nextStatus = retryCount >= maxRetry ? "FAILED" : "RETRYING";
        String sql = """
            UPDATE outbox_event
            SET status = :status,
                retry_count = :retryCount,
                last_error = :error
            WHERE id = :id
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", nextStatus)
                .addValue("retryCount", retryCount)
                .addValue("error", errorMessage);
        jdbc.update(sql, params);
    }
}

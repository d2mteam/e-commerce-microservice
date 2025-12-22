package com.project.infrastructure.query;

import akka.actor.typed.ActorSystem;
import akka.persistence.jdbc.query.javadsl.JdbcReadJournal;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.PersistenceQuery;
import akka.stream.Materializer;
import akka.stream.SystemMaterializer;
import akka.stream.javadsl.Sink;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class JournalQueryService {

    private final ActorSystem<Void> actorSystem;
    private final ObjectMapper objectMapper;

    public List<Map<String, Object>> listEvents(UUID aggregateId) {
        String pid = "Order|" + aggregateId;
        Materializer mat = SystemMaterializer.get(actorSystem).materializer();
        JdbcReadJournal readJournal = PersistenceQuery.get(actorSystem.classicSystem())
                .getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());

        try {
            List<EventEnvelope> envelopes = readJournal.currentEventsByPersistenceId(pid, 0, Long.MAX_VALUE)
                    .runWith(Sink.seq(), mat)
                    .toCompletableFuture()
                    .get();

            return envelopes.stream()
                    .map(env -> {
                        Map<String, Object> m = new java.util.LinkedHashMap<>();
                        m.put("persistenceId", env.persistenceId());
                        m.put("sequenceNumber", env.sequenceNr());
                        m.put("timestamp", Instant.ofEpochMilli(env.timestamp()).toString());
                        m.put("event", objectMapper.valueToTree(env.event()));
                        m.put("eventType", env.event().getClass().getName());
                        return m;
                    })
                    .toList();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to load events", e);
        }
    }
}

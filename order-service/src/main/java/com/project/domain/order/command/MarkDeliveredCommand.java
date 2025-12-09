package com.project.domain.order.command;

import com.project.domain.order.aggregate.OrderAggregate;
import com.project.event_sourcing_core.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class MarkDeliveredCommand extends Command {
    private final UUID userId;
    private final OffsetDateTime deliveredAt;

    @Builder
    public MarkDeliveredCommand(UUID aggregateId, UUID userId, OffsetDateTime deliveredAt) {
        super(OrderAggregate.class.getSimpleName(), aggregateId);
        this.userId = userId;
        this.deliveredAt = deliveredAt;
    }
}

package com.project.orderservice.domain;

import com.project.orderservice.event_sourcing.domain.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ConfirmOrderCommand extends Command {
    public ConfirmOrderCommand(UUID aggregateId) {
        super(aggregateId);
    }
}


package com.project.orderservice.domain.command;

import com.project.orderservice.domain.Command;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CompleteOrderCommand extends Command {
    public CompleteOrderCommand(UUID aggregateId) {
        super(aggregateId);
    }
}
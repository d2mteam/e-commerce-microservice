package com.project.orderservice.event_sourcing.service.command;

import com.project.orderservice.event_sourcing.domain.Aggregate;
import com.project.orderservice.event_sourcing.domain.command.Command;
import jakarta.annotation.Nonnull;

public interface CommandHandler<T extends Command> {

    void handle(Aggregate aggregate, Command command);

    @Nonnull
    Class<T> getCommandType();
}

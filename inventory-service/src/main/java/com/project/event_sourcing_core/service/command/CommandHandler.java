package com.project.event_sourcing_core.service.command;

import com.project.event_sourcing_core.domain.Aggregate;
import com.project.event_sourcing_core.domain.command.Command;
import jakarta.annotation.Nonnull;

public interface CommandHandler<T extends Command> {

    void handle(Aggregate aggregate, Command command);

    @Nonnull
    Class<T> getCommandType();
}

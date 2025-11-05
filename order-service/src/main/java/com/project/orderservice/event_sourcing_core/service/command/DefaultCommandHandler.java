package com.project.orderservice.event_sourcing.service.command;

import com.project.orderservice.event_sourcing.domain.Aggregate;
import com.project.orderservice.event_sourcing.domain.command.Command;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultCommandHandler implements CommandHandler<Command> {

    @Override
    public void handle(Aggregate aggregate, Command command) {
        aggregate.process(command);
    }

    @Nonnull
    @Override
    public Class<Command> getCommandType() {
        return Command.class;
    }
}

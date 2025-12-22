package com.project.akka;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.ConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AkkaInventoryConfiguration {

    @Bean
    public ActorSystem<Void> inventoryActorSystem() {
        return ActorSystem.create(Behaviors.empty(), "inventory-actor-system", ConfigFactory.load());
    }
}

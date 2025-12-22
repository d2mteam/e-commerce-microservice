package com.project.akka;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.ConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AkkaOrderConfiguration {

    @Bean
    public ActorSystem<Void> orderActorSystem() {
        return ActorSystem.create(Behaviors.empty(), "order-actor-system", ConfigFactory.load());
    }
}

package com.example.travelez.backend.reaction.config;

import com.example.travelez.backend.reaction.handler.ReactionTargetHandler;
import com.example.travelez.backend.reaction.model.enums.ReactionTargetType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class ReactionConfig {
    @Bean
    public Map<ReactionTargetType, ReactionTargetHandler> reactionTargetHandlerMap(List<ReactionTargetHandler> handlers) {
        return handlers.stream()
                .collect(Collectors.toMap(ReactionTargetHandler::getTargetType, Function.identity()));
    }
}

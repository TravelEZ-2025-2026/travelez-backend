package com.example.travelez.backend.infrastructure.config;

import com.example.travelez.backend.infrastructure.websocket.handler.StompCommandHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final Map<StompCommand, StompCommandHandler> handlerMap;
    @Value("${spring.rabbitmq.host}")
    private String rabbitmqHost;
    @Value("${spring.rabbitmq.username}")
    private String rabbitmqUser;
    @Value("${spring.datasource.password}")
    private String rabbitmqPass;

    public WebSocketConfig(List<StompCommandHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(StompCommandHandler::getCommand, Function.identity()));
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/ws-raw")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");

        registry.enableStompBrokerRelay("/topic", "queue")
                .setRelayHost(rabbitmqHost)
                .setRelayPort(61613)
                .setClientLogin(rabbitmqUser)
                .setClientPasscode(rabbitmqPass)
                .setSystemLogin(rabbitmqUser)
                .setSystemPasscode(rabbitmqPass);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && accessor.getCommand() != null) {
                    StompCommandHandler handler = handlerMap.get(accessor.getCommand());
                    if (handler != null) {
                        // dev: thay vi close connect thi khong tra ve gi
                        try {
                            handler.handle(accessor);
                        } catch (Exception e) {
                            System.err.println("Blocked STOMP command: " + e.getMessage());
                            return null;
                        }
                        // production
                        // handler.handle(accessor);
                    }
                }
                return message;
            }
        });
    }
}















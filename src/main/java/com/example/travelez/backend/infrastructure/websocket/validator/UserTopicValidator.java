package com.example.travelez.backend.infrastructure.websocket.validator;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.security.component.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserTopicValidator implements TopicValidator {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean supports(String destination) {
        return pathMatcher.match("/topic/user.*", destination);
    }

    @Override
    public void validate(UserPrinciple user, String destination) {
        try {
            String ids = destination.substring(destination.lastIndexOf('.') + 1);
            Long userId = Long.parseLong(ids);
            if (!Objects.equals(user.getUserId(), userId))
                throw new ApiException(ResultCode.FORBIDDEN, "You cannot subscribe to another user's private channel");
        } catch (NumberFormatException e) {
            throw new ApiException(ResultCode.BAD_REQUEST, "Invalid user ID format");
        }
    }
}

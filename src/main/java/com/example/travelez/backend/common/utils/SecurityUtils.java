package com.example.travelez.backend.common.utils;

import com.example.travelez.backend.security.component.UserPrinciple;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static UserPrinciple getUserPrinciple() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof AnonymousAuthenticationToken ? null : (UserPrinciple) authentication.getPrincipal();
    }

    public static Long getCurrentUserId() {
        UserPrinciple userPrinciple = getUserPrinciple();
        return userPrinciple == null ? null : userPrinciple.getUserId();
    }
}

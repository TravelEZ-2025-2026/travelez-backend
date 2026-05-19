package com.example.travelez.backend.users.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.dashboard.model.enums.ActivityCategory;
import com.example.travelez.backend.dashboard.service.impl.AuditLogService;
import com.example.travelez.backend.users.dto.request.AdminUserFilterRequest;
import com.example.travelez.backend.users.dto.request.UserStatusUpdateRequest;
import com.example.travelez.backend.users.dto.response.AdminUserResponse;
import com.example.travelez.backend.users.dto.response.UserAdminDetailResponse;
import com.example.travelez.backend.users.dto.response.UserDetailResponse;
import com.example.travelez.backend.users.event.UserStatusChangedEvent;
import com.example.travelez.backend.users.handler.UserStatusHandler;
import com.example.travelez.backend.users.handler.impl.BanUserHandler;
import com.example.travelez.backend.users.handler.impl.UnbanUserHandler;
import com.example.travelez.backend.users.mapper.UserMapper;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.model.enums.ActionType;
import com.example.travelez.backend.users.model.enums.RoleType;
import com.example.travelez.backend.users.repository.UserRepository;
import com.example.travelez.backend.users.repository.specification.UserSpecification;
import com.example.travelez.backend.users.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BanUserHandler banUserHandler;
    private final UnbanUserHandler unbanUserHandler;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogService auditLogService;

    @Override
    public CommonPage<AdminUserResponse> getAllUsersForAdmin(AdminUserFilterRequest filter, Pageable pageable) {
        List<Specification<User>> specs = new ArrayList<>();
        specs.add(UserSpecification.hasRole(RoleType.TRAVELER));
        
        if (filter.getStatus() != null) {
            specs.add(UserSpecification.hasStatus(filter.getStatus()));
        }
        
        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            specs.add(UserSpecification.searchByKeyword(filter.getKeyword()));
        }
        
        Page<User> users = userRepository.findAll(Specification.allOf(specs), pageable);
        List<AdminUserResponse> responses = users.getContent().stream()
                .map(userMapper::toAdminUserResponse)
                .toList();
        
        return new CommonPage<>(responses, users.getTotalPages(), users.getTotalElements(), 
                pageable.getPageSize(), users.getNumber(), users.isEmpty());
    }

    @Override
    public UserAdminDetailResponse getUserDetail(Long userId) {
        User user = userRepository.findUserProfileById(userId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "User not found"));

        if (user.getRole() != RoleType.TRAVELER) {
            throw new ApiException(ResultCode.FORBIDDEN, "Can only view TRAVELER users");
        }

        return userMapper.toUserAdminDetailResponse(user);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, UserStatusUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "User not found"));

        if (user.getRole() != RoleType.TRAVELER) {
            throw new ApiException(ResultCode.FORBIDDEN, "Can only update TRAVELER users");
        }

        UserStatusHandler handler = getHandler(request.getAction());
        
        if (!handler.canHandle(user)) {
            log.info("User status unchanged: userId={}, currentStatus={}, action={}", 
                    userId, user.getStatus(), request.getAction());
            return;
        }

        handler.handle(user, request.getReason());
        userRepository.save(user);

        String statusText = request.getAction().name().equalsIgnoreCase("BAN") ? "locked" : "unlocked";
        auditLogService.logActivity(
                ActivityCategory.USER,
                "Admin " + statusText + " account ID #" + userId + ". Reason: " + request.getReason(),
                "Action Taken"
        );

        UserStatusChangedEvent event = new UserStatusChangedEvent(
                userId, 
                request.getAction(), 
                request.getReason()
        );
        eventPublisher.publishEvent(event);
    }

    private UserStatusHandler getHandler(ActionType action) {
        Map<ActionType, UserStatusHandler> handlers = Map.of(
                ActionType.BAN, banUserHandler,
                ActionType.UNBAN, unbanUserHandler
        );
        return handlers.get(action);
    }
}

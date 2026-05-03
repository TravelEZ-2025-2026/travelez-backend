package com.example.travelez.backend.itinerary.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import com.example.travelez.backend.itinerary.dto.response.SharedUserSearchResponse;
import com.example.travelez.backend.itinerary.mapper.ItineraryMapper;
import com.example.travelez.backend.itinerary.model.Itinerary;
import com.example.travelez.backend.itinerary.model.ItinerarySharedUser;
import com.example.travelez.backend.itinerary.model.ItinerarySharedUserId;
import com.example.travelez.backend.itinerary.repository.ItineraryRepository;
import com.example.travelez.backend.itinerary.repository.ItinerarySharedUserRepository;
import com.example.travelez.backend.itinerary.service.ItineraryManagementService;
import com.example.travelez.backend.security.component.UserPrinciple;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ItineraryManagementServiceImpl implements ItineraryManagementService {

    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;
    private final ItinerarySharedUserRepository sharedUserRepository;
    private final ItineraryMapper itineraryMapper;

    @Override
    @Transactional
    public void shareItineraryWithUser(Long itineraryId, String username) {
        UserPrinciple currentUser = getCurrentUser();

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Itinerary not found"));

        // Chỉ chủ tài khoản mới có quyền share
        if (!Objects.equals(itinerary.getTraveler().getId(), currentUser.getUserId())) {
            throw new ApiException(ResultCode.FORBIDDEN, "Only owner can share itinerary");
        }

        // Tìm target user qua username
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Target user not found"));

        // Cấm user tự share cho chính mình
        if (Objects.equals(targetUser.getId(), currentUser.getUserId())) {
            throw new ApiException(ResultCode.VALIDATION_FAILED, "You cannot share itinerary with yourself");
        }

        // Kiểm tra xem đã share chưa rồi lưu
        boolean alreadyShared = sharedUserRepository.existsByItineraryIdAndUserId(itineraryId, targetUser.getId());
        if (alreadyShared) {
            throw new ApiException(ResultCode.VALIDATION_FAILED, "This itinerary has already been shared with the user: " + username);
        }

        ItinerarySharedUser sharedUser = ItinerarySharedUser.builder()
                .itineraryId(itineraryId)
                .userId(targetUser.getId())
                .build();
        sharedUserRepository.save(sharedUser);
    }

    @Override
    @Transactional
    public void removeSharedUser(Long itineraryId, String username) {
        UserPrinciple currentUser = getCurrentUser();

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Itinerary not found"));

        if (!Objects.equals(itinerary.getTraveler().getId(), currentUser.getUserId())) {
            throw new ApiException(ResultCode.FORBIDDEN, "Only owner can manage shared users");
        }

        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Target user not found"));

        ItinerarySharedUserId id = new ItinerarySharedUserId(itineraryId, targetUser.getId());
        ItinerarySharedUser sharedUser = sharedUserRepository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.VALIDATION_FAILED, "This itinerary is not currently shared with the user: " + username));

        sharedUserRepository.delete(sharedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public CommonPage<ItinerarySummaryResponse> getSharedWithMeItineraries(Pageable pageable) {
        UserPrinciple currentUser = getCurrentUser();

        Page<Itinerary> itineraries =
                itineraryRepository.findItinerariesSharedWithUser(currentUser.getUserId(), pageable);

        List<ItinerarySummaryResponse> summaryResponses = itineraries.stream()
                .map(itineraryMapper::toSummaryResponse)
                .toList();

        return new CommonPage<>(
                summaryResponses,
                itineraries.getTotalPages(),
                itineraries.getTotalElements(),
                pageable.getPageSize(),
                itineraries.getNumber(),
                itineraries.isEmpty()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SharedUserSearchResponse> searchSharedUsers(Long itineraryId, String keyword) {
        UserPrinciple currentUser = getCurrentUser();

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Itinerary not found"));

        if (!Objects.equals(itinerary.getTraveler().getId(), currentUser.getUserId())) {
            throw new ApiException(ResultCode.FORBIDDEN, "Only owner can search shared users");
        }

        List<ItinerarySharedUser> sharedUsers = sharedUserRepository.searchSharedUsersByKeyword(itineraryId, keyword);

        return sharedUsers.stream()
                .map(itineraryMapper::toSharedUserSearchResponse)
                .toList();
    }

    @Override
    public void exportToGoogleCalendar(Long itineraryId) {
        throw new ApiException(ResultCode.FORBIDDEN, "This feature is not available yet");
    }

    private UserPrinciple getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ApiException(ResultCode.UNAUTHORIZED, "You need to log in to perform this action.");
        }
        return (UserPrinciple) authentication.getPrincipal();
    }
}

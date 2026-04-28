package com.example.travelez.backend.itinerary.service.impl;

import com.example.travelez.backend.ai.pipeline.facade.AiItineraryFacade;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.request.ItineraryReplanRequest;
import com.example.travelez.backend.itinerary.dto.request.ItinerarySaveRequest;
import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import com.example.travelez.backend.itinerary.dto.response.utils.ActivityDTO;
import com.example.travelez.backend.itinerary.dto.response.utils.DayPlan;
import com.example.travelez.backend.itinerary.dto.response.ItineraryDetailResponse;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.mapper.ItineraryMapper;
import com.example.travelez.backend.itinerary.model.Itinerary;
import com.example.travelez.backend.itinerary.model.ItineraryActivity;
import com.example.travelez.backend.itinerary.repository.ItineraryActivityRepository;
import com.example.travelez.backend.itinerary.repository.ItineraryRepository;
import com.example.travelez.backend.itinerary.repository.ItinerarySharedUserRepository;
import com.example.travelez.backend.itinerary.repository.cache.ItineraryCacheRepository;
import com.example.travelez.backend.itinerary.repository.specification.ItinerarySpecification;
import com.example.travelez.backend.itinerary.service.ItineraryService;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.repository.PoiRepository;
import com.example.travelez.backend.security.component.UserPrinciple;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!mock")
public class ItineraryServiceImpl implements ItineraryService {
    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryActivityRepository itineraryActivityRepository;
    private final PoiRepository poiRepository;
    private final ItineraryMapper itineraryMapper;
    private final ItineraryCacheRepository itineraryCacheRepository;
    private final AiItineraryFacade aiPipelineFacade;
    private final ItinerarySharedUserRepository itinerarySharedUserRepository;

    @Override
    public ItineraryResponse generateSmartItinerary(ItineraryCreationRequest request) {
        ItineraryResponse response = aiPipelineFacade.orchestratePipeline(request);

        if (response != null) {
            String tempId = UUID.randomUUID().toString();
            response.setTempId(tempId);

            itineraryCacheRepository.save(tempId, response);
        }
        return response;
    }

    @Override
    public ItineraryResponse getTempItinerary(String tempId) {
        ItineraryResponse data = itineraryCacheRepository.get(tempId);
        if (data == null) {
            throw new ApiException(ResultCode.NOT_FOUND, "The session has expired");
        }
        return data;
    }

    @Override
    @Transactional
    public Long saveItinerary(ItinerarySaveRequest request) {
        UserPrinciple userPrinciple = getCurrentUser();

        User traveler = userRepository.findById(userPrinciple.getUserId())
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "User information not found" ));

        ItineraryResponse aiData = request.getAiResult();
        if (aiData == null) {
            throw new ApiException(ResultCode.VALIDATION_FAILED, "AI itinerary data cannot be empty");
        }

        Itinerary itinerary = itineraryMapper.createItineraryEntity(
                request.getCreateRequest(),
                request.getAiResult(),
                traveler
        );
        Itinerary savedItinerary = itineraryRepository.save(itinerary);

        Set<Long> poiIds = new HashSet<>();
        if (request.getAiResult().getDays() != null) {
            request.getAiResult().getDays().stream()
                    .filter(day -> day.getActivities() != null)
                    .flatMap(day -> day.getActivities().stream())
                    .filter(act -> act.getId() > 0)
                    .forEach(act -> poiIds.add(act.getId()));
        }

        Map<Long, Poi> poiMap = new HashMap<>();
        if (!poiIds.isEmpty()) {
            poiMap = poiRepository.findAllById(poiIds).stream()
                    .collect(Collectors.toMap(Poi::getId, p -> p));
        }

        List<ItineraryActivity> activities = new ArrayList<>();
        if (request.getAiResult().getDays() != null) {
            for (DayPlan day : request.getAiResult().getDays()) {
                LocalDate currentDate = LocalDate.parse(day.getDate());

                if (day.getActivities() != null) {
                    for (ActivityDTO dto : day.getActivities()) {
                        Poi linkedPoi = poiMap.get(dto.getId());

                        activities.add(itineraryMapper.createActivityEntity(
                                dto, savedItinerary, currentDate, linkedPoi
                        ));
                    }
                }
            }
        }

        itineraryActivityRepository.saveAll(activities);
        return savedItinerary.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonPage<ItinerarySummaryResponse> getItineraryList(Pageable pageable) {
        UserPrinciple currentUser = getCurrentUser();

        Specification<Itinerary> spec = ItinerarySpecification.belongsToUser(currentUser.getUserId());

        Page<Itinerary> itineraries = itineraryRepository.findAll(spec, pageable);

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
    public ItineraryDetailResponse getItineraryDetail(Long itineraryId) {
        UserPrinciple currentUser = getCurrentUser();

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Itinerary not found"));

        boolean isOwner = Objects.equals(itinerary.getTraveler().getId(), currentUser.getUserId());

        if (!isOwner) {
            boolean isSharedWithMe = itinerarySharedUserRepository.existsByItineraryIdAndUserId(itineraryId, currentUser.getUserId());
            if (!isSharedWithMe) {
                throw new ApiException(ResultCode.FORBIDDEN, "You are not allowed to access this itinerary. It is not shared with you.");
            }
        }
        // ================================

        List<ItineraryActivity> dbActivities = itineraryActivityRepository
                .findByItineraryIdOrderByItineraryDateAscStartTimeAsc(itineraryId);

        ItineraryDetailResponse response = itineraryMapper.toDetailResponseHeader(itinerary);
        List<DayPlan> dayPlans = groupActivitiesByDate(dbActivities);
        response.setDays(dayPlans);

        return response;
    }

    @Override
    @Transactional
    public void deleteItinerary(Long itineraryId) {
        UserPrinciple currentUser = getCurrentUser();

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Itinerary not found"));

        if (!Objects.equals(itinerary.getTraveler().getId(), currentUser.getUserId())) {
            throw new ApiException(ResultCode.FORBIDDEN, "You are not allowed to delete this itinerary");
        }

        itineraryRepository.delete(itinerary);
    }

    @Override
    public ItineraryResponse replanSmartItinerary(ItineraryReplanRequest request) {
        log.info("Handling Replan Request for user notes: {}", request.getFeedbackNotes());

        // 1. Gọi luồng Replan Pipeline
        ItineraryResponse response = aiPipelineFacade.orchestrateReplanPipeline(request);

        // 2. Lưu vào Cache ngắn hạn phòng user F5 mất kết quả
        if (response != null) {
            String tempId = UUID.randomUUID().toString();
            response.setTempId(tempId);

            itineraryCacheRepository.save(tempId, response);
        }

        return response;
    }


    // --- HELPER METHODS ---

    private List<DayPlan> groupActivitiesByDate(List<ItineraryActivity> dbActivities) {
        if (dbActivities == null || dbActivities.isEmpty()) return new ArrayList<>();

        Map<LocalDate, List<ItineraryActivity>> grouped = dbActivities.stream()
                .collect(Collectors.groupingBy(
                        ItineraryActivity::getItineraryDate,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<DayPlan> days = new ArrayList<>();
        int dayIndex = 1;
        for (Map.Entry<LocalDate, List<ItineraryActivity>> entry : grouped.entrySet()) {
            List<ActivityDTO> activityDTOs = entry.getValue().stream()
                    .map(itineraryMapper::toActivityDTO)
                    .collect(Collectors.toList());

            DayPlan dayPlan = new DayPlan();
            dayPlan.setDayIndex(dayIndex++);
            dayPlan.setDate(entry.getKey().toString());
            dayPlan.setActivities(activityDTOs);
            days.add(dayPlan);
        }
        return days;
    }

    private UserPrinciple getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ApiException(ResultCode.UNAUTHORIZED, "You need to log in to perform this action.");
        }
        return (UserPrinciple) authentication.getPrincipal();
    }
}
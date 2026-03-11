package com.example.travelez.backend.itinerary.service.impl;

import com.example.travelez.backend.ai.service.AiService;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.request.ItinerarySaveRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryDetailResponse;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import com.example.travelez.backend.itinerary.dto.response.utils.ActivityDTO;
import com.example.travelez.backend.itinerary.dto.response.utils.DayPlan;
import com.example.travelez.backend.itinerary.mapper.ItineraryMapper;
import com.example.travelez.backend.itinerary.model.Itinerary;
import com.example.travelez.backend.itinerary.model.ItineraryActivity;
import com.example.travelez.backend.itinerary.repository.ItineraryActivityRepository;
import com.example.travelez.backend.itinerary.repository.ItineraryRepository;
import com.example.travelez.backend.itinerary.repository.specification.ItinerarySpecification;
import com.example.travelez.backend.itinerary.service.ItineraryService;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.repository.PoiRepository;
import com.example.travelez.backend.poi.service.PoiService;
import com.example.travelez.backend.security.component.UserPrinciple;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.repository.UserRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("mock")
public class ItineraryServiceMockImpl implements ItineraryService {

    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryActivityRepository itineraryActivityRepository;
    private final PoiRepository poiRepository;
    private final ItineraryMapper itineraryMapper;

    private final Gson gson = new Gson();

    @Override
    public ItineraryResponse generateSmartItinerary(ItineraryCreationRequest request){
        try{
            InputStream is = new ClassPathResource(
                    "mock/itinerary-response.json"
            ).getInputStream();

            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            ItineraryResponse response = gson.fromJson(json, ItineraryResponse.class);

            response.setTempId(UUID.randomUUID().toString());

            return response;
        } catch (Exception e){
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Error generating itinerary");
        }
    }

    @Override
    public ItineraryResponse getTempItinerary(String tempId) {
        ItineraryResponse response = generateSmartItinerary(new ItineraryCreationRequest());
        response.setTempId(tempId);
        return response;
    }

    @Override
    @Transactional
    public Long saveItinerary(ItinerarySaveRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();

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
    @Transactional
    public CommonPage<ItinerarySummaryResponse> getItineraryList(Pageable pageable){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrinciple currentUser = (UserPrinciple) authentication.getPrincipal();

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
        } catch (Exception e) {
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Error fetching itinerary list");
        }
    }

    @Override
    @Transactional
    public ItineraryDetailResponse getItineraryDetail(Long itineraryId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrinciple currentUser = (UserPrinciple) authentication.getPrincipal();

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Itinerary not found"));

        if (itinerary.getTraveler().getId() != currentUser.getUserId()) {
            throw new ApiException(ResultCode.FORBIDDEN, "You are not allowed to access this itinerary");
        }

        List<ItineraryActivity> dbActivities = itineraryActivityRepository
                .findByItineraryIdOrderByItineraryDateAscStartTimeAsc(itineraryId);

        ItineraryDetailResponse response = itineraryMapper.toDetailResponseHeader(itinerary);
        List<DayPlan> dayPlans = groupActivitiesByDate(dbActivities);
        response.setDays(dayPlans);

        return response;
    }

    @Override
    @Transactional
    public void deleteItinerary(Long itineraryId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrinciple currentUser = (UserPrinciple) authentication.getPrincipal();

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Itinerary not found"));

        if (itinerary.getTraveler().getId() != currentUser.getUserId()) {
            throw new ApiException(ResultCode.FORBIDDEN, "You are not allowed to delete this itinerary");
        }

        itineraryRepository.delete(itinerary);
    }

    @Override
    public ItineraryResponse replanItinerary(ItinerarySaveRequest request) {
        try{
            InputStream is = new ClassPathResource(
                    "mock/itinerary-response.json"
            ).getInputStream();

            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            return gson.fromJson(json, ItineraryResponse.class);
        } catch (Exception e){
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Error replanning itinerary");
        }
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

}

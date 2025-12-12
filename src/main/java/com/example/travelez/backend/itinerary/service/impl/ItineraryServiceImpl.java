package com.example.travelez.backend.itinerary.service.impl;

import com.example.travelez.backend.ai.service.TravelEzAiService;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.infrastructure.gemini.GeminiService;
import com.example.travelez.backend.itinerary.dto.request.CreateItineraryRequest;
import com.example.travelez.backend.itinerary.dto.request.SaveItineraryRequest;
import com.example.travelez.backend.itinerary.dto.response.ActivityDTO;
import com.example.travelez.backend.itinerary.dto.response.DayPlan;
import com.example.travelez.backend.itinerary.dto.response.GetItineraryResponse;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.mapper.ItineraryMapper;
import com.example.travelez.backend.itinerary.model.Itinerary;
import com.example.travelez.backend.itinerary.model.ItineraryActivity;
import com.example.travelez.backend.itinerary.repository.ItineraryActivityRepository;
import com.example.travelez.backend.itinerary.repository.ItineraryRepository;
import com.example.travelez.backend.itinerary.service.ItineraryService;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.repository.PoiRepository;
import com.example.travelez.backend.poi.service.PoiService;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.repository.UserRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class ItineraryServiceImpl implements ItineraryService {
    private final PoiService poiService;
    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryActivityRepository itineraryActivityRepository;
    private final PoiRepository poiRepository;
    private final ItineraryMapper itineraryMapper;

    private final Gson gson = new Gson();
    private final TravelEzAiService travelEzAiService;

    @Override
    @Transactional(readOnly = true)
    public ItineraryResponse generateSmartItinerary(CreateItineraryRequest request) {
        List<Poi> contextPois = new ArrayList<>();
        if (request.getDestinationCities() != null) {
            for (String city : request.getDestinationCities()) {
                contextPois.addAll(poiService.getActivePoisByCity(city));
            }
        }
        log.info("Generating itinerary with context of {} POIs", contextPois.size());

        String poiContextJson = serializePois(contextPois);

        ItineraryResponse response = travelEzAiService.generateItinerary(request, poiContextJson);

        enrichItineraryDetails(response);

        response.setDestinationCities(request.getDestinationCities());

        return response;
    }

    @Override
    @Transactional
    public Long saveItinerary(SaveItineraryRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ResultCode.UNAUTHORIZED, "Người dùng chưa đăng nhập");
        }

        String currentUsername = authentication.getName();
        User traveler = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Không tìm thấy thông tin người dùng: " + currentUsername));

        ItineraryResponse aiData = request.getAiResult();
        if (aiData == null) {
            throw new ApiException(ResultCode.VALIDATION_FAILED, "Dữ liệu lộ trình từ AI không được để trống");
        }

        Itinerary itinerary = itineraryMapper.createItineraryEntity(
                request.getCreateRequest(),
                request.getAiResult(),
                traveler
        );
        Itinerary savedItinerary = itineraryRepository.save(itinerary);

        List<ItineraryActivity> activities = new ArrayList<>();
        if (request.getAiResult().getDays() != null) {
            for (DayPlan day : request.getAiResult().getDays()) {
                LocalDate currentDate = LocalDate.parse(day.getDate());

                if (day.getActivities() != null) {
                    for (ActivityDTO dto : day.getActivities()) {
                        Poi linkedPoi = null;
                        if (dto.getId() > 0) {
                            linkedPoi = poiRepository.findById(dto.getId()).orElse(null);
                        }
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
    public GetItineraryResponse getItineraryDetail(Long itineraryId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Không tìm thấy lộ trình"));

        List<ItineraryActivity> dbActivities = itineraryActivityRepository
                .findByItineraryIdOrderByItineraryDateAscStartTimeAsc(itineraryId);

        return itineraryMapper.toGetItineraryResponse(itinerary, dbActivities);
    }

    // --- HELPER METHODS ---

    private record SimplePoi(long id, String name, String type, String address, Double lat, Double lng, Object hours) {}

    private String serializePois(List<Poi> pois) {
        List<SimplePoi> simpleList = pois.stream().map(p -> new SimplePoi(
                p.getId(),
                p.getName(),
                p.getPoiType().toString(),
                p.getAddress(),
                p.getLatitude(),
                p.getLongitude(),
                p.getOpeningHour()
        )).collect(Collectors.toList());
        return gson.toJson(simpleList);
    }

    private void enrichItineraryDetails(ItineraryResponse response) {
        if (response == null || response.getDays() == null) return;

        Set<Long> poiIds = new HashSet<>();
        for (DayPlan day : response.getDays()) {
            if (day.getActivities() != null) {
                for (ActivityDTO act : day.getActivities()) {
                    if (act.getId() > 0) {
                        poiIds.add(act.getId());
                    }
                }
            }
        }

        if (poiIds.isEmpty()) return;

        List<Poi> pois = poiRepository.findAllById(poiIds);

        Map<Long, Poi> poiMap = pois.stream()
                .collect(Collectors.toMap(Poi::getId, p -> p));

        for (DayPlan day : response.getDays()) {
            if (day.getActivities() != null) {
                for (ActivityDTO act : day.getActivities()) {
                    // Lấy POI từ Map
                    Poi realPoi = poiMap.get(act.getId());

                    itineraryMapper.enrichActivityWithPoi(act, realPoi);
                }
            }
        }
    }
}
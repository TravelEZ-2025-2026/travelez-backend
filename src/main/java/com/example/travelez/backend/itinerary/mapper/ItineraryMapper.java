package com.example.travelez.backend.itinerary.mapper;

import com.example.travelez.backend.itinerary.dto.request.CreateItineraryRequest;
import com.example.travelez.backend.itinerary.dto.response.ActivityDTO;
import com.example.travelez.backend.itinerary.dto.response.DayPlan;
import com.example.travelez.backend.itinerary.dto.response.GetItineraryResponse;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.model.Itinerary;
import com.example.travelez.backend.itinerary.model.ItineraryActivity;
import com.example.travelez.backend.itinerary.model.enums.ItineraryStatus;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.users.model.User;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = {BigDecimal.class, LocalTime.class, ItineraryStatus.class})
public interface ItineraryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(ItineraryStatus.PLANNING)")
    @Mapping(target = "traveler", source = "traveler")
    @Mapping(target = "title", source = "aiData.tripTitle")
    @Mapping(target = "objectives", source = "aiData.reasoningSummary")
    @Mapping(target = "destinationCities", source = "meta.destinationCities")
    @Mapping(target = "styles", source = "meta.styles")
    @Mapping(target = "startDate", source = "meta.startDate")
    @Mapping(target = "endDate", source = "meta.endDate")
    @Mapping(target = "budget", source = "meta.budget")
    @Mapping(target = "hasKids", source = "meta.hasKids")
    @Mapping(target = "hasPets", source = "meta.hasPets")
    @Mapping(target = "companion", source = "meta.companion")
    @Mapping(target = "userNotes", source = "meta.specialNotes")
    Itinerary createItineraryEntity(CreateItineraryRequest meta, ItineraryResponse aiData, User traveler);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itinerary", source = "itinerary")
    @Mapping(target = "itineraryDate", source = "date")
    @Mapping(target = "poi", source = "linkedPoi")
    @Mapping(target = "type", source = "dto.activityType")
    @Mapping(target = "note", source = "dto.aiTip")
    @Mapping(target = "description", source = "dto.activityName")
    @Mapping(target = "startTime", source = "dto.startTime", qualifiedByName = "parseTime")
    @Mapping(target = "endTime", source = "dto.endTime", qualifiedByName = "parseTime")
    @Mapping(target = "activityCost", source = "dto.price", qualifiedByName = "parsePrice")
    @Mapping(target = "timeOfDay", expression = "java(calculateTimeOfDay(parseTime(dto.getStartTime())))")
    ItineraryActivity createActivityEntity(ActivityDTO dto, Itinerary itinerary, LocalDate date, Poi linkedPoi);

    @Mapping(source = "title", target = "tripTitle")
    @Mapping(source = "objectives", target = "reasoningSummary")
    @Mapping(source = "userNotes", target = "specialNotes")
    @Mapping(target = "days", ignore = true)
    GetItineraryResponse toGetItineraryResponseOnlyHeader(Itinerary itinerary);

    default GetItineraryResponse toGetItineraryResponse(Itinerary itinerary, List<ItineraryActivity> activities) {
        GetItineraryResponse response = toGetItineraryResponseOnlyHeader(itinerary);
        response.setDays(mapActivitiesToDayPlans(activities));
        return response;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "lat", ignore = true)
    @Mapping(target = "lng", ignore = true)
    @Mapping(target = "activityType", source = "type")
    @Mapping(target = "aiTip", source = "note")
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "formatTime")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "formatTime")
    @Mapping(target = "price", expression = "java(act.getActivityCost() != null ? act.getActivityCost().toString() : \"0\")")
    ActivityDTO toActivityDTO(ItineraryActivity act);

    @AfterMapping
    default void afterToActivityDTO(@MappingTarget ActivityDTO dto, ItineraryActivity act) {
        if (act.getPoi() != null) {
            Poi p = act.getPoi();
            dto.setId(p.getId());
            dto.setTitle(p.getName());
            dto.setAddress(p.getAddress());
            dto.setLat(p.getLatitude());
            dto.setLng(p.getLongitude());

            String rawDesc = act.getDescription() != null ? act.getDescription() : "";
            dto.setActivityName(rawDesc.contains("|||") ? rawDesc.split("\\|\\|\\|")[0] : rawDesc);

            if (p.getMedias() != null && !p.getMedias().isEmpty()) {
                dto.setImage(p.getMedias().get(0).getUrl());
            }
        } else {
            dto.setId(0L);
            dto.setTitle("Địa điểm tự do");
            dto.setActivityName(act.getDescription());
            dto.setAddress("N/A");
            dto.setLat(0.0);
            dto.setLng(0.0);
        }
    }

    @Named("parseTime")
    default LocalTime parseTime(String timeString) {
        try {
            if (timeString == null) return null;
            String cleanTime = timeString.split("-")[0].trim();
            return LocalTime.parse(cleanTime);
        } catch (Exception e) { return null; }
    }

    @Named("formatTime")
    default String formatTime(LocalTime time) {
        return time == null ? null : time.toString();
    }

    @Named("parsePrice")
    default BigDecimal parsePrice(String priceStr) {
        try {
            if (priceStr != null && !priceStr.equalsIgnoreCase("Free")) {
                String cleanPrice = priceStr.replaceAll("[^0-9.]", "");
                if (cleanPrice.isEmpty()) return BigDecimal.ZERO;
                return new BigDecimal(cleanPrice);
            }
        } catch (Exception e) { /* ignore */ }
        return BigDecimal.ZERO;
    }

    default String calculateTimeOfDay(LocalTime time) {
        if (time == null) return "ANYTIME";
        int hour = time.getHour();
        if (hour >= 5 && hour < 12) return "MORNING";
        if (hour >= 12 && hour < 18) return "AFTERNOON";
        return "EVENING";
    }

    default List<DayPlan> mapActivitiesToDayPlans(List<ItineraryActivity> dbActivities) {
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
                    .map(this::toActivityDTO)
                    .collect(Collectors.toList());

            DayPlan dayPlan = new DayPlan();
            dayPlan.setDayIndex(dayIndex++);
            dayPlan.setDate(entry.getKey().toString());
            dayPlan.setTheme("Ngày " + (dayIndex - 1));
            dayPlan.setActivities(activityDTOs);
            days.add(dayPlan);
        }
        return days;
    }

    @AfterMapping
    default void enrichActivityWithPoi(@MappingTarget ActivityDTO dto, Poi poi) {
        if (poi != null) {
            dto.setTitle(poi.getName());
            dto.setAddress(poi.getAddress());
            dto.setLat(poi.getLatitude());
            dto.setLng(poi.getLongitude());

            dto.setPrice("0"); // Cái này xử ly sau nếu có data giá từ POI

            if (poi.getMedias() != null && !poi.getMedias().isEmpty()) {
                dto.setImage(poi.getMedias().get(0).getUrl());
            } else {
                dto.setImage(null);
            }
        } else {
            if (dto.getTitle() == null) {
                dto.setTitle("Hoạt động tự do");
            }
            dto.setAddress("N/A");
            dto.setLat(0.0);
            dto.setLng(0.0);
            dto.setImage(null);
            dto.setPrice("0");
        }
    }
}

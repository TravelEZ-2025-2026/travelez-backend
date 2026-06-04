package com.example.travelez.backend.itinerary.mapper;

import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import com.example.travelez.backend.itinerary.dto.response.SharedUserSearchResponse;
import com.example.travelez.backend.itinerary.dto.response.utils.ActivityDTO;
import com.example.travelez.backend.itinerary.dto.response.ItineraryDetailResponse;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.model.Itinerary;
import com.example.travelez.backend.itinerary.model.ItineraryActivity;
import com.example.travelez.backend.itinerary.model.ItinerarySharedUser;
import com.example.travelez.backend.itinerary.model.enums.ItineraryStatus;
import com.example.travelez.backend.itinerary.util.ItineraryUtils;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.users.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", imports = { ItineraryStatus.class, ItineraryUtils.class })
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
    @Mapping(target = "estimatedTotalPrice", source = "aiData.estimatedBudget.total")
    @Mapping(target = "estimatedTransportationPrice", source = "aiData.estimatedBudget.transportation")
    @Mapping(target = "estimatedActivityPrice", source = "aiData.estimatedBudget.activity")
    @Mapping(target = "estimatedFoodAndDrinkPrice", source = "aiData.estimatedBudget.foodAndDrink")
    @Mapping(target = "estimatedAccommodationPrice", source = "aiData.estimatedBudget.accommodation")
    @Mapping(target = "estimatedCurrency", source = "aiData.estimatedBudget.currency")
    Itinerary createItineraryEntity(ItineraryCreationRequest meta, ItineraryResponse aiData, User traveler);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itinerary", source = "itinerary")
    @Mapping(target = "itineraryDate", source = "date")
    @Mapping(target = "poi", source = "linkedPoi")
    @Mapping(target = "type", source = "dto.activityType")
    @Mapping(target = "note", source = "dto.aiTip")
    @Mapping(target = "description", source = "dto.activityName")
    @Mapping(target = "startTime", source = "dto.startTime")
    @Mapping(target = "endTime", source = "dto.endTime")
    @Mapping(target = "activityCost", source = "dto.price")
    @Mapping(target = "timeOfDay", expression = "java(ItineraryUtils.calculateTimeOfDay(ItineraryUtils.parseTime(dto.getStartTime())))")
    ItineraryActivity createActivityEntity(ActivityDTO dto, Itinerary itinerary, java.time.LocalDate date,
            Poi linkedPoi);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "traveler.id")
    @Mapping(target = "isPublic", source = "isPublic")
    @Mapping(source = "title", target = "tripTitle")
    @Mapping(source = "objectives", target = "reasoningSummary")
    @Mapping(source = "userNotes", target = "specialNotes")
    @Mapping(target = "estimatedBudget.total", source = "estimatedTotalPrice")
    @Mapping(target = "estimatedBudget.transportation", source = "estimatedTransportationPrice")
    @Mapping(target = "estimatedBudget.activity", source = "estimatedActivityPrice")
    @Mapping(target = "estimatedBudget.foodAndDrink", source = "estimatedFoodAndDrinkPrice")
    @Mapping(target = "estimatedBudget.accommodation", source = "estimatedAccommodationPrice")
    @Mapping(target = "estimatedBudget.currency", source = "estimatedCurrency")
    @Mapping(target = "days", ignore = true)
    @Mapping(target = "calendarSyncedAt", source = "calendarSyncedAt")
    ItineraryDetailResponse toDetailResponseHeader(Itinerary itinerary);

    @Mapping(target = "ownerUsername", source = "traveler.username")
    ItinerarySummaryResponse toSummaryResponse(Itinerary itinerary);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "lat", ignore = true)
    @Mapping(target = "lng", ignore = true)
    @Mapping(target = "activityType", source = "type")
    @Mapping(target = "activityName", source = "description")
    @Mapping(target = "aiTip", source = "note")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    @Mapping(target = "price", expression = "java(act.getActivityCost() != null ? act.getActivityCost() : java.math.BigDecimal.ZERO)")
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
            if (p.getMedias() != null && !p.getMedias().isEmpty()) {
                dto.setImage(p.getMedias().get(0).getUrl());
            }
        } else {
            dto.setId(0L);
            String rawDesc = act.getDescription() != null ? act.getDescription() : "";
            dto.setActivityName(rawDesc.contains("|||") ? rawDesc.split("\\|\\|\\|")[0] : rawDesc);
            dto.setTitle("Địa điểm tự do");
        }
    }

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "avatarUrl", source = "user.avatar.url") // MapStruct tự động check null user.avatar
    @Mapping(target = "sharedAt", source = "createdAt")
    SharedUserSearchResponse toSharedUserSearchResponse(ItinerarySharedUser sharedUser);

}
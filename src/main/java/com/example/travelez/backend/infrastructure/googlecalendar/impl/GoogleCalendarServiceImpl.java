package com.example.travelez.backend.infrastructure.googlecalendar.impl;

import com.example.travelez.backend.config.GoogleOAuthConfig;
import com.example.travelez.backend.infrastructure.googlecalendar.GoogleCalendarService;
import com.example.travelez.backend.itinerary.model.Itinerary;
import com.example.travelez.backend.itinerary.model.ItineraryActivity;
import com.example.travelez.backend.itinerary.repository.ItineraryActivityRepository;
import com.example.travelez.backend.itinerary.repository.ItineraryRepository;
import com.example.travelez.backend.notification.dto.request.SystemNotificationRequest;
import com.example.travelez.backend.notification.enums.NotificationTargetType;
import com.example.travelez.backend.notification.model.enums.NotificationType;
import com.example.travelez.backend.notification.service.SystemNotificationService;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.model.UserOAthToken;
import com.example.travelez.backend.users.model.enums.AuthProvider;
import com.example.travelez.backend.users.repository.TokenRepository;
import com.example.travelez.backend.users.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar.events";
    private static final String CALENDAR_ID = "primary";
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final GoogleOAuthConfig googleOAuthConfig;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryActivityRepository activityRepository;
    private final SystemNotificationService systemNotificationService;

    @Override
    public boolean hasCalendarScope(Long userId) {
        return tokenRepository.findByUserIdAndProvider(userId, AuthProvider.GOOGLE)
                .map(token -> token.getScopes() != null && token.getScopes().contains(CALENDAR_SCOPE))
                .orElse(false);
    }

    @Override
    public String buildCalendarAuthorizationUrl(Long itineraryId) {
        return new GoogleAuthorizationCodeRequestUrl(
                googleOAuthConfig.getClientId(),
                googleOAuthConfig.getCalendarRedirectUri(),
                Arrays.asList(
                        "openid",
                        "email",
                        "profile",
                        CALENDAR_SCOPE
                ))
                .setAccessType("offline")
                .set("prompt", "consent")
                .set("include_granted_scopes", "true")
                .set("state", String.valueOf(itineraryId))
                .build();
    }

    @Override
    @Async
    @Transactional
    public void syncItineraryToCalendar(Long itineraryId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Itinerary itinerary = itineraryRepository.findById(itineraryId).orElse(null);
        if (itinerary == null) return;

        UserOAthToken tokenEntity = tokenRepository.findByUserIdAndProvider(userId, AuthProvider.GOOGLE).orElse(null);
        if (tokenEntity == null) return;

        try {
            UserCredentials credentials = buildCredentials(tokenEntity);
            Calendar calendarClient = buildCalendarClient(credentials);
            List<ItineraryActivity> activities = activityRepository
                    .findByItineraryIdOrderByItineraryDateAscStartTimeAsc(itineraryId);

            for (ItineraryActivity activity : activities) {
                insertActivity(calendarClient, itinerary, activity);
            }

            persistRefreshedToken(tokenEntity, credentials);

            itinerary.setCalendarSyncedAt(LocalDateTime.now());
            itineraryRepository.save(itinerary);

            sendNotification(user, itinerary, true);
        } catch (Exception e) {
            log.error("Calendar sync failed for itinerary {} user {}: {}", itineraryId, userId, e.getMessage());
            sendNotification(user, itinerary, false);
        }
    }

    private UserCredentials buildCredentials(UserOAthToken tokenEntity) {
        Date expiry = tokenEntity.getExpiresAt() != null
                ? Date.from(tokenEntity.getExpiresAt().atZone(VIETNAM_ZONE).toInstant())
                : null;
        return UserCredentials.newBuilder()
                .setClientId(googleOAuthConfig.getClientId())
                .setClientSecret(googleOAuthConfig.getClientSecret())
                .setRefreshToken(tokenEntity.getRefreshToken())
                .setAccessToken(new AccessToken(tokenEntity.getAccessToken(), expiry))
                .build();
    }

    private Calendar buildCalendarClient(UserCredentials credentials) {
        return new Calendar.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("TravelEz")
                .build();
    }

    private void persistRefreshedToken(UserOAthToken tokenEntity, UserCredentials credentials) {
        AccessToken newToken = credentials.getAccessToken();
        if (newToken != null && !newToken.getTokenValue().equals(tokenEntity.getAccessToken())) {
            tokenEntity.setAccessToken(newToken.getTokenValue());
            if (newToken.getExpirationTime() != null) {
                tokenEntity.setExpiresAt(newToken.getExpirationTime()
                        .toInstant().atZone(VIETNAM_ZONE).toLocalDateTime());
            }
            tokenRepository.save(tokenEntity);
            log.info("Google access token refreshed and persisted for user token id={}", tokenEntity.getId());
        }
    }

    private void insertActivity(Calendar calendarClient, Itinerary itinerary, ItineraryActivity activity) {
        try {
            LocalDate date = activity.getItineraryDate();
            LocalTime startTime = activity.getStartTime() != null ? activity.getStartTime() : LocalTime.of(8, 0);
            LocalTime endTime = activity.getEndTime() != null ? activity.getEndTime() : startTime.plusHours(1);

            long startMillis = LocalDateTime.of(date, startTime).atZone(VIETNAM_ZONE).toInstant().toEpochMilli();
            long endMillis = LocalDateTime.of(date, endTime).atZone(VIETNAM_ZONE).toInstant().toEpochMilli();

            String summary = activity.getDescription() != null ? activity.getDescription()
                    : (itinerary.getTitle() + " - Activity");
            String location = activity.getPoi() != null ? activity.getPoi().getName() : null;

            Event event = new Event()
                    .setSummary(summary)
                    .setLocation(location)
                    .setDescription(activity.getNote())
                    .setStart(new EventDateTime().setDateTime(new DateTime(startMillis)))
                    .setEnd(new EventDateTime().setDateTime(new DateTime(endMillis)));

            calendarClient.events().insert(CALENDAR_ID, event).execute();
        } catch (IOException e) {
            log.warn("Failed to sync activity {}: {}", activity.getId(), e.getMessage());
        }
    }

    private void sendNotification(User user, Itinerary itinerary, boolean success) {
        SystemNotificationRequest notification = SystemNotificationRequest.builder()
                .recipient(user)
                .title(success ? "Đồng bộ lịch thành công" : "Đồng bộ lịch thất bại")
                .message(success
                        ? "Lịch trình \"" + itinerary.getTitle() + "\" đã được đồng bộ lên Google Calendar."
                        : "Không thể đồng bộ lịch trình \"" + itinerary.getTitle() + "\" lên Google Calendar.")
                .type(success ? NotificationType.CALENDAR_SYNC_COMPLETED : NotificationType.CALENDAR_SYNC_FAILED)
                .targetType(NotificationTargetType.ITINERARY)
                .targetId(itinerary.getId())
                .build();
        systemNotificationService.sendSystemNotification(notification);
    }

}

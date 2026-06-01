package com.example.travelez.backend.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationStatusResponse {
    private boolean isGoogleLinked;
    private boolean hasCalendarScope;
}

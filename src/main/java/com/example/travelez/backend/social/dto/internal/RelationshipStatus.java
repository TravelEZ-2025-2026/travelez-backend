package com.example.travelez.backend.social.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipStatus {
    private boolean isFollowedByMe;
    private boolean isFollowingMe;

    public static RelationshipStatus none() {
        return new RelationshipStatus(false, false);
    }
}

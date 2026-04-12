package com.example.travelez.backend.ai.pipeline.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemanticQueryMap {
    @SerializedName("search_queries")
    private Map<String, String> searchQueries;
}

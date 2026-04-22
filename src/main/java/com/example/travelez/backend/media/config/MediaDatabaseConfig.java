package com.example.travelez.backend.media.config;

import com.example.travelez.backend.media.dto.enums.MediaTarget;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class MediaDatabaseConfig {
    @Bean
    public Map<MediaTarget, RelationTableDef> mediaTableRegistry() {
        Map<MediaTarget, RelationTableDef> registry = new EnumMap<>(MediaTarget.class);
        registry.put(MediaTarget.POST, new RelationTableDef("media_posts", "post_id"));
        registry.put(MediaTarget.COMMENT, new RelationTableDef("media_comment", "comment_id"));
        registry.put(MediaTarget.MESSAGE, new RelationTableDef("media_message", "message_id"));
        return registry;
    }

    public record RelationTableDef(String tableName, String columnName) {
    }
}

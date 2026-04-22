package com.example.travelez.backend.infrastructure.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AiConfig {
    // Bean VectorStore dành riêng cho POST
    @Bean(name = "postVectorStore")
    public VectorStore postVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("posts_vector_store") // Thay cho withTableName
                .dimensions(768)                      // Thay cho withDimensions
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)               // Thay cho withInitializeSchema
                .build();
    }
}

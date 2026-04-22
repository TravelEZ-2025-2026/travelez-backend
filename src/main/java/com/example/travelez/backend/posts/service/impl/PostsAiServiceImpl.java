package com.example.travelez.backend.posts.service.impl;

import com.example.travelez.backend.posts.dto.request.PostsSearchRequest;
import com.example.travelez.backend.posts.dto.response.PostsCreatedPayload;
import com.example.travelez.backend.posts.dto.response.PostsUpdatedPayload;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.posts.service.PostsAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostsAiServiceImpl implements PostsAiService {

    @Qualifier("postVectorStore")
    private final VectorStore postVectorStore;

    private final JdbcTemplate jdbcTemplate;

    private final EmbeddingModel embeddingModel;

    @Override
    public void saveOrUpdatePostVector(PostsCreatedPayload payload) {
        String searchableText = String.format("Title: %s\nContent: %s", payload.getTitle(), payload.getContent());

        Document document = new Document(
                searchableText,
                Map.of("postId", payload.getPostId(),
                        "status", payload.getStatus().toString(),
                        "createdAt", payload.getCreatedAt().toString()
                ) // Lưu postId làm metadata để lấy ra
        );

        // Spring AI tự động gọi Gemini Embedding API để tạo vector và lưu vào DB
        postVectorStore.add(List.of(document));
    }

    @Override 
    public void updatePostVector(PostsUpdatedPayload payload) {
        if (payload.getStatus() == null) return;
        // Dùng hàm jsonb_build_object của PostgreSQL để tạo JSON ngay trong SQL
        String sql = "UPDATE posts_vector_store " +
        "SET metadata = (metadata::jsonb || jsonb_build_object('status', ?::text))::json " +
        "WHERE CAST(metadata->>'postId' AS BIGINT) = ?";

        // Chỉ truyền value dạng String bình thường
        Object[] params = new Object[] {
            payload.getStatus().toString(), 
            payload.getPostId()
        };

        jdbcTemplate.update(sql, params);
    }

    @Override
    public List<Long> searchSimilarPosts(String query, int topK) {
        Filter.Expression publicFilter = new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key("status"), new Filter.Value(PostStatus.PUBLISHED.toString()));
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.7)
                .filterExpression(publicFilter)
                .build();
        List<Document> documents = postVectorStore.similaritySearch(searchRequest);
        return documents.stream()
                .map(doc -> Long.valueOf(doc.getMetadata().get("postId").toString()))
                .toList();
    }

    @Override
    public List<Long> searchWithPagination(PostsSearchRequest request, Pageable pageable) {

        float[] embedding = embeddingModel.embed(request.getKeyword());

        String vectorString = Arrays.toString(embedding);

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sql.append("SELECT CAST(metadata->>'postId' AS BIGINT) AS postId FROM posts_vector_store ");
        sql.append("WHERE metadata->>'status' = 'PUBLISHED' ");

        if (request.getFromDate() != null) {
            sql.append("AND metadata->>'createdAt' >= ? ");
            params.add(request.getFromDate().toString());
        }
        if (request.getToDate() != null) {
            sql.append("AND metadata->>'createdAt' <= ? ");
            params.add(request.getToDate().toString());
        }

        sql.append("ORDER BY embedding <=> ?::vector ");
        params.add(vectorString);

        sql.append("LIMIT ? OFFSET ? ");
        params.add(pageable.getPageSize());
        params.add(pageable.getPageNumber() * pageable.getPageSize());

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> rs.getLong("postId"),
                params.toArray()
        );
    }

    @Override
    public void deletePostVector(Long postId) {
        try {
            String sql = "DELETE FROM posts_vector_store WHERE CAST(metadata->>'postId' AS BIGINT) = ?";
            jdbcTemplate.update(sql, postId);
        } catch (Exception e) {
            log.error("Failed to delete post vector: {}", e.getMessage());
        }
    }
}

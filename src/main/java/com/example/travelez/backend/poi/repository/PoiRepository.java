package com.example.travelez.backend.poi.repository;

import com.example.travelez.backend.ai.pipeline.model.PoiVectorResult;
import com.example.travelez.backend.poi.model.Place;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.model.enums.PlaceStatus;
import com.example.travelez.backend.poi.model.enums.PoiStatus;
import com.example.travelez.backend.poi.repository.projection.GeneralStatPoiProjection;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PoiRepository extends JpaRepository<Poi, Long>, JpaSpecificationExecutor<Poi> {
    Optional<Poi> findByIdAndSystemStatus(Long id, PoiStatus systemStatus);

    List<Poi> findByPlaceAndSystemStatusAndStatus(
            Place place,
            PoiStatus systemStatus,
            PlaceStatus status
    );

    @Query("SELECT poi.id, m FROM Poi poi JOIN poi.medias m WHERE poi.id IN :placeOfInterestIds")
    List<Object[]> findAllMediasByPoiIds(@Param("placeOfInterestIds") List<Long> placeOfInterestIds);

    @Query(value = """
            SELECT 
                p.id AS id, 
                p.name AS name, 
                CAST(p.poi_type AS VARCHAR) AS poiType, 
                p.poi_type_detail AS poiTypeDetail, 
                p.address AS address, 
                p.latitude AS latitude, 
                p.longitude AS longitude, 
                CAST(p.opening_hour AS TEXT) AS openingHour, 
                p.google_maps_url AS googleMapsUrl, 
                COALESCE(p.rating, 3.0) AS rating, 
                p.description AS description, 
                p.semantic_text AS semanticText
            FROM place_of_interest p
            WHERE p.poi_type = :category 
              AND p.deleted_at IS NULL
            ORDER BY p.gemini_vector <=> cast(:queryVector as vector) ASC 
            LIMIT :kLimit
            """, nativeQuery = true)
    List<PoiVectorResult> findTopPoisByCategoryAndVector(
            @Param("category") String category,
            @Param("queryVector") String queryVector,
            @Param("kLimit") int kLimit
    );

    @Query(value = """
            SELECT p.* FROM place_of_interest p 
            WHERE p.system_status = 'ACTIVE' 
              AND p.status = 'OPERATIONAL'
              AND (:placeId IS NULL OR p.place_id = :placeId)
              AND (cast(:poiType as text) IS NULL OR p.poi_type = cast(:poiType as poi_type_enum))
            ORDER BY p.description_vector <=> cast(:embedding as vector) 
            LIMIT :limit
            """, nativeQuery = true)
    List<Poi> findBySemanticSearch(
            @Param("embedding") String embedding,
            @Param("placeId") Long placeId,
            @Param("poiType") String poiType,
            @Param("limit") int limit
    );

    boolean existsByIdAndSystemStatus(Long id, PoiStatus systemStatus);

    @Query("SELECT " +
            "COUNT(p) AS total, " +
            "COALESCE(SUM(CASE WHEN p.systemStatus = 'ACTIVE' THEN 1 ELSE 0 END), 0) AS activeCount, " +
            "COALESCE(SUM(CASE WHEN p.systemStatus = 'BANNED' THEN 1 ELSE 0 END), 0) AS bannedCount, " +
            "COALESCE(SUM(CASE WHEN p.status = 'OPERATIONAL' THEN 1 ELSE 0 END), 0) AS operationalCount, " +
            "COALESCE(SUM(CASE WHEN p.status <> 'OPERATIONAL' THEN 1 ELSE 0 END), 0) AS closedCount, " +
            "COALESCE(SUM(p.reviewCount), 0) AS totalReview," +
            "COALESCE(avg (p.rating), 0) AS averageRating " +
            "FROM Poi p " +
            "WHERE (:placeId IS NULL OR p.place.id = :placeId) " +
            "AND (:wardId IS NULL OR p.ward.id = :wardId)")
    GeneralStatPoiProjection getGeneralStats(@Param("placeId") Long placeId, @Param("wardId") Long wardId);

    // 2. Thống kê số lượng theo từng loại hình (CAFE, HOTEL, RESTAURANT...)
    @Query("SELECT p.poiType, COUNT(p) " +
            "FROM Poi p " +
            "WHERE (:placeId IS NULL OR p.place.id = :placeId) " +
            "AND (:wardId IS NULL OR p.ward.id = :wardId) " +
            "GROUP BY p.poiType")
    List<Object[]> getCountGroupByType(@Param("placeId") Long placeId, @Param("wardId") Long wardId);

    @NotNull
    @EntityGraph(attributePaths = {"ward", "place"})
    Page<Poi> findAll(Specification<Poi> specification, @NotNull Pageable pageable);

    @EntityGraph(attributePaths = {"ward", "place", "medias"})
    @Query("SELECT p FROM Poi p WHERE p.id = :poiId")
    Optional<Poi> findByPoiId(@Param("poiId") Long poiId);

    @Modifying
    @Query("UPDATE Poi p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void softDeleteById(@Param("id") long id);

    @Query("""
           SELECT DISTINCT p FROM Poi p 
           LEFT JOIN FETCH p.medias 
           LEFT JOIN FETCH p.place 
           LEFT JOIN FETCH p.ward 
           WHERE p.id IN :ids
           """)
    List<Poi> findPoisWithDetailsByIds(@Param("ids") Set<Long> ids);

    @Query(value = "SELECT cast(gemini_vector as text) FROM place_of_interest WHERE id = :poiId", nativeQuery = true)
    Optional<String> findGeminiVectorStringById(@Param("poiId") Long poiId);

    @Query(value = """
            -- GIAI ĐOẠN 1: Lấy Top 100 POI sát với ý định của Prompt nhất
            WITH candidate_pois AS (
                SELECT 
                    p.id AS id, 
                    p.name AS name, 
                    CAST(p.poi_type AS VARCHAR) AS poiType, 
                    p.poi_type_detail AS poiTypeDetail, 
                    p.address AS address, 
                    p.latitude AS latitude, 
                    p.longitude AS longitude, 
                    CAST(p.opening_hour AS TEXT) AS openingHour, 
                    p.google_maps_url AS googleMapsUrl, 
                    COALESCE(p.rating, 3.0) AS rating, 
                    p.description AS description, 
                    p.semantic_text AS semanticText,
                    p.gemini_vector <=> cast(:queryVector as vector) AS query_distance,
                    p.gemini_vector
                FROM place_of_interest p
                WHERE p.poi_type = :category 
                  AND p.deleted_at IS NULL
                ORDER BY query_distance ASC 
                LIMIT 100
            )
            -- GIAI ĐOẠN 2: Chấm điểm lại với 30% trọng số ưu tiên sở thích User
            SELECT 
                c.id, c.name, c.poiType, c.poiTypeDetail, c.address, c.latitude, c.longitude, 
                c.openingHour, c.googleMapsUrl, c.rating, c.description, c.semanticText
            FROM candidate_pois c
            ORDER BY 
                (0.7 * c.query_distance) + (0.3 * (c.gemini_vector <=> cast(:userVector as vector))) ASC 
            LIMIT :kLimit
            """, nativeQuery = true)
    List<PoiVectorResult> findTopPoisByCategoryWithReRanking(
            @Param("category") String category,
            @Param("queryVector") String queryVector,
            @Param("userVector") String userVector,
            @Param("kLimit") int kLimit
    );
}

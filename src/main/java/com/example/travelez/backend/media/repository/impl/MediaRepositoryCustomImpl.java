package com.example.travelez.backend.media.repository.impl;

import com.example.travelez.backend.media.config.MediaDatabaseConfig.RelationTableDef;
import com.example.travelez.backend.media.dto.enums.MediaTarget;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.media.repository.MediaRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MediaRepositoryCustomImpl implements MediaRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final Map<MediaTarget, RelationTableDef> tableRegistry;

    @Override
    public List<Media> batchInsertMedias(List<Media> medias) {
        String sql = "INSERT INTO media (url, type, cloud_name) VALUES (:url, :type, :cloudName)";
        SqlParameterSource[] batchArgs = medias.stream().map(media -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("url", media.getUrl());
            params.addValue("type", media.getType().name(), Types.OTHER);
            params.addValue("cloudName", media.getCloudName());
            return params;
        }).toArray(SqlParameterSource[]::new);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.batchUpdate(sql, batchArgs, keyHolder);

        // List<Long> generatedIds = new ArrayList<>();
        // for(Map<String, Object> key : keyHolder.getKeyList()) {
        //     Number id = (Number) key.get("id");
        //     generatedIds.add(id.longValue());
        // }
        List<Map<String, Object>> keyList = keyHolder.getKeyList();
        for (int i = 0; i < medias.size(); i++) {
            Number id = (Number) keyList.get(i).get("id");
            medias.get(i).setId(id.longValue());
        }

        return medias;
    }

    @Override
    public void batchInsertMediaRelation(MediaTarget target, Long entityId, List<Long> mediaIds) {
        RelationTableDef def = tableRegistry.get(target);
        if (def == null) throw new IllegalArgumentException("Invalid media target: " + target);
        String sql = String.format("INSERT INTO %s (%s, media_id) VALUES (?, ?)", def.tableName(), def.columnName());
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, entityId);
                ps.setLong(2, mediaIds.get(i));
            }

            @Override
            public int getBatchSize() {
                return mediaIds.size();
            }
        });
    }
}

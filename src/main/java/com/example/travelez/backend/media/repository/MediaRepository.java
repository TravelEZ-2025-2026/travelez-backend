package com.example.travelez.backend.media.repository;

import com.example.travelez.backend.media.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query("SELECT p.id, m FROM Posts p JOIN p.medias m WHERE p.id IN :postIds")
    List<Object[]> findAllByPostIds(@Param("postIds") List<Long> postIds);

    //    raw[0] la id, raw[1] la cloud_name
    @Query(value = """
            WITH RECURSIVE comment_tree AS ( 
                -- 1. Lấy comment gốc (Node cha)
                SELECT id FROM comment WHERE id =:commentId
            
                UNION ALL 
            
                -- 2. Đệ quy tìm tất cả các con, cháu chắt...
                SELECT c.id
                FROM comment c
                INNER JOIN comment_tree ct ON c.parent_comment_id = ct.id
             )
            
            SELECT m.id, m.cloud_name
            FROM media m
            INNER JOIN media_comment mc ON m.id = mc.media_id
            WHERE mc.comment_id IN (SELECT id FROM comment_tree)
            """, nativeQuery = true)
    List<Object[]> findAllInCommentTree(Long commentId);

    @Query(value = """
              select m.* from media m
              inner join media_posts mp on m.id = mp.media_id
            where mp.post_id = :postId
            
              union 
            
              select m.* from media m
              inner join media_comment mc on m.id = mc.media_id
              inner join comment c on mc.comment_id = c.id
            where c.post_id = :postId
            """, nativeQuery = true)
    List<Media> findAllByPostId(@Param("postId") Long postId);

}

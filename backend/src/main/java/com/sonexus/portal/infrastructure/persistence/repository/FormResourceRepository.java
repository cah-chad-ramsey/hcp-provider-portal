package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.FormResourceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormResourceRepository extends JpaRepository<FormResourceEntity, Long> {

    /**
     * Find all forms with pagination and optional filters
     */
    @Query("SELECT f FROM FormResourceEntity f WHERE " +
           "(:programId IS NULL OR f.programId = :programId) AND " +
           "(:category IS NULL OR f.category = :category) AND " +
           "(:searchTerm IS NULL OR LOWER(f.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(f.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY f.uploadedAt DESC")
    Page<FormResourceEntity> findAllWithFilters(
            @Param("programId") Long programId,
            @Param("category") String category,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    /**
     * Find all versions of a form
     */
    @Query("SELECT f FROM FormResourceEntity f WHERE f.parentId = :parentId OR f.id = :parentId ORDER BY f.version DESC")
    List<FormResourceEntity> findAllVersions(@Param("parentId") Long parentId);

    /**
     * Find forms by program
     */
    List<FormResourceEntity> findByProgramIdOrderByUploadedAtDesc(Long programId);

    /**
     * Find forms by category
     */
    List<FormResourceEntity> findByCategoryOrderByUploadedAtDesc(String category);

    /**
     * Count forms by category
     */
    long countByCategory(String category);
}

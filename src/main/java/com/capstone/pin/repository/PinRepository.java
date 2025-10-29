package com.capstone.pin.repository;

import com.capstone.pin.entity.Pin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PinRepository extends JpaRepository<Pin, Long> {

    /** 사용자 ID로 핀 목록 조회 */
    List<Pin> findByUserId(Long userId);

    /** 사용자 ID와 핀 ID로 조회 (권한 확인용) */
    Optional<Pin> findByIdAndUserId(Long pinId, Long userId);

    /** 특정 위치 근처의 핀 조회 (간단한 경계 박스 검색) */
    @Query("SELECT p FROM Pin p WHERE " +
           "p.latitude BETWEEN :minLat AND :maxLat AND " +
           "p.longitude BETWEEN :minLon AND :maxLon")
    List<Pin> findPinsNearLocation(
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon
    );

}

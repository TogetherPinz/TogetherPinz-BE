package com.capstone.pin.service;

import com.capstone.pin.dto.*;
import com.capstone.pin.entity.Pin;
import com.capstone.pin.repository.PinRepository;
import com.capstone.user.entity.User;
import com.capstone.user.service.UserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PinService {

    private final PinRepository pinRepository;
    private final UserCacheService userCacheService;

    /** 핀 생성 */
    @Transactional
    public PinInfo createPin(Long userId, CreatePinRequest request) {
        // 사용자 조회
        User user = userCacheService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 핀 생성
        Pin pin = Pin.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .user(user)
                .notificationRadius(request.getNotificationRadius())
                .currentMemberCount(1)
                .build();

        Pin savedPin = pinRepository.save(pin);
        log.info("핀 생성 성공: pinId={}, userId={}", savedPin.getId(), userId);

        return PinInfo.fromEntity(savedPin);
    }

    /** 핀 조회 (단건) */
    public PinInfo getPin(Long pinId) {
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없습니다."));
        return PinInfo.fromEntity(pin);
    }

    /** 사용자의 핀 목록 조회 */
    public List<PinInfo> getUserPins(Long userId) {
        List<Pin> pins = pinRepository.findByUserId(userId);
        return pins.stream()
                .map(PinInfo::fromEntity)
                .collect(Collectors.toList());
    }

    /** 모든 핀 조회 */
    public List<PinInfo> getAllPins() {
        List<Pin> pins = pinRepository.findAll();
        return pins.stream()
                .map(PinInfo::fromEntity)
                .collect(Collectors.toList());
    }

    /** 핀 수정 */
    @Transactional
    public PinInfo updatePin(Long userId, Long pinId, UpdatePinRequest request) {
        // 핀 조회 및 권한 확인
        Pin pin = pinRepository.findByIdAndUserId(pinId, userId)
                .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없거나 수정 권한이 없습니다."));

        // 핀 정보 수정
        pin.updatePin(
                request.getTitle(),
                request.getDescription(),
                request.getLatitude(),
                request.getLongitude(),
                request.getNotificationRadius()
        );

        Pin updatedPin = pinRepository.save(pin);
        log.info("핀 수정 성공: pinId={}, userId={}", pinId, userId);

        return PinInfo.fromEntity(updatedPin);
    }

    /** 핀 삭제 */
    @Transactional
    public void deletePin(Long userId, Long pinId) {
        // 핀 조회 및 권한 확인
        Pin pin = pinRepository.findByIdAndUserId(pinId, userId)
                .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없거나 삭제 권한이 없습니다."));

        pinRepository.delete(pin);
        log.info("핀 삭제 성공: pinId={}, userId={}", pinId, userId);
    }

    /** 특정 위치 근처의 핀 조회 */
    public List<PinInfo> getPinsNearLocation(Double latitude, Double longitude, Double radiusKm) {
        // 간단한 경계 박스 계산 (1도 ≈ 111km)
        Double latDelta = radiusKm / 111.0;
        Double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        List<Pin> pins = pinRepository.findPinsNearLocation(
                latitude - latDelta,
                latitude + latDelta,
                longitude - lonDelta,
                longitude + lonDelta
        );

        return pins.stream()
                .map(PinInfo::fromEntity)
                .collect(Collectors.toList());
    }

}

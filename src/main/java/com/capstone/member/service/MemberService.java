package com.capstone.member.service;

import com.capstone.member.dto.*;
import com.capstone.member.entity.Member;
import com.capstone.member.enums.MemberRole;
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

/**
 * MemberService
 * 멤버 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 핀에 소속된 멤버의 생성, 조회, 수정, 삭제 및 추가/제거 기능을 제공합니다.
 * 멤버 관련 데이터는 MemberCacheService를 통해 접근합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberCacheService memberCacheService;
    private final PinRepository pinRepository;
    private final UserCacheService userCacheService;

    private static final int MAX_GROUP_MEMBERS = 8;

    /** 멤버 생성 (사용자가 핀을 통해 그룹에 참여) */
    @Transactional
    public MemberInfo createMember(Long userId, CreateMemberRequest request) {
        // 사용자 조회
        User user = userCacheService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 핀 조회
        Pin pin = pinRepository.findById(request.getPinId())
                .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없습니다."));

        // 이미 핀에 속해있는지 확인
        if (memberCacheService.existsByPinIdAndUserId(request.getPinId(), userId)) {
            throw new IllegalArgumentException("이미 해당 핀에 속해있습니다.");
        }

        // 핀 멤버 수 확인
        Long currentMemberCount = memberCacheService.countByPinId(request.getPinId());
        if (currentMemberCount >= MAX_GROUP_MEMBERS) {
            throw new IllegalArgumentException("그룹 멤버가 최대 인원(" + MAX_GROUP_MEMBERS + "명)에 도달했습니다.");
        }

        // 멤버 생성 (핀 소유자는 OWNER 역할)
        Member member = Member.builder()
                .pin(pin)
                .user(user)
                .role(MemberRole.OWNER)
                .build();

        // 핀의 멤버 수 증가
        pin.incrementMemberCount();
        pinRepository.save(pin);

        Member savedMember = memberCacheService.saveMember(member);
        log.info("그룹 생성 성공: groupId={}, userId={}, pinId={}", savedMember.getId(), userId, request.getPinId());

        return MemberInfo.from(savedMember);
    }

    /** 사용자가 속한 모든 그룹 조회 */
    public List<MemberInfo> getMembersByUserId(Long userId) {
        List<Member> members = memberCacheService.getMembersByUserId(userId);
        return members.stream()
                .map(MemberInfo::from)
                .collect(Collectors.toList());
    }

    /** 특정 핀에 속한 모든 그룹 멤버 조회 */
    public List<MemberInfo> getMembersByPinId(Long pinId) {
        List<Member> members = memberCacheService.getMembersByPinId(pinId);
        return members.stream()
                .map(MemberInfo::from)
                .collect(Collectors.toList());
    }

    /** 그룹 단건 조회 */
    public MemberInfo getMember(Long memberId) {
        Member member = memberCacheService.getMemberById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        return MemberInfo.from(member);
    }


    /** 그룹 삭제 (그룹 탈퇴) */
    @Transactional
    public void deleteMember(Long userId, Long memberId) {
        // 그룹 조회
        Member member = memberCacheService.getMemberById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        // 권한 확인 (본인만 탄퇴 가능)
        if (!member.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 그룹 멤버십만 삭제할 수 있습니다.");
        }

        // 핀의 멤버 수 감소
        Pin pin = member.getPin();
        pin.decrementMemberCount();
        pinRepository.save(pin);

        memberCacheService.deleteMember(member);
        log.info("그룹 삭제 성공: groupId={}, userId={}", memberId, userId);
    }

    /** 그룹 멤버 추가 */
    @Transactional
    public MemberInfo addMember(Long requestUserId, Long pinId, AddMemberRequest request) {
        // 요청자 확인
        Member member = memberCacheService.getMemberByPinIdAndUserId(pinId, requestUserId)
                .orElseThrow(() -> new IllegalArgumentException("핀 멤버를 찾을 수 없습니다."));

        // 핀 조회
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없습니다."));

        // 요청자가 핀의 소유자인지 확인
        if (member.getRole() == MemberRole.MEMBER) {
            throw new IllegalArgumentException("핀 소유자만 멤버를 추가할 수 있습니다.");
        }

        // 추가할 사용자 조회
        User user = userCacheService.getUserByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("추가할 사용자를 찾을 수 없습니다."));

        // 이미 그룹에 속해있는지 확인
        if (memberCacheService.existsByPinIdAndUserId(pinId, user.getId())) {
            throw new IllegalArgumentException("이미 해당 그룹에 속해있는 사용자입니다.");
        }

        // 그룹 멤버 수 확인
        Long currentMemberCount = memberCacheService.countByPinId(pinId);
        if (currentMemberCount >= MAX_GROUP_MEMBERS) {
            throw new IllegalArgumentException("그룹 멤버가 최대 인원(" + MAX_GROUP_MEMBERS + "명)에 도달했습니다.");
        }

        // 그룹 멤버 추가 (MEMBER 역할)
        Member newMember = Member.builder()
                .pin(pin)
                .user(user)
                .role(MemberRole.MEMBER)
                .build();

        memberCacheService.saveMember(newMember);

        // 핀의 멤버 수 증가
        pin.incrementMemberCount();
        pinRepository.save(pin);

        Member savedMember = memberCacheService.saveMember(member);
        log.info("그룹 멤버 추가 성공: pinId={}, newUserId={}", pinId, user.getId());

        return MemberInfo.from(savedMember);
    }

    /** 그룹 멤버 제거 */
    @Transactional
    public void removeMember(Long requestUserId, Long pinId, Long memberId) {
        // 요청자 확인
        Member requester = memberCacheService.getMemberByPinIdAndUserId(pinId, requestUserId)
                .orElseThrow(() -> new IllegalArgumentException("핀 멤버를 찾을 수 없습니다."));

        // 핀 조회
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없습니다."));

        // 요청자가 핀의 소유자인지 확인
        if (requester.getRole() == MemberRole.MEMBER) {
            throw new IllegalArgumentException("핀 소유자만 멤버를 제거할 수 있습니다.");
        }

        // 제거할 멤버의 그룹 조회
        Member member = memberCacheService.getMemberByPinIdAndUserId(pinId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 그룹에 속해있지 않습니다."));

        // 소유자는 제거할 수 없음
        if (member.getRole() == MemberRole.OWNER) {
            throw new IllegalArgumentException("그룹 소유자는 제거할 수 없습니다.");
        }

        // 핀의 멤버 수 감소
        pin.decrementMemberCount();
        pinRepository.save(pin);

        memberCacheService.deleteMember(member);
        log.info("그룹 멤버 제거 성공: pinId={}, removedUserId={}", pinId, memberId);
    }

    /** 핀 탈퇴 (내가 속한 핀만 탈퇴 가능, 내가 Owner일 경우 핀이 삭제됨) */
    @Transactional
    public void leavePinGroup(Long userId, Long pinId) {
        // 핀 조회
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없습니다."));

        // 사용자가 핀에 속해있는지 확인
        Member member = memberCacheService.getMemberByPinIdAndUserId(pinId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 핀에 속해있지 않습니다."));

        // Owner인 경우 핀 삭제
        if (member.getRole() == MemberRole.OWNER) {
            // 핀에 속한 모든 멤버 삭제
            List<Member> members = memberCacheService.getMembersByPinId(pinId);
            for (Member m : members) {
                memberCacheService.deleteMember(m);
            }
            // 핀 삭제
            pinRepository.delete(pin);
            log.info("핀 탈퇴(삭제) 성공: pinId={}, userId={}", pinId, userId);
        } else {
            // Member인 경우 멤버만 제거
            pin.decrementMemberCount();
            pinRepository.save(pin);
            memberCacheService.deleteMember(member);
            log.info("핀 탈퇴 성공: pinId={}, userId={}", pinId, userId);
        }
    }
}

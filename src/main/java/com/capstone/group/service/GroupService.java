package com.capstone.group.service;

import com.capstone.group.dto.*;
import com.capstone.group.entity.Group;
import com.capstone.group.enums.MemberRole;
import com.capstone.group.repository.GroupRepository;
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
public class GroupService {

    private final GroupRepository groupRepository;
    private final PinRepository pinRepository;
    private final UserCacheService userCacheService;

    private static final int MAX_GROUP_MEMBERS = 8;

    /** 그룹 생성 (사용자가 핀을 통해 그룹에 참여) */
    @Transactional
    public GroupInfo createGroup(Long userId, CreateGroupRequest request) {
        // 사용자 조회
        User user = userCacheService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 핀 조회
        Pin pin = pinRepository.findById(request.getPinId())
                .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없습니다."));

        // 핀 소유자가 맞는지 확인
        if (!pin.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("핀 소유자만 그룹을 생성할 수 있습니다.");
        }

        // 이미 그룹에 속해있는지 확인
        if (groupRepository.existsByPinIdAndUserId(request.getPinId(), userId)) {
            throw new IllegalArgumentException("이미 해당 그룹에 속해있습니다.");
        }

        // 그룹 멤버 수 확인
        Long currentMemberCount = groupRepository.countByPinId(request.getPinId());
        if (currentMemberCount >= MAX_GROUP_MEMBERS) {
            throw new IllegalArgumentException("그룹 멤버가 최대 인원(" + MAX_GROUP_MEMBERS + "명)에 도달했습니다.");
        }

        // 그룹 생성 (핀 소유자는 OWNER 역할)
        Group group = Group.builder()
                .pin(pin)
                .user(user)
                .role(MemberRole.OWNER)
                .build();

        // 핀의 멤버 수 증가
        pin.incrementMemberCount();
        pinRepository.save(pin);

        Group savedGroup = groupRepository.save(group);
        log.info("그룹 생성 성공: groupId={}, userId={}, pinId={}", savedGroup.getId(), userId, request.getPinId());

        return GroupInfo.from(savedGroup);
    }

    /** 사용자가 속한 모든 그룹 조회 */
    public List<GroupInfo> getGroupsByUserId(Long userId) {
        List<Group> groups = groupRepository.findByUserId(userId);
        return groups.stream()
                .map(GroupInfo::from)
                .collect(Collectors.toList());
    }

    /** 특정 핀에 속한 모든 그룹 멤버 조회 */
    public List<GroupInfo> getGroupsByPinId(Long pinId) {
        List<Group> groups = groupRepository.findByPinId(pinId);
        return groups.stream()
                .map(GroupInfo::from)
                .collect(Collectors.toList());
    }

    /** 그룹 단건 조회 */
    public GroupInfo getGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        return GroupInfo.from(group);
    }

    /** 그룹 수정 (핀 변경) */
    @Transactional
    public GroupInfo updateGroup(Long userId, Long groupId, UpdateGroupRequest request) {
        // 그룹 조회
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        // 권한 확인 (소유자만 수정 가능)
        if (!group.getUser().getId().equals(userId) || group.getRole() != MemberRole.OWNER) {
            throw new IllegalArgumentException("그룹 소유자만 수정할 수 있습니다.");
        }

        // 새로운 핀 조회
        Pin newPin = pinRepository.findById(request.getPinId())
                .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없습니다."));

        // 새로운 핀의 소유자가 맞는지 확인
        if (!newPin.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인 소유의 핀으로만 변경할 수 있습니다.");
        }

        // 기존 핀의 멤버 수 감소
        Pin oldPin = group.getPin();
        oldPin.decrementMemberCount();
        pinRepository.save(oldPin);

        // 새로운 핀의 멤버 수 증가
        newPin.incrementMemberCount();
        pinRepository.save(newPin);

        // 그룹의 핀 변경 (리플렉션 또는 새 엔티티 생성 필요)
        // 여기서는 새 그룹 엔티티를 생성하는 방식 사용
        Group updatedGroup = Group.builder()
                .pin(newPin)
                .user(group.getUser())
                .role(group.getRole())
                .build();
        
        groupRepository.delete(group);
        Group savedGroup = groupRepository.save(updatedGroup);
        log.info("그룹 수정 성공: groupId={}, newPinId={}", groupId, request.getPinId());

        return GroupInfo.from(savedGroup);
    }

    /** 그룹 삭제 (그룹 탈퇴) */
    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        // 그룹 조회
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        // 권한 확인 (본인만 탈퇴 가능)
        if (!group.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 그룹 멤버십만 삭제할 수 있습니다.");
        }

        // 핀의 멤버 수 감소
        Pin pin = group.getPin();
        pin.decrementMemberCount();
        pinRepository.save(pin);

        groupRepository.delete(group);
        log.info("그룹 삭제 성공: groupId={}, userId={}", groupId, userId);
    }

    /** 그룹 멤버 추가 */
    @Transactional
    public GroupInfo addMember(Long requestUserId, Long pinId, AddMemberRequest request) {
        // 요청자 확인
        User requestUser = userCacheService.getUserById(requestUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 핀 조회
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없습니다."));

        // 요청자가 핀의 소유자인지 확인
        if (!pin.getUser().getId().equals(requestUserId)) {
            throw new IllegalArgumentException("핀 소유자만 멤버를 추가할 수 있습니다.");
        }

        // 추가할 사용자 조회
        User newMember = userCacheService.getUserById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("추가할 사용자를 찾을 수 없습니다."));

        // 이미 그룹에 속해있는지 확인
        if (groupRepository.existsByPinIdAndUserId(pinId, request.getUserId())) {
            throw new IllegalArgumentException("이미 해당 그룹에 속해있는 사용자입니다.");
        }

        // 그룹 멤버 수 확인
        Long currentMemberCount = groupRepository.countByPinId(pinId);
        if (currentMemberCount >= MAX_GROUP_MEMBERS) {
            throw new IllegalArgumentException("그룹 멤버가 최대 인원(" + MAX_GROUP_MEMBERS + "명)에 도달했습니다.");
        }

        // 그룹 멤버 추가 (MEMBER 역할)
        Group group = Group.builder()
                .pin(pin)
                .user(newMember)
                .role(MemberRole.MEMBER)
                .build();

        // 핀의 멤버 수 증가
        pin.incrementMemberCount();
        pinRepository.save(pin);

        Group savedGroup = groupRepository.save(group);
        log.info("그룹 멤버 추가 성공: pinId={}, newUserId={}", pinId, request.getUserId());

        return GroupInfo.from(savedGroup);
    }

    /** 그룹 멤버 제거 */
    @Transactional
    public void removeMember(Long requestUserId, Long pinId, Long memberId) {
        // 요청자 확인
        User requestUser = userCacheService.getUserById(requestUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 핀 조회
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new IllegalArgumentException("핀을 찾을 수 없습니다."));

        // 요청자가 핀의 소유자인지 확인
        if (!pin.getUser().getId().equals(requestUserId)) {
            throw new IllegalArgumentException("핀 소유자만 멤버를 제거할 수 있습니다.");
        }

        // 제거할 멤버의 그룹 조회
        Group group = groupRepository.findByPinIdAndUserId(pinId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 그룹에 속해있지 않습니다."));

        // 소유자는 제거할 수 없음
        if (group.getRole() == MemberRole.OWNER) {
            throw new IllegalArgumentException("그룹 소유자는 제거할 수 없습니다.");
        }

        // 핀의 멤버 수 감소
        pin.decrementMemberCount();
        pinRepository.save(pin);

        groupRepository.delete(group);
        log.info("그룹 멤버 제거 성공: pinId={}, removedUserId={}", pinId, memberId);
    }
}

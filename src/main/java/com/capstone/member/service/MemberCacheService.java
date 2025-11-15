package com.capstone.member.service;

import com.capstone.member.entity.Member;
import com.capstone.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * MemberCacheService
 * 멤버 엔티티의 CRUD 작업에 대한 캐싱 기능을 제공합니다.
 * 데이터베이스와의 직접적인 상호작용은 MemberRepository를 통해 이루어집니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCacheService {

    private final MemberRepository memberRepository;

    /** 멤버 ID로 멤버 조회 */
    public Optional<Member> getMemberById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    /** 특정 핀에 속한 모든 멤버 조회 */
    public List<Member> getMembersByPinId(Long pinId) {
        return memberRepository.findByPinId(pinId);
    }

    /** 특정 사용자가 속한 모든 멤버 조회 */
    public List<Member> getMembersByUserId(Long userId) {
        return memberRepository.findByUserId(userId);
    }

    /** 특정 핀에 속한 특정 사용자의 멤버 조회 */
    public Optional<Member> getMemberByPinIdAndUserId(Long pinId, Long userId) {
        return memberRepository.findByPinIdAndUserId(pinId, userId);
    }

    /** 특정 핀의 멤버 수 조회 */
    public Long countByPinId(Long pinId) {
        return memberRepository.countByPinId(pinId);
    }

    /** 특정 사용자가 특정 핀에 속해있는지 확인 */
    public boolean existsByPinIdAndUserId(Long pinId, Long userId) {
        return memberRepository.existsByPinIdAndUserId(pinId, userId);
    }

    /** 멤버 저장 */
    @Transactional
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }

    /** 멤버 삭제 */
    @Transactional
    public void deleteMember(Member member) {
        memberRepository.delete(member);
    }
}

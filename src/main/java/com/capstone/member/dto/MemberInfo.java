package com.capstone.member.dto;

import com.capstone.member.enums.MemberRole;
import com.capstone.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberInfo {

    private Long id;
    private Long pinId;
    private String pinTitle;
    private Long userId;
    private String userName;
    private MemberRole role;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;

    public static MemberInfo from(Member member) {
        return MemberInfo.builder()
                .id(member.getId())
                .pinId(member.getPin().getId())
                .pinTitle(member.getPin().getTitle())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}

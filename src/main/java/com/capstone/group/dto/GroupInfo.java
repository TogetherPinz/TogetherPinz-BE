package com.capstone.group.dto;

import com.capstone.group.enums.MemberRole;
import com.capstone.group.entity.Group;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GroupInfo {

    private Long id;
    private Long pinId;
    private String pinTitle;
    private Long userId;
    private String userName;
    private MemberRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GroupInfo from(Group group) {
        return GroupInfo.builder()
                .id(group.getId())
                .pinId(group.getPin().getId())
                .pinTitle(group.getPin().getTitle())
                .userId(group.getUser().getId())
                .userName(group.getUser().getName())
                .role(group.getRole())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}

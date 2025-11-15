package com.capstone.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRequest {

    @NotNull(message = "핀 ID는 필수입니다.")
    private Long pinId;
}

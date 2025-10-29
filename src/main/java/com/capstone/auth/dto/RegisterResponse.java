package com.capstone.auth.dto;

import com.capstone.user.entity.User;
import com.capstone.user.dto.UserInfo;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterResponse {

    private String username;
    private String message;
    private UserInfo userInfo;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime registeredDate;

    public static RegisterResponse from(User user) {
        return RegisterResponse.builder()
                .userInfo(UserInfo.fromEntity(user))
                .registeredDate(user.getCreatedDate())
                .build();
    }

}

package com.capstone.user.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class FindUsernameRequest {

    private String phone;
    private String email;

}

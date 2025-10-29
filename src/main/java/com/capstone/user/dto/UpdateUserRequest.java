package com.capstone.user.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UpdateUserRequest {

    private String name;
    private String phone;
    private String email;

}

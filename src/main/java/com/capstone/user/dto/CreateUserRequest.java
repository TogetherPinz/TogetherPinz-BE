package com.capstone.user.dto;

import com.capstone.user.entity.User;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class CreateUserRequest {

    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;

}

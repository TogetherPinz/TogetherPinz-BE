package com.capstone.member.enums;

import lombok.Getter;

@Getter
public enum MemberRole {

    OWNER("그룹 소유자"),
    MEMBER("그룹 멤버");

    private final String description;

    MemberRole(String description) {
        this.description = description;
    }

    static public MemberRole fromString(String value) {
        for (MemberRole role : MemberRole.values()) {
            if (role.name().equalsIgnoreCase(value) || role.getDescription().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}

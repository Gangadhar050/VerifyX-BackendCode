package com.verify_x.jwt;

import lombok.*;

import java.util.Arrays;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPrincipal {
    private Long userId;
    private String email;
    private String username;


}

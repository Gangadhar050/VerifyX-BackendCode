package com.verify_x.jwt;

import lombok.*;

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

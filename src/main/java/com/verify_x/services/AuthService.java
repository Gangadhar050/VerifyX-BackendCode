package com.verify_x.services;

//import com.verify_x.dto.LoginRequestDto;
//import com.verify_x.dto.LoginResponseDto;
//import com.verify_x.dto.UserRegistrationDto;
import com.verify_x.dto.*;
import com.verify_x.enums.Role;

public interface AuthService {


        String register(UserRegistrationDto registrationDto);

        LoginResponseDto login(LoginRequestDto loginRequest);

        CurrentUserDto getCurrentUser();

        void logout(String token);

        LoginResponseDto adminLogin(AdminLoginRequestDto request);

        void adminLogout(String token);

    }



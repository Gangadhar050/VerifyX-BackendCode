package com.verify_x.serviceImpl;

import com.verify_x.dto.*;
import com.verify_x.entity.Admin;
import com.verify_x.entity.Candidate;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.Role;
import com.verify_x.exception.EmailAlreadyExistsException;
import com.verify_x.exception.UserAlreadyExistsException;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.AdminRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.jwt.TokenBlacklist;
import com.verify_x.jwt.JwtService;
import com.verify_x.services.CandidateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private CandidateService candidateService;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private TokenBlacklist tokenBlacklist;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(candidateRepository, passwordEncoder,
                authenticationManager, jwtService, candidateService,
                adminRepository, tokenBlacklist);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRegisterSuccess() {
        UserRegistrationDto dto = UserRegistrationDto.builder()
                .username("john")
                .email("john@example.com")
                .phoneNumber("1234567890")
                .password("pass")
                .appliedRole("DEV")
                .candidateType(CandidateType.FRESHER)
                .build();

        when(candidateRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(candidateRepository.existsByPhoneNumber(dto.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");

        Candidate saved = Candidate.builder()
                .id(1L)
                .username(dto.getUsername())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .password("encoded")
                .appliedRole(dto.getAppliedRole())
                .candidateType(dto.getCandidateType())
                .role(Role.CANDIDATE)
                .build();

        when(candidateRepository.save(any(Candidate.class))).thenReturn(saved);

        String result = authService.register(dto);

        assertEquals("Registration Successful", result);
        verify(candidateService).saveCandidateProfile(saved);
    }

    @Test
    void testRegisterEmailExists() {
        UserRegistrationDto dto = UserRegistrationDto.builder()
                .email("dup@example.com")
                .phoneNumber("1111111111")
                .build();

        when(candidateRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(dto));
    }
    @Test
    void testLoginSuccess() {

        LoginRequestDto dto = LoginRequestDto.builder()
                .email("joe@example.com")
                .password("pwd")
                .build();

        Candidate candidate = Candidate.builder()
                .id(2L)
                .username("joe")
                .email(dto.getEmail())
                .role(Role.CANDIDATE)
                .candidateType(CandidateType.FRESHER)
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(candidateRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(candidate));

        when(jwtService.generateToken(candidate))
                .thenReturn("token-123");

        LoginResponseDto response = authService.login(dto);

        assertNotNull(response);
        assertEquals("token-123", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(candidate.getId(), response.getUserId());
        assertEquals(candidate.getEmail(), response.getEmail());
    }

    @Test
    void testGetCurrentUserSuccess() {
        UserPrincipal principal = UserPrincipal.builder()
                .userId(3L)
                .username("alice")
                .email("alice@example.com")
                .role(Role.CANDIDATE)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        Candidate user = Candidate.builder()
                .id(3L)
                .username("alice")
                .email("alice@example.com")
                .role(Role.CANDIDATE)
                .build();

        when(candidateRepository.findById(3L)).thenReturn(Optional.of(user));

        CurrentUserDto dto = authService.getCurrentUser();

        assertEquals("alice", dto.getUsername());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals(Role.CANDIDATE, dto.getRole());
    }

    @Test
    void testAdminLogoutBlankTokenThrows() {
        assertThrows(IllegalArgumentException.class, () -> authService.adminLogout("   "));
    }

    @Test
    void testAdminLogoutSuccess() {
        String token = "tok-xyz";

        doNothing().when(tokenBlacklist).blacklistToken(token);

        authService.adminLogout(token);

        verify(tokenBlacklist).blacklistToken(token);
        // SecurityContext should be cleared
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

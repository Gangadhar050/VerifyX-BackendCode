//package com.verify_x.serviceImpl;
//
//import com.verify_x.dto.ScreeningRequestExperienceDto;
//import com.verify_x.dto.ScreeningRequestFresherDto;
//import com.verify_x.dto.ScreeningResponseFresherDto;
//import com.verify_x.entity.Screening;
//import com.verify_x.entity.User;
//import com.verify_x.enums.EmploymentStatus;
//import com.verify_x.enums.ScreeningStatus;
//import com.verify_x.exception.BadRequestException;
//import com.verify_x.exception.ResourceAlreadyExistsException;
//import com.verify_x.mapper.ScreeningMapper;
//import com.verify_x.repository.ScreeningRepository;
//import com.verify_x.services.CurrentUserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.YearMonth;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class ScreeningServiceImplTest {
//
//    @Mock
//    private ScreeningRepository screeningRepository;
//
//    @Spy
//    private ScreeningMapper screeningMapper = new ScreeningMapper();
//
//    @Mock
//    private CurrentUserService currentUserService;
//
//    @InjectMocks
//    private ScreeningServiceImpl screeningService;
//
//    private User testUser;
//
//    @BeforeEach
//    void setUp() {
//        testUser = new User();
//        testUser.setId(1L);
//        testUser.setUsername("testuser");
//    }
//
//    @Test
//    void testStartScreeningFresher_Success_NoGap() {
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//        when(screeningRepository.existsByUser(testUser)).thenReturn(false);
//        when(screeningRepository.save(any(Screening.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        YearMonth current = YearMonth.now();
//        ScreeningRequestFresherDto request = new ScreeningRequestFresherDto("John Doe", current);
//
//        ScreeningResponseFresherDto response = screeningService.startScreeningFresher(request);
//
//        assertNotNull(response);
//        assertEquals(EmploymentStatus.FRESHER, response.getEmploymentStatus());
//        assertEquals(0, response.getTotalGapMonths());
//        assertEquals(ScreeningStatus.IN_PROGRESS, response.getScreeningStatus());
//        assertEquals(current, response.getGraduationMonthYear());
//    }
//
//    @Test
//    void testStartScreeningFresher_Success_WithGap() {
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//        when(screeningRepository.existsByUser(testUser)).thenReturn(false);
//        when(screeningRepository.save(any(Screening.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        YearMonth graduation = YearMonth.now().minusMonths(6);
//        ScreeningRequestFresherDto request = new ScreeningRequestFresherDto("John Doe", graduation);
//
//        ScreeningResponseFresherDto response = screeningService.startScreeningFresher(request);
//
//        assertNotNull(response);
//        assertEquals(EmploymentStatus.FRESHER, response.getEmploymentStatus());
//        assertEquals(6, response.getTotalGapMonths());
//        assertEquals(ScreeningStatus.DOCUMENT_PENDING, response.getScreeningStatus());
//        assertEquals(graduation, response.getGraduationMonthYear());
//    }
//
//    @Test
//    void testStartScreeningFresher_AlreadyExists() {
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//        when(screeningRepository.existsByUser(testUser)).thenReturn(true);
//
//        ScreeningRequestFresherDto request = new ScreeningRequestFresherDto("John Doe", YearMonth.now());
//
//        assertThrows(ResourceAlreadyExistsException.class, () -> screeningService.startScreeningFresher(request));
//    }
//
//    @Test
//    void testUpdateScreeningFresher_Success() {
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//        Screening existing = new Screening();
//        existing.setUser(testUser);
//        existing.setEmploymentStatus(EmploymentStatus.FRESHER);
//        existing.setGraduationMonthYear(YearMonth.now().minusMonths(2));
//        existing.setTotalGapMonths(2);
//        existing.setScreeningStatus(ScreeningStatus.IN_PROGRESS);
//
//        when(screeningRepository.findByUser(testUser)).thenReturn(Optional.of(existing));
//        when(screeningRepository.save(any(Screening.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        YearMonth newGraduation = YearMonth.now().minusMonths(8);
//        ScreeningRequestFresherDto request = new ScreeningRequestFresherDto("John Doe", newGraduation);
//
//        ScreeningResponseFresherDto response = screeningService.updateScreeningFresher(request);
//
//        assertNotNull(response);
//        assertEquals(8, response.getTotalGapMonths());
//        assertEquals(ScreeningStatus.DOCUMENT_PENDING, response.getScreeningStatus());
//    }
//
//    @Test
//    void testStartScreeningExperience_ThrowsException_ForFresherStatus() {
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//        when(screeningRepository.existsByUser(testUser)).thenReturn(false);
//
//        ScreeningRequestExperienceDto request = new ScreeningRequestExperienceDto();
//        request.setEmploymentStatus(EmploymentStatus.FRESHER);
//
//        assertThrows(BadRequestException.class, () -> screeningService.startScreeningExperience(request));
//    }
//
//    @Test
//    void testUpdateScreeningExperience_ThrowsException_ForFresherStatus() {
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//        Screening existing = new Screening();
//        existing.setUser(testUser);
//
//        when(screeningRepository.findByUser(testUser)).thenReturn(Optional.of(existing));
//
//        ScreeningRequestExperienceDto request = new ScreeningRequestExperienceDto();
//        request.setEmploymentStatus(EmploymentStatus.FRESHER);
//
//        assertThrows(BadRequestException.class, () -> screeningService.updateScreening(request));
//    }
//}

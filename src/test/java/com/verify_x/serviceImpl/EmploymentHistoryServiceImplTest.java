//package com.verify_x.serviceImpl;
//
//import com.verify_x.dto.EmploymentHistoryRequestDto;
//import com.verify_x.dto.EmploymentHistoryResponseDto;
//import com.verify_x.entity.EmploymentHistory;
//import com.verify_x.entity.Screening;
//import com.verify_x.entity.User;
//import com.verify_x.enums.EmploymentStatus;
//import com.verify_x.enums.VerificationStatus;
//import com.verify_x.exception.BadRequestException;
//import com.verify_x.repository.EmploymentHistoryRepository;
//import com.verify_x.repository.ScreeningRepository;
//import com.verify_x.services.CurrentUserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.when;
//import static org.mockito.ArgumentMatchers.any;
//
//@ExtendWith(MockitoExtension.class)
//public class EmploymentHistoryServiceImplTest {
//
//    @Mock
//    private EmploymentHistoryRepository employmentRepository;
//
//    @Mock
//    private ScreeningRepository screeningRepository;
//
//    @Mock
//    private com.verify_x.mapper.EmploymentHistoryMapper employmentMapper;
//
//    @Mock
//    private CurrentUserService currentUserService;
//
//    @InjectMocks
//    private EmploymentHistoryServiceImpl employmentHistoryService;
//
//    private User testUser;
//    private Screening fresherScreening;
//
//    @BeforeEach
//    void setUp() {
//        testUser = new User();
//        testUser.setId(1L);
//        testUser.setUsername("testuser");
//
//        fresherScreening = new Screening();
//        fresherScreening.setId(1L);
//        fresherScreening.setUser(testUser);
//        fresherScreening.setEmploymentStatus(EmploymentStatus.FRESHER);
//    }
//
//
//    @Test
//    void testUpdateEmployment_ThrowsException_ForFresher() {
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//        when(screeningRepository.findByUser(testUser)).thenReturn(Optional.of(fresherScreening));
//
//        EmploymentHistoryRequestDto request = new EmploymentHistoryRequestDto();
//
//        assertThrows(BadRequestException.class, () -> employmentHistoryService.updateEmployment(1L, request));
//    }
//
//    @Test
//    void testDeleteEmployment_ThrowsException_ForFresher() {
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//        when(screeningRepository.findByUser(testUser)).thenReturn(Optional.of(fresherScreening));
//
//        assertThrows(BadRequestException.class, () -> employmentHistoryService.deleteEmployment(1L));
//    }
//
//    @Test
//    void testUpdateEmployment_Success() {
//        when(currentUserService.getCurrentUser()).thenReturn(testUser);
//
//        Screening screening = new Screening();
//        screening.setId(1L);
//        screening.setUser(testUser);
//        screening.setEmploymentStatus(EmploymentStatus.EMPLOYED);
//        when(screeningRepository.findByUser(testUser)).thenReturn(Optional.of(screening));
//
//        EmploymentHistory existing = new EmploymentHistory();
//        existing.setId(10L);
//        existing.setScreening(screening);
//        when(employmentRepository.findById(10L)).thenReturn(Optional.of(existing));
//        when(employmentRepository.save(any(EmploymentHistory.class))).thenAnswer(i -> i.getArgument(0));
//
//        EmploymentHistoryRequestDto request = EmploymentHistoryRequestDto.builder()
//                .companyName("Google")
//                .designation("Software Engineer")
//                .workLocation("Mountain View")
//                .joiningDate(LocalDate.of(2020, 1, 1))
//                .relievingDate(LocalDate.of(2022, 1, 1))
//                .currentlyWorking(false)
//                .duration("24 months")
//                .employmentType("Full-time")
//                .description("Coding Antigravity AI")
//                .companyWebsite("https://google.com")
//                .HrEmail("hr@google.com")
//                .HrMobile("1234567890")
//                .verificationStatus(VerificationStatus.PENDING)
//                .remarks("Looks good")
//                .build();
//
//        when(employmentMapper.toResponse(any(EmploymentHistory.class))).thenAnswer(i -> {
//            EmploymentHistory eh = i.getArgument(0);
//            return EmploymentHistoryResponseDto.builder()
//                    .companyName(eh.getCompanyName())
//                    .designation(eh.getDesignation())
//                    .workLocation(eh.getWorkLocation())
//                    .joiningDate(eh.getJoiningDate())
//                    .relievingDate(eh.getRelievingDate())
//                    .currentlyWorking(eh.getCurrentlyWorking())
//                    .duration(eh.getDuration())
//                    .employmentType(eh.getEmploymentType())
//                    .description(eh.getDescription())
//                    .companyWebsite(eh.getCompanyWebsite())
//                    .HrEmail(eh.getHrEmail())
//                    .HrMobile(eh.getHrMobile())
//                    .build();
//        });
//
//        EmploymentHistoryResponseDto response = employmentHistoryService.updateEmployment(10L, request);
//
//        assertNotNull(response);
//        assertEquals("Google", response.getCompanyName());
//        assertEquals("Software Engineer", response.getDesignation());
//        assertEquals("Mountain View", response.getWorkLocation());
//        assertEquals("2020-01-01", response.getJoiningDate());
//        assertEquals("2022-01-01", response.getRelievingDate());
//        assertEquals(false, response.getCurrentlyWorking());
//        assertEquals("24 months", response.getDuration());
//        assertEquals("Full-time", response.getEmploymentType());
//        assertEquals("Coding Antigravity AI", response.getDescription());
//        assertEquals("https://google.com", response.getCompanyWebsite());
//        assertEquals("hr@google.com", response.getHrEmail());
//        assertEquals("1234567890", response.getHrMobile());
//    }
//}

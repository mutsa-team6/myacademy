package com.project.myacademy.domain.academy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.myacademy.domain.academy.dto.AcademyDto;
import com.project.myacademy.domain.academy.dto.CreateAcademyRequest;
import com.project.myacademy.domain.academy.service.AcademyService;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.global.configuration.SecurityConfig;
import com.project.myacademy.global.util.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AcademyRestController.class)
@ImportAutoConfiguration(SecurityConfig.class)
class AcademyRestControllerTest {

    @MockBean
    private AcademyService academyService;
    @MockBean
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jwt.token.secret}")
    private String secretKey;

    @BeforeEach
    void setUp() {
        final Employee admin = EmployeeFixture.ROLE_ADMIN.init();
        final Employee employee1 = EmployeeFixture.ROLE_USER1.init();
        final Employee employee2 = EmployeeFixture.ROLE_USER2.init();
    }

    @Test
    @DisplayName("학원 생성 : 성공")
    void create() throws Exception {
        final CreateAcademyRequest request = new CreateAcademyRequest(
                "name",
                "address",
                "phoneNum",
                "admin",
                "businessRegistrationNumber",
                "password"
        );
        final AcademyDto academyDto = new AcademyDto(
                1L,
                "name",
                "admin",
                "학원 등록이 정상적으로 완료되었습니다."
        );

        when(academyService.createAcademy(any(CreateAcademyRequest.class))).thenReturn(academyDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/academies/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.name").value("name"))
                .andExpect(jsonPath("$.result.owner").value("admin"))
                .andExpect(jsonPath("$.result.message").value("학원 등록이 정상적으로 완료되었습니다."));

        verify(academyService).createAcademy(any(CreateAcademyRequest.class));
    }

}
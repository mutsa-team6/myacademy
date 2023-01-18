package com.project.myacademy.domain.academy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.myacademy.domain.academy.dto.*;
import com.project.myacademy.domain.academy.service.AcademyService;
import com.project.myacademy.domain.academy.util.EmployeeFixtureUtil;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.global.configuration.SecurityConfig;
import com.project.myacademy.global.util.JwtTokenUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AcademyRestController.class)
@ImportAutoConfiguration(SecurityConfig.class)
class AcademyRestControllerTest {

    @MockBean
    private AcademyService academyService;
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jwt.token.secret}")
    private String secretKey;

    final Employee admin = EmployeeFixtureUtil.ROLE_ADMIN.init();
    final Employee employee1 = EmployeeFixtureUtil.ROLE_USER1.init();
    final Employee employee2 = EmployeeFixtureUtil.ROLE_USER2.init();

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

    @Test
    @DisplayName("학원 정보 수정 : 성공")
    void update() throws Exception {
        final String token = JwtTokenUtil.createToken("admin", secretKey, 3600L);
        final UpdateAcademyReqeust request = new UpdateAcademyReqeust(
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
                "학원 수정이 정상적으로 완료되었습니다."
        );

        when(academyService.updateAcademy(anyLong(), any(UpdateAcademyReqeust.class), anyString())).thenReturn(academyDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/academies/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.name").value("name"))
                .andExpect(jsonPath("$.result.owner").value("admin"))
                .andExpect(jsonPath("$.result.message").value("학원 수정이 정상적으로 완료되었습니다."));

        verify(academyService).updateAcademy(anyLong(), any(UpdateAcademyReqeust.class), anyString());
    }

    @Test
    @DisplayName("학원 정보 삭제 : 성공")
    void delete() throws Exception {
        final String token = JwtTokenUtil.createToken("admin", secretKey, 3600L);

        when(academyService.deleteAcademy(anyLong(), anyString())).thenReturn(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/academies/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.message").value("학원 삭제가 정상적으로 완료되었습니다."));

        verify(academyService).deleteAcademy(anyLong(), anyString());
    }

    @Test
    @DisplayName("학원 로그인 : 성공")
    void login() throws Exception {
        final String token = JwtTokenUtil.createToken("admin", secretKey, 3600L);
        final LoginAcademyRequest request = new LoginAcademyRequest("businessRegistrationNumber", "password");
        final LoginAcademyResponse response = new LoginAcademyResponse(1L, token);

        when(academyService.loginAcademy(any(LoginAcademyRequest.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/academies/login")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.academyId").value(1L))
                .andExpect(jsonPath("$.result.jwt").exists());

        verify(academyService).loginAcademy(any(LoginAcademyRequest.class));
    }
}
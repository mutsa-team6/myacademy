package com.project.myacademy.domain.academy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.myacademy.domain.academy.AcademyRestController;
import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.CreateAcademyRequest;
import com.project.myacademy.domain.academy.dto.CreateAcademyResponse;
import com.project.myacademy.domain.academy.dto.FindAcademyRequest;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WithMockUser //security통과할려면 무조건 있어야함.
@WebMvcTest(AcademyRestController.class)
class AcademyRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AcademyService academyService;

    private final CreateAcademyResponse createAcademyResponse = new CreateAcademyResponse(1l, "학원이름", "원장이름", "학원주소", "010-0000-0000", "0000");
    private final CreateAcademyRequest createAcademyRequest = new CreateAcademyRequest("학원이름", "학원주소", "010-0000-0000", "원장이름", "0000");

    @Nested
    @DisplayName("학원 등록")
    class CreateAcademy {
        @Test
        @DisplayName("학원 등록 성공")
        public void academyCreate_success() throws Exception {
            //given
            given(academyService.createAcademy(any())).willReturn(createAcademyResponse);

            mockMvc.perform(post("/api/v1/academies")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(createAcademyRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.academyId").exists())
                    .andExpect(jsonPath("$.result.name").exists())
                    .andExpect(jsonPath("$.result.owner").exists())
                    .andExpect(jsonPath("$.result.address").exists())
                    .andExpect(jsonPath("$.result.phoneNum").exists())
                    .andExpect(jsonPath("$.result.businessRegistrationNumber").exists());
        }

        @Test
        @DisplayName("학원 등록 실패1 - 학원이름 중복")
        public void academyCreate_fail1() throws Exception {
            given(academyService.createAcademy(any())).willThrow(new AppException(ErrorCode.DUPLICATED_ACADEMY));

            mockMvc.perform(post("/api/v1/academies")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(createAcademyRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("학원 삭제 성공")
        public void academyDelete_success() throws Exception {
            //given
            given(academyService.deleteAcademy(any())).willReturn(1l);

            mockMvc.perform(delete("/api/v1/academies/1/delete")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(createAcademyRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").exists());
        }

        @Test
        @DisplayName("학원 삭제 실패1 - 학원 찾을 수 없음.")
        public void academyDelete_fail1() throws Exception {
            //given
            given(academyService.deleteAcademy(any())).willThrow(new AppException(ErrorCode.ACADEMY_NOT_FOUND));

            mockMvc.perform(delete("/api/v1/academies/1/delete")
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }
}
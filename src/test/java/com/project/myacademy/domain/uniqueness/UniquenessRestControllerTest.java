//package com.project.myacademy.domain.uniqueness;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.project.myacademy.domain.employee.EmployeeRole;
//import com.project.myacademy.domain.uniqueness.dto.CreateUniquenessResponse;
//import com.project.myacademy.global.configuration.SecurityConfig;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.test.mock.mockito.MockBeans;
//import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(UniquenessRestController.class)
//@ImportAutoConfiguration(SecurityConfig.class)
//class UniquenessRestControllerTest {
//
//    @MockBean
//    UniquenessService uniquenessService;
//    @Autowired
//    WebApplicationContext webApplicationContext;
//    @Autowired
//    MockMvc mockMvc;
//    @Autowired
//    ObjectMapper objectMapper;
//
//    @BeforeEach
//    void setUp() {
//        mockMvc = MockMvcBuilders
//                .webAppContextSetup(webApplicationContext)
//                .apply(SecurityMockMvcConfigurers.springSecurity())
//                .build();
//    }
//
//    @Nested
//    @DisplayName("생성테스트")
//    class create {
//
//        @Test
//        @DisplayName("특이사항 등록")
//        void uniquenessCreateSuccessTest() throws Exception {
//            CreateUniquenessResponse response = CreateUniquenessResponse.builder()
//                    .studentId(1L)
//                    .studentName("studentName")
//                    .uniquenessId(1L)
//                    .body("body")
//                    .build();
//
//            when(uniquenessService.createUniqueness(any(), any(), any(), any())).thenReturn(response);
//
//            mockMvc.perform(post("api/v1/academies/1/students/1/uniqueness")
//                            .header())
//                    .andDo(print())
//                    .andExpect(status().isOk());
//        }
//
//    }
//
//    @Nested
//    @DisplayName("조회테스트")
//    class readAll {
//    }
//
//    @Nested
//    @DisplayName("수정테스트")
//    class update {
//    }
//
//    @Nested
//    @DisplayName("삭제테스트")
//    class delete {
//    }
//}
package com.project.myacademy.global.configuration;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "My Academy",
                description = "안녕하세요 멋사2기 6조 인규와 아이들 입니다!\n" +
                        "\nSwagger 사용 방법 : \n" +
                        "1. 학원 등록 : 학원 탭에서 가입\n" +
                        "2. admin 계정 생성 : 직원 회원가입에서 계정명=\"admin\" 이름=\"대표자명\" 으로 가입",


                version = "v1"
        ),
        tags = {
                @Tag(name = "학원", description = "학원 등록, 수정, 조회, 삭제"),
                @Tag(name = "직원", description = "직원 등록, 수정, 조회, 삭제"),
                @Tag(name = "학원공지사항", description = "학원 공지사항 등록, 수정, 조회, 삭제"),
                @Tag(name = "학부모", description = "학부모 등록, 수정, 조회, 삭제"),
                @Tag(name = "학생", description = "학생 등록, 수정, 조회, 삭제"),
                @Tag(name = "학생특이사항", description = "학생 특이사항 등록, 수정, 조회, 삭제"),
                @Tag(name = "강사", description = "강사 등록, 수정, 조회, 삭제"),
                @Tag(name = "강의", description = "강의 등록, 수정, 조회, 삭제"),
                @Tag(name = "수강신청", description = "수강 등록, 수정, 조회, 삭제"),
                @Tag(name = "수강대기", description = "수강대기 등록, 수정, 조회, 삭제"),
                @Tag(name = "결제", description = "결제 등록, 수정, 조회, 삭제")

        }

)
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi FirstOpenApi() {
        String[] paths = {"/api/v1/academies/*"};

        return GroupedOpenApi
                .builder()
                .group("학원,직원,학원공지사항")
                .pathsToMatch(paths)
                .addOpenApiCustomiser(buildSecurityOpenApi())
                .build();
    }

    @Bean
    public GroupedOpenApi SecurityGroupOpenApi() {
        String[] paths = {"/api/v1/**"};

        return GroupedOpenApi
                .builder()
                .group("학원 관리 시스템 v1")
                .pathsToMatch(paths)
                .addOpenApiCustomiser(buildSecurityOpenApi())
                .build();
    }


    public OpenApiCustomiser buildSecurityOpenApi() {
        return OpenApi -> OpenApi.addSecurityItem(new SecurityRequirement().addList("jwt token"))
                .getComponents().addSecuritySchemes("jwt token", new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .bearerFormat("JWT")
                        .scheme("Bearer"));
    }
}


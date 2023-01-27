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
                title = "\uD83C\uDF31My Academy\uD83C\uDF31",
                description = "안녕하세요 멋사2기 6조 인규와 아이들 입니다😁 \n" +
                        "\n배포링크 👉 [클릭](http://ec2-3-39-187-138.ap-northeast-2.compute.amazonaws.com:8080/)\n" +
                        "\n👀Swagger 사용 방법👀 \n" +
                        "1. 학원 등록 : 학원 탭에서 가입 \n" +
                        "2. admin 계정 생성 : 직원 회원가입에서 계정명=\"admin\" / 이름=\"대표자명\" 으로 가입 \n" +
                        "3. admin 계정 로그인 : 로그인 성공시 쿠키에 토큰이 담겨, 해당 계정에 ADMIN 권한이 부여됩니다! (토큰 입력 필요❌)\n" +
                        "\n💡오른쪽 상단 \"Select a definition\"을 이용하시면 좀 더 쾌적하게 보실수 있습니다💡",


                version = "v1"
        )
//        tags = {
//                @Tag(name = "학원", description = "학원 등록, 수정, 조회, 삭제"),
//                @Tag(name = "직원", description = "직원 등록, 수정, 조회, 삭제"),
//                @Tag(name = "학원공지사항", description = "학원 공지사항 등록, 수정, 조회, 삭제"),
//                @Tag(name = "학부모", description = "학부모 등록, 수정, 조회, 삭제"),
//                @Tag(name = "학생", description = "학생 등록, 수정, 조회, 삭제"),
//                @Tag(name = "학생특이사항", description = "학생 특이사항 등록, 수정, 조회, 삭제"),
//                @Tag(name = "강사", description = "강사 등록, 수정, 조회, 삭제"),
//                @Tag(name = "강의", description = "강의 등록, 수정, 조회, 삭제"),
//                @Tag(name = "수강신청", description = "수강 등록, 수정, 조회, 삭제"),
//                @Tag(name = "수강대기", description = "수강대기 등록, 수정, 조회, 삭제"),
//                @Tag(name = "결제", description = "결제 등록, 수정, 조회, 삭제")
//        "/api/v1/academies","/api/v1/academies/**/delete","/api/v1/academies/find",
//        "/api/v1/academies/{}"}
)

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi FirstOpenApi() {
        String[] paths = {
                "com.project.myacademy.domain.academy",
                "com.project.myacademy.domain.employee",
                "com.project.myacademy.domain.announcement"
        };

        return GroupedOpenApi
                .builder()
                .group("1. 직원, 학원관리")
                .packagesToScan(paths)
//                .addOpenApiCustomiser(buildSecurityOpenApi())
                .build();
    }

    @Bean
    public GroupedOpenApi SecondOpenApi() {
        String[] paths = {
                "com.project.myacademy.domain.parent",
                "com.project.myacademy.domain.student",
                "com.project.myacademy.domain.uniqueness"
        };

        return GroupedOpenApi
                .builder()
                .group("2. 학생, 학부모 관리")
                .packagesToScan(paths)
//               .addOpenApiCustomiser(buildSecurityOpenApi())
                .build();
    }

    @Bean
    public GroupedOpenApi ThirdOpenApi() {
        String[] paths = {
                "com.project.myacademy.domain.teacher",
                "com.project.myacademy.domain.lecture",
                "com.project.myacademy.domain.enrollment",
                "com.project.myacademy.domain.waitinglist",
                "com.project.myacademy.domain.payment"
        };

        return GroupedOpenApi
                .builder()
                .group("3. 강의 및 결제 관리")
                .packagesToScan(paths)
//                .addOpenApiCustomiser(buildSecurityOpenApi())
                .build();
    }

    @Bean
    public GroupedOpenApi SecurityGroupOpenApi() {
        String[] paths = {"/api/v1/**"};

        return GroupedOpenApi
                .builder()
                .group("0. 학원 관리 시스템")
                .pathsToMatch(paths)
//                .addOpenApiCustomiser(buildSecurityOpenApi())
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


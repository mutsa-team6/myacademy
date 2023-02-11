package com.project.myacademy.global.configuration;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "\uD83C\uDF31My Academy\uD83C\uDF31",
                description = "안녕하세요 멋사2기 6조 인규와 아이들 입니다😁 \n" +
                        "\n배포링크 👉 [클릭](http://ec2-13-209-97-187.ap-northeast-2.compute.amazonaws.com/)\n" +
                        "\n👀Swagger 사용 방법👀 \n" +
                        "1. 학원 등록 : 학원 탭에서 가입 \n" +
                        "2. admin 계정 생성 : 직원 회원가입에서 계정명=\"admin\" / 이름=\"대표자명\" 으로 가입 \n" +
                        "3. admin 계정 로그인 : 로그인 성공시 쿠키에 토큰이 담겨, 해당 계정에 ADMIN 권한이 부여됩니다! (토큰 입력 필요❌)\n" +
                        "\n💡오른쪽 상단 \"Select a definition\"을 이용하시면 좀 더 쾌적하게 보실수 있습니다💡",
                version = "v1"
        )
)

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi firstOpenApi() {
        String[] paths = {
                "com.project.myacademy.domain.academy",
                "com.project.myacademy.domain.employee",
                "com.project.myacademy.domain.announcement",
                "com.project.myacademy.domain.file.announcementfile",
                "com.project.myacademy.domain.file.employeeprofile"
        };

        return GroupedOpenApi
                .builder()
                .group("1. 직원, 학원관리")
                .packagesToScan(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi secondOpenApi() {
        String[] paths = {
                "com.project.myacademy.domain.parent",
                "com.project.myacademy.domain.student",
                "com.project.myacademy.domain.uniqueness"
        };

        return GroupedOpenApi
                .builder()
                .group("2. 학생, 학부모 관리")
                .packagesToScan(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi thirdOpenApi() {
        String[] paths = {
                "com.project.myacademy.domain.teacher",
                "com.project.myacademy.domain.lecture",
                "com.project.myacademy.domain.enrollment",
                "com.project.myacademy.domain.waitinglist",
                "com.project.myacademy.domain.discount",
                "com.project.myacademy.domain.payment",
                "com.project.myacademy.domain.file.teacherprofile"
        };

        return GroupedOpenApi
                .builder()
                .group("3. 강의 및 결제 관리")
                .packagesToScan(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi securityGroupOpenApi() {
        String[] paths = {"/api/v1/**"};

        return GroupedOpenApi
                .builder()
                .group("0. 학원 관리 시스템")
                .pathsToMatch(paths)
                .build();
    }
}


package com.project.myacademy.domain.email;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.discount.Discount;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.student.Student;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private AcademyRepository academyRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private JavaMailSender sender;
    @InjectMocks
    private EmailService emailService;

    private Academy academy;
    private Employee employee;
    private MimeMessage message;

    @BeforeEach
    void setup() {
        academy = Academy.builder().id(1L).name("academy").owner("owner").build();
        employee = Employee.builder().id(1L).name("staff").email("email").account("account").password("password").employeeRole(EmployeeRole.ROLE_STAFF).academy(academy).build();
        message = sender.createMimeMessage();
    }

//    @Nested
//    @DisplayName("전송")
//    class EmailSend{
//
//        CreateEmailRequest request = new CreateEmailRequest("percykwon@naver.com", "title", "body");
//
//        @Test
//        @DisplayName("이메일 전송 성공")
//        void send_email_success() throws MessagingException {
//
//            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
//            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
//
//
//            emailService.sendEmail(academy.getId(),request.getEmail(),request.getTitle(),request.getBody(),employee.getAccount());
//
//        }
//
//        @Test
//        @DisplayName("이메일 전송 실패(1) - 학원이 존재하지 않을 때")
//        void send_email_fail1() {
//
//        }
//
//        @Test
//        @DisplayName("이메일 전송 실패(2) - 전송하는 직원이 해당 학원 소속이 아닐 때")
//        void send_email_fail2() {
//
//        }
//    }

}
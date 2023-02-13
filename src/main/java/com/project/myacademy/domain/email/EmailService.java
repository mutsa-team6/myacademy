package com.project.myacademy.domain.email;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@RequiredArgsConstructor
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender sender;
    private final AcademyRepository academyRepository;
    private final EmployeeRepository employeeRepository;

    public void sendEmail(Long academyId, String toAddress, String subject, String body, String account) throws MessagingException {

        Academy academy = validateAcademyById(academyId);
        validateRequestEmployeeByAcademy(account, academy);

        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

                helper.setTo(toAddress);
                helper.setSubject(subject);
                helper.setText(body);
                sender.send(message);
    }

    private Academy validateAcademyById(Long academyId) {
        return academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
    }

    public Employee validateRequestEmployeeByAcademy(String account, Academy academy) {
        return employeeRepository.findByAccountAndAcademy(account, academy)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND));
    }
}

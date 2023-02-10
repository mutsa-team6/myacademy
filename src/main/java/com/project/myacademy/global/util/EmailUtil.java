package com.project.myacademy.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.validation.constraints.Email;

@RequiredArgsConstructor
@Component
@Slf4j
public class EmailUtil {

    private final JavaMailSender sender;

    public void sendEmail(String toAddress, String subject, String body) throws MessagingException {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

                helper.setTo(toAddress);
                helper.setSubject(subject);
                helper.setText(body);
                sender.send(message);




    }
}

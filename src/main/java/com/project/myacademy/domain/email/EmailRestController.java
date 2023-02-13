package com.project.myacademy.domain.email;

import com.project.myacademy.global.Response;
import com.project.myacademy.global.exception.BindingException;
import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.global.util.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;

@Tag(name = "12. 이메일", description = "이메일 전송")
@RestController
@RequestMapping("api/v1/academies")
@RequiredArgsConstructor
@Slf4j
public class EmailRestController {

    private final EmailService emailService;

    @Operation(summary = "이메일 전송", description = "ADMIN, STAFF 회원만 전송 가능합니다.")
    @PostMapping("/{academyId}/send/email")
    public ResponseEntity<Response<String>> create(@PathVariable("academyId") Long academyId,
                                                   @Validated @RequestBody CreateEmailRequest request,
                                                   BindingResult bindingResult,
                                                   Authentication authentication) throws MessagingException {

        if (bindingResult.hasFieldErrors()) {
            throw new BindingException(ErrorCode.BINDING_ERROR, bindingResult.getFieldError().getDefaultMessage());
        }

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        emailService.sendEmail(academyId, request.getEmail(), request.getTitle(), request.getBody(), requestAccount);

        return ResponseEntity.ok().body(Response.success("이메일 전송 성공"));
    }
}

package com.project.myacademy.domain.payment;

import com.project.myacademy.domain.payment.dto.*;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.util.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "11. 결제", description = "Toss 결제 OpenAPI")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class TossPayRestController {
    private final PaymentService paymentService;

    /**
     * 프런트에서 결제 요청하기 위한 api
     * 요청값들에 대한 검증이 이루어지는 controller
     *
     * @param request
     * @param studentId
     * @param authentication
     * @return
     */
    @Operation(summary = "결제 요청", description = "OpenApi로 보내기 전에 올바른 요청인지 검증합니다.")
    @PostMapping("/students/{studentId}")
    public ResponseEntity<Response<CreatePaymentResponse>> request(@RequestBody CreatePaymentRequest request,
                                                                  @PathVariable Long studentId,
                                                                  Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        return ResponseEntity.ok().body(Response.success(paymentService.createPayment(request, academyId, studentId, account)));
    }

    /**
     * 결제 승인 요청 controllr (검증, 승인요청)
     *
     * @param paymentKey 토스측 결제 키
     * @param orderId 우리측 결제 키
     * @param amount 금액
     * @return
     */
    @Operation(summary = "결제 승인 성공", description = "OpenApi로 부터 결제 성공시 가격 검증과 가격 승인 처리")
    @GetMapping("/success")
    public ResponseEntity<Response<ApprovePaymentResponse>> successApproveRequest(@RequestParam String paymentKey,
                                                                                  @RequestParam String orderId,
                                                                                  @RequestParam Integer amount) {
        paymentService.verifyRequest(paymentKey, orderId, amount);
        ApprovePaymentResponse result = paymentService.successApprovePayment(paymentKey, orderId, amount);
        return ResponseEntity.ok().body(Response.success(result));
    }

    /**
     * 결제 승인 실패 요청 controllr (검증, 승인요청)
     */

    @Operation(summary = "결제 승인 실패", description = "OpenApi로 부터 결제 실패시 에러메세지 반환")
    @GetMapping("/fail")
    public ResponseEntity<Response<FailApprovePaymentResponse>> failApproveRequest(@RequestParam String errorCode,
                                                            @RequestParam String errorMsg,
                                                            @RequestParam String orderId) {
        return ResponseEntity.ok().body(Response.error(errorCode, paymentService.failApprovePayment(errorCode, errorMsg, orderId)));
    }

    @Operation(summary = "결제 취소", description = "paymentKey를 사용하여 결제취소 이유 작성")
    @PostMapping("/cancel")
    public ResponseEntity<Response<ApprovePaymentResponse>> cancel(@RequestParam String paymentKey,
                                                                   @RequestParam String cancelReason,
                                                                   Authentication authentication){
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        return ResponseEntity.ok().body(Response.success(paymentService.cancelPayment(paymentKey,cancelReason,account,academyId)));
    }
}
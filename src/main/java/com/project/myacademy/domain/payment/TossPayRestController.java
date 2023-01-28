package com.project.myacademy.domain.payment;

import com.project.myacademy.domain.payment.dto.PaymentRequest;
import com.project.myacademy.domain.payment.dto.PaymentResponse;
import com.project.myacademy.domain.payment.dto.ApproveResponse;
import com.project.myacademy.domain.payment.dto.FailApproveResponse;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.util.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * @param request
     * @param studentId
     * @param authentication
     * @return
     */
    @Operation(summary = "결제 요청", description = "OpenApi로 보내기 전에 올바른 요청인지 검증합니다.")
    @PostMapping("/students/{studentId}")
    public Response<PaymentResponse> requestPayments(@RequestBody PaymentRequest request,
                                                     @PathVariable Long studentId,
                                                     Authentication authentication ) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        return Response.success(paymentService.requestPayments(request, academyId, studentId,account));
    }

    /**
     * @param paymentKey
     * @param orderId
     * @param amount
     * @return
     */
    @Operation(summary = "결제 승인 성공", description = "OpenApi로 부터 결제 성공시 가격 검증과 가격 승인 처리")
    @GetMapping("/success")
    public Response<ApproveResponse> requestFinalPayments(@RequestParam String paymentKey,
                                                          @RequestParam String orderId,
                                                          @RequestParam Integer amount){
        paymentService.verifyRequest(paymentKey,orderId,amount);
        ApproveResponse result = paymentService.requestFinalPayment(paymentKey,orderId,amount);
        return Response.success(result);
    }

    @Operation(summary = "결제 승인 실패", description = "OpenApi로 부터 결제 실패시 에러메세지 반환")
    @GetMapping("/fail")
    public Response<FailApproveResponse> requestFail(@RequestParam String errorCode,
                                                     @RequestParam String errorMsg,
                                                     @RequestParam String orderId){
        return Response.error(errorCode, paymentService.requestFail(errorCode, errorMsg, orderId));

    }
}
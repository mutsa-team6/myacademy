package com.project.myacademy.domain.payment;

import com.project.myacademy.domain.payment.dto.PaymentRequest;
import com.project.myacademy.domain.payment.dto.PaymentResponse;
import com.project.myacademy.domain.payment.dto.PaymentResponseHandleDto;
import com.project.myacademy.domain.payment.dto.PaymentResponseHandleFailDto;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/academies/")
@RequiredArgsConstructor
@Slf4j
public class TossPayRestController {
    private final PaymentService paymentService;

    /**
     * 프런트에서 결제 요청하기 위한 api
     * 요청값들에 대한 검증이 이루어지는 controller
     * @param request
     * @param academyId
     * @param studentId
     * @param authentication
     * @return
     */

    @PostMapping("/{academyId}/students/{studentId}/payment")
    public Response<PaymentResponse> requestPayments(@ModelAttribute PaymentRequest request,
                                                     @PathVariable Long academyId,
                                                     @PathVariable Long studentId,
                                                     Authentication authentication ) {
        System.out.println(authentication);
        String account = authentication.getName();
        System.out.println(account);
        return Response.success(paymentService.requestPayments(request, academyId, studentId,account));
    }

    /**
     *
     * @param paymentKey
     * @param orderId
     * @param amount
     * @return
     */
    @GetMapping("/{academyId}/payment/success")
    public Response<PaymentResponseHandleDto> requestFinalPayments(@RequestParam String paymentKey,
                                                                   @RequestParam String orderId,
                                                                   @RequestParam Long amount){
        paymentService.verifyRequest(paymentKey,orderId,amount);
        PaymentResponseHandleDto result = paymentService.requestFinalPayment(paymentKey,orderId,amount);
        return Response.success(result);
    }

    @GetMapping("/{academyId}/payment/fail")
    public Response<PaymentResponseHandleFailDto> requestFail(@RequestParam String errorCode,
                                                              @RequestParam String errorMsg,
                                                              @RequestParam String orderId){
        return Response.error(errorCode, paymentService.requestFail(errorCode, errorMsg, orderId));

    }
}
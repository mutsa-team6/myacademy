package com.project.myacademy.domain.discount;

import com.project.myacademy.domain.discount.dto.*;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/academies")
@Slf4j
public class DiscountRestController {

    private final DiscountService discountService;

    // 할인정책 적용 가능 여부 확인
    @PostMapping("/{academyId}/discounts/check")
    public ResponseEntity<Response<CheckDiscountResponse>> check(@PathVariable("academyId") Long academyId,
                                                                 @RequestBody CheckDiscountRequest checkDiscountRequest,
                                                                 Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        CheckDiscountResponse response = discountService.checkDiscount(academyId, checkDiscountRequest, account);
        return ResponseEntity.ok().body(Response.success(response));
    }

    // 할인정책 등록
    @PostMapping("/{academyId}/discounts")
    public ResponseEntity<Response<CreateDiscountResponse>> create(@PathVariable("academyId") Long academyId,
                                                                   @RequestBody CreateDiscountRequest createDiscountRequest,
                                                                   Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateDiscountResponse response = discountService.createDiscount(academyId, createDiscountRequest, account);
        return ResponseEntity.ok().body(Response.success(response));
    }

    // 할인정책 전체 조회
    @GetMapping("/{academyId}/discounts")
    public ResponseEntity<Response<Page<GetDiscountResponse>>> getAll(@PathVariable("academyId") Long academyId,
                                                                      Authentication authentication,
                                                                      @PageableDefault(size = 20, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        Page<GetDiscountResponse> response = discountService.getAllDiscounts(academyId, account, pageable);
        return ResponseEntity.ok().body(Response.success(response));
    }

    // 수강이력에 적용된 할인정책 조회
    @GetMapping("/{academyId}/enrollments/{enrollmentId}/discounts")
    public ResponseEntity<Response<GetAppliedDiscountResponse>> getAppliedOne(@PathVariable("academyId") Long academyId,
                                                                 @PathVariable("enrollmentId") Long enrollmentId,
                                                                 Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        GetAppliedDiscountResponse response = discountService.getAppliedDiscount(academyId, enrollmentId, account);
        return ResponseEntity.ok().body(Response.success(response));
    }

    // 할인정책 삭제
    @DeleteMapping("/{academyId}/discounts/{discountId}")
    public ResponseEntity<Response<DeleteDiscountResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                   @PathVariable("discountId") Long discountId,
                                                                   Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteDiscountResponse response = discountService.deleteDiscount(academyId, discountId, account);
        return ResponseEntity.ok().body(Response.success(response));
    }
}
package com.project.myacademy.domain.uniqueness;

import com.project.myacademy.domain.uniqueness.dto.*;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.exception.BindingException;
import com.project.myacademy.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.project.myacademy.global.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "06. 학생 특이사항", description = "학생 특이사항 등록,수정,조회")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class UniquenessRestController {

    private final UniquenessService uniquenessService;

    /**
     * 학생 특이사항 작성
     */
    @Operation(summary = "학생 특이사항 등록", description = "학생 특이사항을 등록합니다.")
    @PostMapping("/{academyId}/students/{studentId}/uniqueness")
    public ResponseEntity<Response<CreateUniquenessResponse>> create(@PathVariable Long academyId,
                                                                     @PathVariable Long studentId, @Validated @RequestBody CreateUniquenessRequest request, BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasFieldErrors()) {
            throw new BindingException(ErrorCode.BINDING_ERROR, bindingResult.getFieldError().getDefaultMessage());
        }
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateUniquenessResponse response = uniquenessService.createUniqueness(academyId, studentId, request, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 특정 학생 특이사항 목록 조회
     */
    @Operation(summary = "학생 특이사항 전체 조회", description = "학생 특이사항을 모두 조회합니다.")
    @GetMapping("/{academyId}/students/{studentId}/uniqueness")
    public ResponseEntity<Response<Page<ReadAllUniquenessResponse>>> readAll(@PathVariable Long academyId,
                                                                             @PathVariable Long studentId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("id").descending());
        Page<ReadAllUniquenessResponse> responses = uniquenessService.readAllUniqueness(academyId, studentId, pageable, requestAccount);
        return ResponseEntity.ok().body(Response.success(responses));
    }

    /**
     * 특정 특이사항 수정
     */
    @Operation(summary = "학생 특이사항 수정", description = "학생 특이사항을 수정합니다.")
    @PutMapping("/{academyId}/students/{studentId}/uniqueness/{uniquenessId}")
    public ResponseEntity<Response<UpdateUniquenessResponse>> update(@PathVariable Long academyId,
                                                                     @PathVariable Long studentId,
                                                                     @PathVariable Long uniquenessId, @RequestBody UpdateUniquenessRequest request, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        UpdateUniquenessResponse response = uniquenessService.updateUniqueness(academyId, studentId, uniquenessId, request, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 특정 특이사항 삭제
     */
    @Operation(summary = "학생 특이사항 삭제", description = "학생 특이사항을 soft-delete 됩니다.")
    @DeleteMapping("/{academyId}/students/{studentId}/uniqueness/{uniquenessId}")
    public ResponseEntity<Response<DeleteUniquenessResponse>> delete(@PathVariable Long academyId,
                                                                     @PathVariable Long studentId,
                                                                     @PathVariable Long uniquenessId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteUniquenessResponse response = uniquenessService.deleteUniqueness(academyId, studentId, uniquenessId, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }
}

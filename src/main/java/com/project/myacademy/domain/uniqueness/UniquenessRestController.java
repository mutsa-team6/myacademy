package com.project.myacademy.domain.uniqueness;

import com.project.myacademy.domain.uniqueness.dto.*;
import com.project.myacademy.global.Response;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "6. 학생 특이사항", description = "학생 특이사항 등록,수정,조회")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class UniquenessRestController {

    private final UniquenessService uniquenessService;

    /**
     * 학생 특이사항 작성
     */
    @Operation(summary = "학생 특이사항 등록", description = "ADMIN,STAFF 회원만 등록이 가능합니다.")
    @PostMapping("/{academyId}/students/{studentId}/uniqueness")
    public ResponseEntity<Response<CreateUniquenessResponse>> create(@PathVariable Long academyId,
                                                                     @PathVariable Long studentId, CreateUniquenessRequest request, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateUniquenessResponse response = uniquenessService.createUniqueness(academyId, studentId, request, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 특정 학생 특이사항 목록 조회
     */
    @Operation(summary = "학생 특이사항 전체 조회", description = "ADMIN,STAFF 회원만 조회가 가능합니다.")
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
    @Operation(summary = "학생 특이사항 수정", description = "ADMIN,STAFF 회원만 수정이 가능합니다.")
    @PutMapping("/{academyId}/students/{studentId}/uniqueness/{uniquenessId}")
    public ResponseEntity<Response<UpdateUniquenessResponse>> update(@PathVariable Long academyId,
                                                                     @PathVariable Long studentId,
                                                                     @PathVariable Long uniquenessId, UpdateUniquenessRequest request, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        UpdateUniquenessResponse response = uniquenessService.updateUniqueness(academyId, studentId, uniquenessId, request, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 특정 특이사항 삭제
     */
    @Operation(summary = "학생 특이사항 삭제", description = "ADMIN,STAFF 회원만 삭제가 가능합니다. \n\n soft-delete 됩니다.")
    @DeleteMapping("/{academyId}/students/{studentId}/uniqueness/{uniquenessId}")
    public ResponseEntity<Response<DeleteUniquenessResponse>> delete(@PathVariable Long academyId,
                                                                     @PathVariable Long studentId,
                                                                     @PathVariable Long uniquenessId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteUniquenessResponse response = uniquenessService.deleteUniqueness(academyId, studentId, uniquenessId, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }
}

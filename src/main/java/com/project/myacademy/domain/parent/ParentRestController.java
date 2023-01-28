package com.project.myacademy.domain.parent;

import com.project.myacademy.domain.parent.dto.*;
import com.project.myacademy.global.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.project.myacademy.global.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "04. 학부모", description = "학부모 등록,수정,조회")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class ParentRestController {

    private final ParentService parentService;

    /**
     * 부모 등록
     */
    @Operation(summary = "학부모 등록", description = "ADMIN,STAFF 회원만 등록이 가능합니다.")
    @PostMapping("/{academyId}/parents")
    public ResponseEntity<Response<CreateParentResponse>> create(@PathVariable Long academyId, CreateParentRequest request, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateParentResponse response = parentService.createParent(academyId, request, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 부모 정보 단건 조회
     */
    @Operation(summary = "학부모 조회", description = "ADMIN,STAFF 회원만 조회가 가능합니다.")
    @GetMapping("/{academyId}/parents/{parentId}")
    public ResponseEntity<Response<ReadParentResponse>> read(@PathVariable Long academyId, @PathVariable Long parentId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        ReadParentResponse response = parentService.readParent(academyId, parentId, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 부모 정보 수정
     */
    @Operation(summary = "학부모 수정", description = "ADMIN,STAFF 회원만 수정이 가능합니다.")
    @PutMapping("/{academyId}/parents/{parentId}")
    public ResponseEntity<Response<UpdateParentResponse>> update(@PathVariable Long academyId, @PathVariable Long parentId, UpdateParentRequest request, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        UpdateParentResponse response = parentService.updateParent(academyId, parentId, request, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 부모 정보 삭제
     */
    @Operation(summary = "학부모 삭제", description = "ADMIN,STAFF 회원만 삭제가 가능합니다. \n\n soft-delete 됩니다.")
    @DeleteMapping("/{academyId}/parents/{parentId}")
    public ResponseEntity<Response<DeleteParentResponse>> delete(@PathVariable Long academyId, @PathVariable Long parentId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteParentResponse response = parentService.deleteParent(academyId, parentId, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }
}

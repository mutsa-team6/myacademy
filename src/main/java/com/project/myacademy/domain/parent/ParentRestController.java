package com.project.myacademy.domain.parent;

import com.project.myacademy.domain.parent.dto.*;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class ParentRestController {

    private final ParentService parentService;

    /**
     * 부모 등록
     */
    @PostMapping("/{academyId}/parents")
    public ResponseEntity<Response<CreateParentResponse>> create(@PathVariable Long academyId, CreateParentRequest request, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateParentResponse response = parentService.createParent(academyId, request, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 부모 정보 단건 조회
     */
    @GetMapping("/{academyId}/parents/{parentId}")
    public ResponseEntity<Response<ReadParentResponse>> read(@PathVariable Long academyId, @PathVariable Long parentId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        ReadParentResponse response = parentService.readParent(academyId, parentId, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 부모 정보 수정
     */
    @PutMapping("/{academyId}/parents/{parentId}")
    public ResponseEntity<Response<UpdateParentResponse>> update(@PathVariable Long academyId, @PathVariable Long parentId, UpdateParentRequest request, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        UpdateParentResponse response = parentService.updateParent(academyId, parentId, request, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 부모 정보 삭제
     */
    @DeleteMapping("/{academyId}/parents/{parentId}")
    public ResponseEntity<Response<DeleteParentResponse>> delete(@PathVariable Long academyId, @PathVariable Long parentId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteParentResponse response = parentService.deleteParent(academyId, parentId, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }
}

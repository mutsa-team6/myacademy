package com.project.myacademy.domain.parent;

import com.project.myacademy.domain.parent.dto.*;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("/parents")
    public ResponseEntity<Response<CreateParentResponse>> create(CreateParentRequest request) {
        //String userName = authentication.getName();
        CreateParentResponse response = parentService.createParent(request);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 부모 정보 단건 조회
     */
    @GetMapping("/parents/{parentId}")
    public ResponseEntity<Response<FindParentResponse>> find(@PathVariable Long parentId) {
        FindParentResponse response = parentService.findParent(parentId);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 부모 정보 수정
     */
    @PutMapping("/parents/{parentId}")
    public ResponseEntity<Response<UpdateParentResponse>> update(@PathVariable Long parentId, UpdateParentRequest request) {
        UpdateParentResponse response = parentService.updateParent(parentId, request)
    }
}

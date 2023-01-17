package com.project.myacademy.domain.parent;

import com.project.myacademy.domain.parent.dto.CreateParentRequest;
import com.project.myacademy.domain.parent.dto.CreateParentResponse;
import com.project.myacademy.domain.parent.dto.FindParentResponse;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public Response<CreateParentResponse> create(CreateParentRequest request) {
        //String userName = authentication.getName();
        CreateParentResponse response = parentService.createParent(request);
        return Response.success(response);
    }

    /**
     * 부모 정보 단건 조회
     */
    @GetMapping("/parents/{parentId}")
    public Response<FindParentResponse> find(@PathVariable Long parentId) {
        FindParentResponse response = parentService.findParent(parentId);
        return Response.success(response);
    }
}

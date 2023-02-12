package com.project.myacademy.domain.academy;

import com.project.myacademy.domain.academy.dto.CreateAcademyRequest;
import com.project.myacademy.domain.academy.dto.CreateAcademyResponse;
import com.project.myacademy.domain.academy.dto.DeleteAcademyResponse;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.exception.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "01-1. 학원", description = "학원 등록, 조회, 삭제")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/academies")
public class AcademyRestController {

    private final AcademyService academyService;

    @Operation(summary = "학원 등록", description = "학원을 등록합니다. \n\n 같은 이름의 학원은 등록이 불가합니다.")
    @PostMapping("")
    public ResponseEntity<Response<CreateAcademyResponse>> create(@Validated @RequestBody CreateAcademyRequest request, BindingResult bindingResult) {

        if (bindingResult.hasFieldErrors()) {
            throw new BindingException(ErrorCode.BINDING_ERROR, bindingResult.getFieldError().getDefaultMessage());
        }

        CreateAcademyResponse response = academyService.createAcademy(request);

        return ResponseEntity.ok().body(Response.success(response));
    }

    @Operation(summary = "학원 삭제", description = "학원을 soft-delete 합니다.")
    @DeleteMapping("/{academyId}/delete")
    public ResponseEntity<Response<DeleteAcademyResponse>> delete(@PathVariable Long academyId) {

        Long deletedAcademyId = academyService.deleteAcademy(academyId);

        return ResponseEntity.ok(Response.success(new DeleteAcademyResponse(
                deletedAcademyId, "학원 삭제가 정상적으로 완료되었습니다.")));
    }
}
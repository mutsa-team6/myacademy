package com.project.myacademy.domain.academy;

import com.project.myacademy.domain.academy.dto.*;
import com.project.myacademy.global.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "01. 학원", description = "학원 등록, 조회, 삭제")
@RestController
@RequestMapping("/api/v1/academies")
@RequiredArgsConstructor
@Slf4j
public class AcademyRestController {

    // 학원 관련 기능은 일단 닫아두기 (관리자가 입력해줌)

    private final AcademyService academyService;

    @Operation(summary = "학원 등록", description = "학원을 등록합니다.")
    @PostMapping("")
    public ResponseEntity create(@RequestBody CreateAcademyRequest request) {

        CreateAcademyResponse response = academyService.createAcademy(request);

        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 학원 삭제
     *
     * @param academyId
     * @return ResponseEntity
     */
    @Operation(summary = "학원 삭제", description = "학원을 soft-delete 합니다.")
    @DeleteMapping("/{academyId}/delete")
    public ResponseEntity delete(@PathVariable Long academyId) {

        Long deletedAcademyId = academyService.deleteAcademy(academyId);

        return ResponseEntity.ok(Response.success(new DeleteAcademyResponse(
                deletedAcademyId,
                "학원 삭제가 정상적으로 완료되었습니다.")));
    }
}

//    /**
//     * 학원 정보 수정
//     *
//     * @param academyId
//     * @param reqeust
//     * @param authentication
//     * @return ResponseEntity
//     */
//    @PutMapping("/{academyId}")
//    public ResponseEntity update(@PathVariable Long academyId, @RequestBody UpdateAcademyReqeust reqeust, Authentication authentication) {
//        log.info("Academy id : " + academyId);
//
//        AcademyDto updatedAcademyDto = academyService.updateAcademy(academyId, reqeust, authentication.getName());
//
//        return ResponseEntity.ok(Response.success(new UpdateAcademyResponse(
//                updatedAcademyDto.getId(),
//                updatedAcademyDto.getName(),
//                updatedAcademyDto.getOwner(),
//                "학원 수정이 정상적으로 완료되었습니다.")));
//    }
//

//
//    /**
//     * 학원 로그인
//     *
//     * @param request
//     * @return ResponseEntity
//     */
//    @PostMapping("/login")
//    public ResponseEntity login(@RequestBody LoginAcademyRequest request) {
//
//        LoginAcademyResponse response = academyService.loginAcademy(request);
//        log.info("학원 로그인 정상적으로 완료되었습니다.");
//        log.info("학원 번호 : " + response.getAcademyId());
//        log.info("학원 토큰이 발급되었습니다.");
//
//        return ResponseEntity.ok(Response.success(response));
//    }
package com.project.myacademy.domain.academy;

import com.project.myacademy.domain.academy.dto.*;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academies")
@RequiredArgsConstructor
@Slf4j
public class AcademyRestController {

    // 학원 관련 기능은 일단 닫아두기 (관리자가 입력해줌)

    private final AcademyService academyService;

    /**
     * 학원 이름과 사업자 등록 번호를 받아서
     * 학원 찾기
     *
     * @param request
     * @return ResponseEntity
     */
    @PostMapping("/find")
    public ResponseEntity find(@RequestBody FindAcademyRequest request) {

        FindAcademyResponse response = academyService.findAcademy(request);

        log.info("🔎 검색하려는 학원이 존재함");

        return ResponseEntity.ok(Response.success(response.getAcademyId()));
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
//    /**
//     * 학원 삭제
//     *
//     * @param academyId
//     * @param authentication
//     * @return ResponseEntity
//     */
//    @DeleteMapping("/{academyId}")
//    public ResponseEntity delete(@PathVariable Long academyId, Authentication authentication) {
//        log.info("Academy id : " + academyId);
//
//        Long deletedAcademyId = academyService.deleteAcademy(academyId, authentication.getName());
//
//        return ResponseEntity.ok(Response.success(new DeleteAcademyResponse(
//                deletedAcademyId,
//                "학원 삭제가 정상적으로 완료되었습니다.")));
//    }
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
}

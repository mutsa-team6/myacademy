package com.project.myacademy.domain.waitinglist;

import com.project.myacademy.domain.waitinglist.dto.CreateWaitinglistResponse;
import com.project.myacademy.domain.waitinglist.dto.DeleteWaitinglistResponse;
import com.project.myacademy.domain.waitinglist.dto.ReadAllWaitinglistResponse;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.util.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "09. 수강대기", description = "대기번호 등록,조회")
@RestController
@RequestMapping("api/v1/academies")
@RequiredArgsConstructor
@Slf4j
public class WaitinglistRestController {

    private final WaitinglistService waitingListService;

    // 대기번호 전체 리스트 조회
    @Operation(summary = "대기번호 전체 조회", description = "대기번호 전체를 조회합니다.")
    @GetMapping("/{academyId}/waitinglists")
    public ResponseEntity<Response<Page<ReadAllWaitinglistResponse>>> readAll(@PathVariable("academyId") Long academyId, Authentication authentication,
                @PageableDefault(size = 20, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        Page<ReadAllWaitinglistResponse> waitinglists = waitingListService.readAllWaitinglists(academyId, requestAccount, pageable);
        log.info("대기번호 리스트 조회 성공");
        return ResponseEntity.ok().body(Response.success(waitinglists));
    }

    // 대기번호 등록
    @Operation(summary = "대기번호 등록", description = "ADMIN,STAFF 회원만 대기번호 등록이 가능합니다.")
    @PostMapping("/{academyId}/students/{studentId}/lectures/{lectureId}/waitinglists")
    public ResponseEntity<Response<CreateWaitinglistResponse>> create(@PathVariable("academyId") Long academyId,
                                                                      @PathVariable("studentId") Long studentId,
                                                                      @PathVariable("lectureId") Long lectureId,
                                                                      Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateWaitinglistResponse createWaitinglist = waitingListService.createWaitinglist(academyId, studentId, lectureId, requestAccount);
        log.info("대기번호 생성 성공");
        return ResponseEntity.ok().body(Response.success(createWaitinglist));

    }

    @Operation(summary = "대기번호 삭제", description = "ADMIN, STAFF 회원만 대기번호 삭제 가능합니다.")
    @DeleteMapping("/{academyId}/students/{studentId}/lectures/{lectureId}/waitinglists/{waitinglistId}")
    public ResponseEntity<Response<DeleteWaitinglistResponse>> create(@PathVariable("academyId") Long academyId,
                                                                      @PathVariable("studentId") Long studentId,
                                                                      @PathVariable("lectureId") Long lectureId,
                                                                      @PathVariable("waitinglistId") Long waitinglistId,
                                                                      Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteWaitinglistResponse deletedWaitinglist = waitingListService.deleteWaitinglist(academyId, studentId, lectureId, waitinglistId, requestAccount);
        log.info("대기번호 삭제 성공");
        return ResponseEntity.ok().body(Response.success(deletedWaitinglist));
    }
}
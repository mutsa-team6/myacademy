package com.project.myacademy.domain.waitinglist;

import com.project.myacademy.domain.waitinglist.dto.CreateWaitinglistRequest;
import com.project.myacademy.domain.waitinglist.dto.CreateWaitinglistResponse;
import com.project.myacademy.domain.waitinglist.dto.ReadAllWaitinglistResponse;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/academies")
@RequiredArgsConstructor
@Slf4j
public class WaitinglistRestController {

    private final WaitinglistService waitingListService;

    // 대기번호 전체 리스트 조회
    @GetMapping("/{academyId}/waitinglists")
    public ResponseEntity<Response<Page<ReadAllWaitinglistResponse>>> readAll(@PathVariable("academyId") Long academyId, Authentication authentication,
                @PageableDefault(size = 20, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReadAllWaitinglistResponse> waitinglists = waitingListService.readAllWaitinglists(academyId, authentication.getName(), pageable);
        log.info("대기번호 리스트 조회 성공");
        return ResponseEntity.ok().body(Response.success(waitinglists));
    }

    // 대기번호 등록
    @PostMapping("/{academyId}/students/{studentId}/lectures/{lectureId}/waitinglists")
    public ResponseEntity<Response<CreateWaitinglistResponse>> create(@PathVariable("academyId") Long academyId,
                                                                      @PathVariable("studentId") Long studentId,
                                                                      @PathVariable("lectureId") Long lectureId,
                                                                      @RequestBody CreateWaitinglistRequest request,
                                                                      Authentication authentication) {
        CreateWaitinglistResponse createWaitinglist = waitingListService.createWaitinglist(academyId, studentId, lectureId, request, authentication.getName());
        log.info("대기번호 생성 성공");
        return ResponseEntity.ok().body(Response.success(createWaitinglist));

    }
}
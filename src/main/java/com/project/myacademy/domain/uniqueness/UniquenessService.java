package com.project.myacademy.domain.uniqueness;

import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.domain.uniqueness.dto.*;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UniquenessService {

    private final UniquenessRepository uniquenessRepository;
    private final StudentRepository studentRepository;

    /**
     * @param studentId 특이사항의 대상이 되는 학생 Id
     * @param request   특이사항의 요청시 받는 request Dto
     */
    @Transactional
    public CreateUniquenessResponse createUniqueness(Long studentId, CreateUniquenessRequest request) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        Uniqueness savedUniqueness = uniquenessRepository.save(Uniqueness.toUniqueness(request, student));

        return CreateUniquenessResponse.of(savedUniqueness);
    }

    /**
     * @param studentId 특이사항의 대상이 되는 학생 Id
     * @param pageable  20개씩 id순서대로(최신순대로)
     */
    public Page<ReadAllUniquenessResponse> readAllUniqueness(Long studentId, PageRequest pageable) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        return uniquenessRepository.findAllByStudent(student, pageable).map(uniqueness -> ReadAllUniquenessResponse.of(uniqueness));
    }

    /**
     * @param studentId    특이사항의 대상이 되는 학생 Id
     * @param uniquenessId 수정하려고하는 특이사항 Id
     * @param request      수정내용이 담긴 dto
     */
    @Transactional
    public UpdateUniquenessResponse updateUniqueness(Long studentId, Long uniquenessId, UpdateUniquenessRequest request) {

        studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        Uniqueness uniqueness = uniquenessRepository.findById(uniquenessId)
                .orElseThrow(() -> new AppException(ErrorCode.UNIQUENESS_NOT_FOUND));

        uniqueness.updateUniqueness(request);

        return UpdateUniquenessResponse.of(uniqueness);
    }

    /**
     * @param studentId    특이사항의 대상이 되는 학생 Id
     * @param uniquenessId 삭제하려고 하는 특이사항 Id
     */
    @Transactional
    public DeleteUniquenessResponse deleteUniqueness(Long studentId, Long uniquenessId) {

        studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        Uniqueness uniqueness = uniquenessRepository.findById(uniquenessId)
                .orElseThrow(() -> new AppException(ErrorCode.UNIQUENESS_NOT_FOUND));

        uniquenessRepository.delete(uniqueness);

        return DeleteUniquenessResponse.of(uniqueness);
    }
}

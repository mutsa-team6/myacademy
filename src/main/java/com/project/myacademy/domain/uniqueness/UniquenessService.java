package com.project.myacademy.domain.uniqueness;

import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.domain.uniqueness.dto.CreateUniquenessRequest;
import com.project.myacademy.domain.uniqueness.dto.CreateUniquenessResponse;
import com.project.myacademy.domain.uniqueness.dto.ReadAllUniquenessResponse;
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
}

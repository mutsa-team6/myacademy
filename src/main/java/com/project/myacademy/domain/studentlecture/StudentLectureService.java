package com.project.myacademy.domain.studentlecture;

import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.lecture.LectureRepository;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.domain.studentlecture.dto.CreateStudentLectureRequest;
import com.project.myacademy.domain.studentlecture.dto.CreateStudentLectureResponse;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StudentLectureService {

    private final StudentLectureRepository studentLectureRepository;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;
    private final LectureRepository lectureRepository;
    public CreateStudentLectureResponse createStudentLecture(Long studentId, Long lectureId, CreateStudentLectureRequest request) {

        // Authentication으로 넘어온 직원 정보 확인, 없으면 강좌 생성 불가
//        Employee employee = employeeRepository.findByName(employeeName)
//                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PERMISSION));

        // 학생 존재 유무 확인
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        // 강좌의 존재 유무 확인
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));

        StudentLecture savedStudentLecture = studentLectureRepository.save(StudentLecture.addClass(student, lecture, request));

        return CreateStudentLectureResponse.of(savedStudentLecture.getId());
    }
}

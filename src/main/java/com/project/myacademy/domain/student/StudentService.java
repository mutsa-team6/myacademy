package com.project.myacademy.domain.student;

import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.parent.Parent;
import com.project.myacademy.domain.parent.ParentRepository;
import com.project.myacademy.domain.student.dto.*;
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
public class StudentService {
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * 학생 등록
     */
    @Transactional
    public CreateStudentResponse createStudent(CreateStudentRequest request, String account) {

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                        .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        //중복체크
        studentRepository.findByPhoneNum(request.getPhoneNum())
                .ifPresent(user -> {
                    throw new AppException(ErrorCode.DUPLICATED_STUDENT);
                });

        //request의 parentPhoneNum으로 조회했을때 없으면 null 값을 parent에 저장
        Parent parent = parentRepository.findByPhoneNum(request.getParentPhoneNum())
                .orElseGet(() -> null);

        Student savedStudent = studentRepository.save(Student.toStudent(request, parent));

        return CreateStudentResponse.of(savedStudent);
    }

    /**
     * 학생 정보 단건 조회
     */
    public FindStudentResponse findStudent(Long studentsId, String account) {

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Student student = studentRepository.findById(studentsId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        return FindStudentResponse.of(student);
    }

    /**
     * 학생 정보 전체 조회
     */
    public Page<FindAllStudentResponse> findAllStudent(PageRequest pageable, String account) {

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        return studentRepository.findAll(pageable).map(student -> FindAllStudentResponse.of(student));
    }

    /**
     * 학생 정보 수정
     */
    @Transactional
    public UpdateStudentResponse updateStudent(long studentsId, UpdateStudentRequest request, String account) {

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Student student = studentRepository.findById(studentsId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        student.updateStudent(request);

        return UpdateStudentResponse.of(student);
    }

    /**
     * 학생 정보 삭제
     */
    @Transactional
    public DeleteStudentResponse deleteStudent(Long studentId, String account) {

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        studentRepository.delete(student);

        return DeleteStudentResponse.of(student);
    }
}

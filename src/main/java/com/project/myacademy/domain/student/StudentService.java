package com.project.myacademy.domain.student;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
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
@Transactional(readOnly = true)
public class StudentService {
    /**
     * 학생이 2개의 학원을 다닐경우 처리가 필요함 (회의필요)
     */
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final EmployeeRepository employeeRepository;
    private final AcademyRepository academyRepository;

    /**
     * @param academyId 학원 id
     * @param request   학생등록 정보가 담긴 dto
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public CreateStudentResponse createStudent(Long academyId, CreateStudentRequest request, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);

        //학생이 학원을 두개다닌다면? (이부분 처리 필요함)
        //학생 중복 체크
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
     * @param studentId PathVariable로 받아온 조회할 학생 id
     * @param academyId 학원 id
     * @param account    jwt로 받아온 사용자(Employee) 계정
     */
    public FindStudentResponse findStudent(Long academyId, Long studentId, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        //student Id에 해당하는 학생이 존재하는지 확인
        Student student = validateStudent(studentId);

        return FindStudentResponse.of(student);
    }

    /**
     * @param academyId
     * @param pageable  page 설정 : 20개씩 조회
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    public Page<FindAllStudentResponse> findAllStudent(Long academyId, PageRequest pageable, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);

        return studentRepository.findAll(pageable).map(student -> FindAllStudentResponse.of(student));
    }

    /**
     * @param studentId PathVariable로 받아온 수정할 학생 id
     * @param academyId 학원 id
     * @param request   수정할 내용을 담은 requestDto
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public UpdateStudentResponse updateStudent(Long academyId, long studentId, UpdateStudentRequest request, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        //student Id에 해당하는 학생이 존재하는지 확인
        Student student = validateStudent(studentId);

        student.updateStudent(request);

        return UpdateStudentResponse.of(student);
    }

    /**
     * @param studentId PathVariable로 받아온 삭제할 학생 id
     * @param academyId 학원 id
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public DeleteStudentResponse deleteStudent(Long academyId, Long studentId, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        //student Id에 해당하는 학생이 존재하는지 확인
        Student student = validateStudent(studentId);

        studentRepository.delete(student);

        return DeleteStudentResponse.of(student);
    }

    private Student validateStudent(Long studentId) {
        // 학생 존재 유무 확인
        Student validateStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
        return validateStudent;
    }

    private Academy validateAcademy(Long academyId) {
        // 학원 존재 유무 확인
        Academy validatedAcademy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
        return validatedAcademy;
    }

    private Employee validateAcademyEmployee(String account, Academy academy) {
        // 해당 학원 소속 직원 맞는지 확인
        Employee employee = employeeRepository.findByAccountAndAcademy(account, academy)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        return employee;
    }
}

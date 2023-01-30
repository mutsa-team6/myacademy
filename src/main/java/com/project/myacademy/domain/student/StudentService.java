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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        // 학생을 관리 할 수 있는 권한인지 확인(강사만 불가능)
        if(Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
        //학원 id에 부모 존재 유무 확인
        Parent parent = validateParent(academyId,request.getParentPhoneNum());

        //학생 PhoneNum 과 academyId로 학생 중복 체크
        studentRepository.findByPhoneNumAndAcademyId(request.getPhoneNum(),academyId)
                .ifPresent(user -> {
                    throw new AppException(ErrorCode.DUPLICATED_STUDENT);
                });


        Student savedStudent = studentRepository.save(Student.toStudent(request, parent, academyId));

        return CreateStudentResponse.of(savedStudent);
    }

    /**
     * @param studentId PathVariable로 받아온 조회할 학생 id
     * @param academyId 학원 id
     * @param account    jwt로 받아온 사용자(Employee) 계정
     */
    public ReadStudentResponse readStudent(Long academyId, Long studentId, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        // 학생을 관리 할 수 있는 권한인지 확인(강사만 불가능)
        if(Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
        //student Id에 해당하는 학생이 존재하는지 확인
        Student student = validateStudent(academyId, studentId);

        return ReadStudentResponse.of(student);
    }

    /**
     * @param academyId
     * @param pageable  page 설정 : 20개씩 조회
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    public Page<ReadAllStudentResponse> readAllStudent(Long academyId, Pageable pageable, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        // 학생을 관리 할 수 있는 권한인지 확인(강사만 불가능)
        if(Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        return studentRepository.findAllByAcademyId(pageable, academyId).map(ReadAllStudentResponse::of);
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
        // 학생을 관리 할 수 있는 권한인지 확인(강사만 불가능)
        if(Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
        //student Id에 해당하는 학생이 존재하는지 확인
        Student student = validateStudent(academyId, studentId);

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
        // 학생을 관리 할 수 있는 권한인지 확인(강사만 불가능)
        if(Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
        //student Id에 해당하는 학생이 존재하는지 확인
        Student student = validateStudent(academyId,studentId);

        studentRepository.delete(student);

        return DeleteStudentResponse.of(student);
    }

    /**
     * 이름으로 학생 가져오는 UI용 메서드
     */
    public List<ReadAllStudentResponse> findStudentForStudentList(Long academyId, String studentName) {

        List<Student> foundStudents = studentRepository.findByAcademyIdAndName(academyId, studentName);

        return foundStudents.stream().map(student -> ReadAllStudentResponse.of(student)).collect(Collectors.toList());

    }
    private Student validateStudent(Long academyId, Long studentId) {
        // 학생 존재 유무 확인
        Student student = studentRepository.findByAcademyIdAndId(academyId, studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
        return student;
    }

    private Academy validateAcademy(Long academyId) {
        // 학원 존재 유무 확인
        Academy academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
        return academy;
    }

    private Parent validateParent(Long academyId, String phoneNum) {
        //부모 존재 유무 확인
        Parent parent = parentRepository.findByPhoneNumAndAcademyId(phoneNum,academyId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_FOUND));
        return parent;
    }

    private Employee validateAcademyEmployee(String account, Academy academy) {
        // 해당 학원 소속 직원 맞는지 확인
        Employee employee = employeeRepository.findByAccountAndAcademy(account, academy)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        return employee;
    }
}

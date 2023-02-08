package com.project.myacademy.domain.student;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final EmployeeRepository employeeRepository;
    private final AcademyRepository academyRepository;

    /**
     * 학생 등록
     *
     * @param academyId 학원 id
     * @param request   학생등록 정보가 담긴 dto
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public CreateStudentResponse createStudent(Long academyId, CreateStudentRequest request, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);
        // 학원 Id와 부모 전화번호로 부모 조회 - 없을시 PARENT_NOT_FOUND 에러발생
        Parent parent = validateParentByPhoneNum(academyId, request.getParentPhoneNum());

        //학생 PhoneNum 과 학원Id로 학생 중복 체크 - 있으면 DUPLICATED_STUDENT 에러발생
        studentRepository.findByPhoneNumAndAcademyId(request.getPhoneNum(), academyId)
                .ifPresent(user -> {
                    throw new AppException(ErrorCode.DUPLICATED_STUDENT);
                });
        //학원 Id와 요청받은 학생 EMail로 중복 체크 - 있으면 DUPLICATED_EMAIL 에러발생
        studentRepository.findByEmailAndAcademyId(request.getEmail(), academyId)
                .ifPresent(user -> {
                    throw new AppException(ErrorCode.DUPLICATED_EMAIL);
                });


        Student savedStudent = studentRepository.save(Student.toStudent(request, parent, academyId));

        return CreateStudentResponse.of(savedStudent);
    }

    /**
     * 학생 단건 조회
     *
     * @param studentId PathVariable로 받아온 조회할 학생 id
     * @param academyId 학원 id
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    public ReadStudentResponse readStudent(Long academyId, Long studentId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 학생이 등록되어 있는지 확인 - 없으면 STUDENT_NOT_FOUND 에러발생
        Student student = validateStudentById(academyId, studentId);

        return ReadStudentResponse.of(student);
    }

    /**
     * 학생 전체 조회
     *
     * @param academyId 학원 Id
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    public Page<ReadAllStudentResponse> readAllStudent(Long academyId, Pageable pageable, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        return studentRepository.findAllByAcademyId(pageable, academyId).map(ReadAllStudentResponse::of);
    }

    /**
     * 학생 수정
     *
     * @param studentId 수정할 학생 id
     * @param academyId 학원 id
     * @param request   수정할 내용을 담은 requestDto
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public UpdateStudentResponse updateStudent(Long academyId, long studentId, UpdateStudentRequest request, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);
        // 학생이 등록되어 있는지 확인 - 없으면 STUDENT_NOT_FOUND 에러발생
        Student existStudent = validateStudentById(academyId, studentId);

        //변경될 Email이 기존에 있으면 에러처리;
        Optional<Student> sameEmailStudent = Optional.of(studentRepository.findByEmailAndAcademyId(request.getEmail(), academyId)
                .orElseGet(() -> existStudent));
        if (sameEmailStudent.get() != existStudent) {
            throw new AppException(ErrorCode.DUPLICATED_EMAIL);
        }

        //변경될 PhonNum이 기존에 있으면 에러처리
        Optional<Student> samePhoneNumStudent = Optional.of(studentRepository.findByPhoneNumAndAcademyId(request.getPhoneNum(), academyId)
                .orElseGet(() -> existStudent));
        if (samePhoneNumStudent.get() != existStudent) {
            throw new AppException(ErrorCode.DUPLICATED_PHONENUM);
        }

        existStudent.updateStudent(request);

        return UpdateStudentResponse.of(existStudent);
    }

    /**
     * 학생 삭제
     *
     * @param studentId PathVariable로 받아온 삭제할 학생 id
     * @param academyId 학원 id
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public DeleteStudentResponse deleteStudent(Long academyId, Long studentId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        // 학생이 등록되어 있는지 확인 - 없으면 STUDENT_NOT_FOUND 에러발생
        Student student = validateStudentById(academyId, studentId);

        studentRepository.delete(student);

        return DeleteStudentResponse.of(student);
    }

    /**
     * 이름으로 학생 가져오는 UI용 메서드
     */
    public Page<ReadAllStudentResponse> findStudentForStudentList(Long academyId, String studentName, Pageable pageable) {

        Page<Student> foundStudents = studentRepository.findByAcademyIdAndName(academyId, studentName, pageable);

        return foundStudents.map(student -> ReadAllStudentResponse.of(student));

    }

    // 학생이 등록되어 있는지 확인 - 없으면 STUDENT_NOT_FOUND 에러발생
    private Student validateStudentById(Long academyId, Long studentId) {
        Student student = studentRepository.findByAcademyIdAndId(academyId, studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
        return student;
    }

    // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
    public Academy validateAcademyById(Long academyId) {

        Academy validateAcademy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
        return validateAcademy;
    }

    // 학원 Id와 부모 전화번호로 부모 조회 - 없을시 PARENT_NOT_FOUND 에러발생
    private Parent validateParentByPhoneNum(Long academyId, String phoneNum) {
        Parent parent = parentRepository.findByPhoneNumAndAcademyId(phoneNum, academyId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_FOUND));
        return parent;
    }

    // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
    public Employee validateRequestEmployeeByAcademy(String account, Academy academy) {
        Employee employee = employeeRepository.findByAccountAndAcademy(account, academy)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND));
        return employee;
    }

    // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
    public void validateAuthorityUser(Employee employee) {
        if (employee.getEmployeeRole().equals(EmployeeRole.ROLE_USER)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }
}

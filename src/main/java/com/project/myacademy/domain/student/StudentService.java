package com.project.myacademy.domain.student;

import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.parent.Parent;
import com.project.myacademy.domain.parent.ParentRepository;
import com.project.myacademy.domain.student.dto.CreateStudentRequest;
import com.project.myacademy.domain.student.dto.CreateStudentResponse;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;

    /**
     * 학생 등록
     */
    public CreateStudentResponse createStudent(CreateStudentRequest request) {

        //토큰에 들어있는 userName이 Employ에 있는지 확인

        //중복된 학생인지 체크
        studentRepository.findByPhoneNum(request.getPhoneNum())
                .ifPresent(user -> {
                    throw new AppException(ErrorCode.DUPLICATED_STUDENT);
                });

        //부모 번호를 잘못 입력한 경우라면?

        //request의 parentPhoneNum으로 조회했을때 없으면 null 값을 parent에 저장
        Parent parent = parentRepository.findByPhoneNum(request.getParentPhoneNum())
                .orElseGet(() -> null);

        Student savedStudent = studentRepository.save(Student.toStudent(request, parent));

        return CreateStudentResponse.of(savedStudent);
    }
}

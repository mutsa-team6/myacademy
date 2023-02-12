package com.project.myacademy.domain.uniqueness;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.domain.uniqueness.dto.CreateUniquenessRequest;
import com.project.myacademy.domain.uniqueness.dto.UpdateUniquenessRequest;
import com.project.myacademy.domain.uniqueness.util.AcademyFixtureUtil;
import com.project.myacademy.domain.uniqueness.util.EmployeeFixtureUtil;
import com.project.myacademy.domain.uniqueness.util.StudentFixtureUtil;
import com.project.myacademy.domain.uniqueness.util.UniquenessFixtureUtil;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UniquenessServiceTest {

    UniquenessRepository uniquenessRepository = mock(UniquenessRepository.class);
    StudentRepository studentRepository = mock(StudentRepository.class);
    EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    AcademyRepository academyRepository = mock(AcademyRepository.class);
    UniquenessService uniquenessService = new UniquenessService(uniquenessRepository, studentRepository, employeeRepository, academyRepository);
    private Employee mockEmployee = mock(Employee.class);

    @Nested
    @DisplayName("특이사항 등록 테스트")
    class createUniqueness {

        CreateUniquenessRequest request = new CreateUniquenessRequest("body");
        Academy academy1 = AcademyFixtureUtil.ACADEMY1.init();
        Employee employee1 = EmployeeFixtureUtil.ROLE_ADMIN.init();
        Student student1 = StudentFixtureUtil.STUDENT1.init();
        Uniqueness uniqueness1 = UniquenessFixtureUtil.UNIQUENESS1.init();

        @Test
        @DisplayName("특이사항 등록 : 성공")
        void createUniquenessSuccess() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));
            when(uniquenessRepository.save(any())).thenReturn(uniqueness1);

            assertEquals("body", uniquenessService.createUniqueness(1L, 1L, request, "admin").getBody());
        }

        @Test
        @DisplayName("특이사항 등록 : 실패 - 학원에러")
        void createUniquenessFailAcademy() {

            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));
            when(uniquenessRepository.save(any())).thenReturn(uniqueness1);

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.createUniqueness(1L, 1L, request, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("특이사항 등록 : 실패 - 직원에러")
        void createUniquenessFailEmployee() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));
            when(uniquenessRepository.save(any())).thenReturn(uniqueness1);

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.createUniqueness(1L, 1L, request, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("특이사항 등록 : 실패 - 학생에러")
        void createUniquenessFailStudent() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(uniquenessRepository.save(any())).thenReturn(uniqueness1);

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.createUniqueness(1L, 1L, request, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("특이사항 조회 테스트")
    class readUniqueness {
        Academy academy1 = AcademyFixtureUtil.ACADEMY1.init();
        Employee employee1 = EmployeeFixtureUtil.ROLE_ADMIN.init();
        Student student1 = StudentFixtureUtil.STUDENT1.init();
        Uniqueness uniqueness1 = UniquenessFixtureUtil.UNIQUENESS1.init();
        Uniqueness uniqueness2 = UniquenessFixtureUtil.UNIQUENESS2.init();
        PageImpl<Uniqueness> uniquenessList = new PageImpl<>(List.of(uniqueness1, uniqueness2));
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("id").descending());

        @Test
        @DisplayName("특이사항 조회 : 성공")
        void readUniquenessSuccess() {

            ReflectionTestUtils.setField(uniqueness1, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(uniqueness2, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(mockEmployee));
            when(mockEmployee.getEmployeeRole()).thenReturn(EmployeeRole.ROLE_ADMIN);
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));
            when(uniquenessRepository.findAllByStudent(student1, pageable)).thenReturn(uniquenessList);

            assertDoesNotThrow(() -> uniquenessService.readAllUniqueness(academy1.getId(), student1.getId(), pageable, employee1.getAccount()));
        }

        @Test
        @DisplayName("특이사항 조회 : 실패 - 학원에러")
        void readUniquenessFailAcademy() {

            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));
            when(uniquenessRepository.findAllByStudent(any(), any())).thenReturn(uniquenessList);

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.readAllUniqueness(1L, 1L, pageable, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("특이사항 조회 : 실패 - 학생에러")
        void readUniquenessFailStudent() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(mockEmployee));
            when(mockEmployee.getEmployeeRole()).thenReturn(EmployeeRole.ROLE_ADMIN);
            when(studentRepository.findById(any())).thenReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.readAllUniqueness(1L, 1L, pageable, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("특이사항 수정 테스트")
    class updateUniqueness {

        UpdateUniquenessRequest request = new UpdateUniquenessRequest("updatedbody");
        Academy academy1 = AcademyFixtureUtil.ACADEMY1.init();
        Employee employee1 = EmployeeFixtureUtil.ROLE_ADMIN.init();
        Student student1 = StudentFixtureUtil.STUDENT1.init();
        Uniqueness uniqueness1 = UniquenessFixtureUtil.UNIQUENESS1.init();

        @Test
        @DisplayName("특이사항 수정 : 성공")
        void updateUniquenessSuccess() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));
            when(uniquenessRepository.findById(any())).thenReturn(Optional.of(uniqueness1));

            assertEquals("updatedbody", uniquenessService.updateUniqueness(1L, 1L, 1L, request, "admin").getBody());
        }

        @Test
        @DisplayName("특이사항 수정 : 실패 - 학원에러")
        void updateUniquenessFailAcademy() {

            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));
            when(uniquenessRepository.findById(any())).thenReturn(Optional.of(uniqueness1));

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.updateUniqueness(1L, 1L, 1L, request, "admin").getBody());
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("특이사항 수정 : 실패 - 직원에러")
        void updateUniquenessFailEmployee() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));
            when(uniquenessRepository.findById(any())).thenReturn(Optional.of(uniqueness1));

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.updateUniqueness(1L, 1L, 1L, request, "admin").getBody());
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("특이사항 수정 : 실패 - 학생에러")
        void updateUniquenessFailStudent() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(uniquenessRepository.findById(any())).thenReturn(Optional.of(uniqueness1));

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.updateUniqueness(1L, 1L, 1L, request, "admin").getBody());
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
        }

        @Test
        @DisplayName("특이사항 수정 : 실패 - 특이사항에러")
        void updateUniquenessFailUniqueness() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.updateUniqueness(1L, 1L, 1L, request, "admin").getBody());
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.UNIQUENESS_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("특이사항 삭제 테스트")
    class deleteUniqueness {

        Academy academy1 = AcademyFixtureUtil.ACADEMY1.init();
        Employee employee1 = EmployeeFixtureUtil.ROLE_ADMIN.init();
        Student student1 = StudentFixtureUtil.STUDENT1.init();
        Uniqueness uniqueness1 = UniquenessFixtureUtil.UNIQUENESS1.init();

        @Test
        @DisplayName("특이사항 삭제 : 성공")
        void deleteUniquenessSuccess() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));
            when(uniquenessRepository.findById(any())).thenReturn(Optional.of(uniqueness1));

            assertDoesNotThrow(() -> uniquenessService.deleteUniqueness(1L, 1L, 1L, "admin"));
        }

        @Test
        @DisplayName("특이사항 삭제 : 실패 - 학원에러")
        void deleteUniquenessFailAcademy() {

            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));
            when(uniquenessRepository.findById(any())).thenReturn(Optional.of(uniqueness1));

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.deleteUniqueness(1L, 1L, 1L, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("특이사항 삭제 : 실패 - 직원에러")
        void deleteUniquenessFailEmployee() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));
            when(uniquenessRepository.findById(any())).thenReturn(Optional.of(uniqueness1));

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.deleteUniqueness(1L, 1L, 1L, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("특이사항 삭제 : 실패 - 학생에러")
        void deleteUniquenessFailStudent() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(uniquenessRepository.findById(any())).thenReturn(Optional.of(uniqueness1));

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.deleteUniqueness(1L, 1L, 1L, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
        }

        @Test
        @DisplayName("특이사항 삭제 : 실패 - 특이사항에러")
        void deleteUniquenessFailUniqueness() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(studentRepository.findById(any())).thenReturn(Optional.of(student1));

            AppException appException = assertThrows(AppException.class, () -> uniquenessService.deleteUniqueness(1L, 1L, 1L, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.UNIQUENESS_NOT_FOUND);
        }
    }
}
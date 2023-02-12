package com.project.myacademy.domain.parent;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.parent.dto.CreateParentRequest;
import com.project.myacademy.domain.parent.dto.UpdateParentRequest;
import com.project.myacademy.domain.parent.util.AcademyFixtureUtil;
import com.project.myacademy.domain.parent.util.EmployeeFixtureUtil;
import com.project.myacademy.domain.parent.util.ParentFixtureUtil;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParentServiceTest {

    ParentRepository parentRepository = mock(ParentRepository.class);
    EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    AcademyRepository academyRepository = mock(AcademyRepository.class);

    ParentService parentService = new ParentService(parentRepository, employeeRepository, academyRepository);
    Employee mockEmployee = mock(Employee.class);

    @Nested
    @DisplayName("부모 정보 등록 테스트")
    class createParent {

        CreateParentRequest request = new CreateParentRequest("name", "phoneNum", "address");
        Academy academy1 = AcademyFixtureUtil.ACADEMY1.init();
        Employee employee1 = EmployeeFixtureUtil.ROLE_ADMIN.init();
        Parent parent1 = ParentFixtureUtil.PARENT1.init();


        @Test
        @DisplayName("부모 정보 등록 : 성공")
        void createParentSuccess() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(parentRepository.findByPhoneNumAndAcademyId(any(), any())).thenReturn(Optional.empty());
            when(parentRepository.save(any())).thenReturn(parent1);

            assertEquals("name", parentService.createParent(1L, request, "admin").getName());
        }

        @Test
        @DisplayName("부모 정보 등록 : 실패 - 학원에러")
        void createParentFailAcademy() {

            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(parentRepository.findByPhoneNumAndAcademyId(any(), any())).thenReturn(Optional.empty());
            when(parentRepository.save(any())).thenReturn(parent1);

            AppException appException = assertThrows(AppException.class, () -> parentService.createParent(1L, request, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("부모 정보 등록 : 실패 - 직원에러")
        void createParentFailEmployee() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(parentRepository.findByPhoneNumAndAcademyId(any(), any())).thenReturn(Optional.empty());
            when(parentRepository.save(any())).thenReturn(parent1);

            AppException appException = assertThrows(AppException.class, () -> parentService.createParent(1L, request, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("부모 정보 등록 : 실패 - 권한에러")
        void createParentFailPermission() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(mockEmployee));
            when(mockEmployee.getEmployeeRole()).thenReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class, () -> parentService.createParent(1L, request, "user"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
        }

        @Test
        @DisplayName("부모 정보 등록 : 실패 - 부모에러")
        void createParentFailParent() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(parentRepository.findByPhoneNumAndAcademyId(any(), any())).thenReturn(Optional.of(parent1));
            when(parentRepository.save(any())).thenReturn(parent1);

            AppException appException = assertThrows(AppException.class, () -> parentService.createParent(1L, request, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_PARENT);
        }
    }

    @Nested
    @DisplayName("부모 정보 조회 테스트")
    class readParent {

        Academy academy1 = AcademyFixtureUtil.ACADEMY1.init();
        Employee employee1 = EmployeeFixtureUtil.ROLE_ADMIN.init();
        Parent parent1 = ParentFixtureUtil.PARENT1.init();

        @Test
        @DisplayName("부모 정보 조회 : 성공")
        void readParentSuccess() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(parentRepository.findByIdAndAcademyId(any(), any())).thenReturn(Optional.of(parent1));

            assertEquals("name", parentService.readParent(1L, 1L, "admin").getName());
        }

        @Test
        @DisplayName("부모 정보 조회 : 실패 - 학원에러")
        void readParentFailAcademy() {

            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(parentRepository.findByIdAndAcademyId(any(), any())).thenReturn(Optional.of(parent1));

            AppException appException = assertThrows(AppException.class, () -> parentService.readParent(1L, 1L, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("부모 정보 조회 : 실패 - 직원에러")
        void readParentFailEmployee() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(parentRepository.findByIdAndAcademyId(any(), any())).thenReturn(Optional.of(parent1));

            AppException appException = assertThrows(AppException.class, () -> parentService.readParent(1L, 1L, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("부모 정보 조회 : 실패 - 부모에러")
        void readParentFailParent() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));

            AppException appException = assertThrows(AppException.class, () -> parentService.readParent(1L, 1L, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.PARENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("부모 정보 수정 테스트")
    class updateParent {

        UpdateParentRequest request = new UpdateParentRequest("updatedname", "phoneNum", "address");
        Academy academy1 = AcademyFixtureUtil.ACADEMY1.init();
        Employee employee1 = EmployeeFixtureUtil.ROLE_ADMIN.init();
        Parent parent1 = ParentFixtureUtil.PARENT1.init();

        @Test
        @DisplayName("부모 정보 수정 : 성공")
        void updateParentSuccess() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(parentRepository.findByIdAndAcademyId(any(), any())).thenReturn(Optional.of(parent1));

            assertEquals("updatedname", parentService.updateParent(1L, 1L, request, "admin").getName());
        }

        @Test
        @DisplayName("부모 정보 수정 : 실패 - 학원에러")
        void updateParentFailAcademy() {

            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(parentRepository.findByIdAndAcademyId(any(), any())).thenReturn(Optional.of(parent1));

            AppException appException = assertThrows(AppException.class, () -> parentService.updateParent(1L, 1L, request, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("부모 정보 수정 : 실패 - 직원에러")
        void updateParentFailEmployee() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(parentRepository.findByIdAndAcademyId(any(), any())).thenReturn(Optional.of(parent1));

            AppException appException = assertThrows(AppException.class, () -> parentService.updateParent(1L, 1L, request, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("부모 정보 수정 : 실패 - 권한에러")
        void updateParentFailPermission() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(mockEmployee));
            when(mockEmployee.getEmployeeRole()).thenReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class, () -> parentService.updateParent(1L, 1L, request, "user"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
        }

        @Test
        @DisplayName("부모 정보 수정 : 실패 - 부모에러")
        void updateParentFailParent() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));

            AppException appException = assertThrows(AppException.class, () -> parentService.updateParent(1L, 1L, request, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.PARENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("부모 정보 삭제 테스트")
    class deleteParent {

        Academy academy1 = AcademyFixtureUtil.ACADEMY1.init();
        Employee employee1 = EmployeeFixtureUtil.ROLE_ADMIN.init();
        Parent parent1 = ParentFixtureUtil.PARENT1.init();

        @Test
        @DisplayName("부모 정보 삭제 : 성공")
        void deleteParentSuccess() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(parentRepository.findByIdAndAcademyId(any(), any())).thenReturn(Optional.of(parent1));

            assertDoesNotThrow(() -> parentService.deleteParent(1L, 1L, "admin"));
        }

        @Test
        @DisplayName("부모 정보 삭제 : 실패 - 학원에러")
        void deleteParentFailAcademy() {

            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));
            when(parentRepository.findByIdAndAcademyId(any(), any())).thenReturn(Optional.of(parent1));

            AppException appException = assertThrows(AppException.class, () -> parentService.deleteParent(1L, 1L, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("부모 정보 삭제 : 실패 - 직원에러")
        void deleteParentFailEmployee() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(parentRepository.findByIdAndAcademyId(any(), any())).thenReturn(Optional.of(parent1));

            AppException appException = assertThrows(AppException.class, () -> parentService.deleteParent(1L, 1L, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("부모 정보 삭제 : 실패 - 권한에러")
        void deleteParentFailPermission() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(mockEmployee));
            when(mockEmployee.getEmployeeRole()).thenReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class, () -> parentService.deleteParent(1L, 1L, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
        }

        @Test
        @DisplayName("부모 정보 삭제 : 실패 - 부모에러")
        void deleteParentFailParent() {

            when(academyRepository.findById(any())).thenReturn(Optional.of(academy1));
            when(employeeRepository.findByAccountAndAcademy(any(), any())).thenReturn(Optional.of(employee1));

            AppException appException = assertThrows(AppException.class, () -> parentService.deleteParent(1L, 1L, "admin"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.PARENT_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("부모의 전화번호와 등록된 학원으로 부모가 존재하는지 찾아오기")
    void checkExistByPhoneAndAcademy_success() {

        Parent parent1 = ParentFixtureUtil.PARENT1.init();

        when(parentRepository.existsByPhoneNum(any(String.class))).thenReturn(true);

        assertDoesNotThrow(() -> parentService.checkExistByPhoneAndAcademy(parent1.getPhoneNum()));
    }
}
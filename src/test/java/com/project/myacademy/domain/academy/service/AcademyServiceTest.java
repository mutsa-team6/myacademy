package com.project.myacademy.domain.academy.service;

import com.project.myacademy.domain.academy.dto.UpdateAcademyReqeust;
import com.project.myacademy.domain.academy.util.AcademyFixtureUtil;
import com.project.myacademy.domain.academy.dto.CreateAcademyRequest;
import com.project.myacademy.domain.academy.entity.Academy;
import com.project.myacademy.domain.academy.repository.AcademyRepository;
import com.project.myacademy.domain.academy.util.EmployeeFixtureUtil;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AcademyServiceTest {

    AcademyRepository academyRepository = mock(AcademyRepository.class);
    EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
    AcademyService academyService = new AcademyService(academyRepository, employeeRepository, bCryptPasswordEncoder);

    @Test
    @DisplayName("학원 생성 : 성공")
    void createAcademy() {
        final CreateAcademyRequest request = new CreateAcademyRequest(
                "name",
                "address",
                "phoneNum",
                "admin",
                "businessRegistrationNumber",
                "password"
        );
        final Academy academy = AcademyFixtureUtil.ACADEMY_ADMIN.init();

        when(academyRepository.findByBusinessRegistrationNumber(request.getBusinessRegistrationNumber())).thenReturn(Optional.empty());
        when(academyRepository.save(any(Academy.class))).thenReturn(academy);

        assertDoesNotThrow(() -> academyService.createAcademy(request));
        assertEquals("name", academyService.createAcademy(request).getName());
        assertEquals("admin", academyService.createAcademy(request).getOwner());

        verify(academyRepository, atLeastOnce()).findByBusinessRegistrationNumber(request.getBusinessRegistrationNumber());
        verify(academyRepository, atLeastOnce()).save(any(Academy.class));
    }

    @Test
    @DisplayName("학원 정보 수정 : 성공")
    void updateAcademy() {
        final Employee employee = EmployeeFixtureUtil.ROLE_ADMIN.init();
        final UpdateAcademyReqeust request = new UpdateAcademyReqeust(
                "name",
                "address",
                "phoneNum",
                "admin",
                "businessRegistrationNumber",
                "password"
        );
        final Academy academy = AcademyFixtureUtil.ACADEMY_ADMIN.init();

        when(employeeRepository.findByName(anyString())).thenReturn(Optional.of(employee));
        when(academyRepository.findById(anyLong())).thenReturn(Optional.of(academy));
        when(academyRepository.save(any(Academy.class))).thenReturn(academy);

        assertDoesNotThrow(() -> academyService.updateAcademy(1L, request, "admin"));
        assertEquals("admin", academyService.updateAcademy(1L, request, "admin").getOwner());

        verify(employeeRepository, atLeastOnce()).findByName(anyString());
        verify(academyRepository, atLeastOnce()).findById(anyLong());
        verify(academyRepository, atLeastOnce()).save(any(Academy.class));
    }

    @Test
    @DisplayName("학원 정보 삭제 : 성공")
    void deleteAcademy() {
        final Employee employee = EmployeeFixtureUtil.ROLE_ADMIN.init();
        final Academy academy = AcademyFixtureUtil.ACADEMY_ADMIN.init();

        when(employeeRepository.findByName(anyString())).thenReturn(Optional.of(employee));
        when(academyRepository.findById(anyLong())).thenReturn(Optional.of(academy));

        assertDoesNotThrow(() -> academyService.deleteAcademy(1L, "admin"));
        assertEquals(1L, academyService.deleteAcademy(1L, "admin"));

        verify(employeeRepository, atLeastOnce()).findByName(anyString());
        verify(academyRepository, atLeastOnce()).findById(anyLong());
    }
}
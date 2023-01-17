package com.project.myacademy.domain.academy.service;

import com.project.myacademy.domain.academy.dto.CreateAcademyRequest;
import com.project.myacademy.domain.academy.entity.Academy;
import com.project.myacademy.domain.academy.repository.AcademyRepository;
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
        final Academy academy = new Academy(
                1L,
                "name",
                "address",
                "phoneNum",
                "admin",
                "businessRegistrationNumber",
                "password"
        );

        when(academyRepository.findByBusinessRegistrationNumber(request.getBusinessRegistrationNumber())).thenReturn(Optional.empty());
        when(academyRepository.save(any(Academy.class))).thenReturn(academy);

        assertDoesNotThrow(() -> academyService.createAcademy(request));
        assertEquals("name", academyService.createAcademy(request).getName());
        assertEquals("admin", academyService.createAcademy(request).getOwner());

        verify(academyRepository, atLeastOnce()).findByBusinessRegistrationNumber(request.getBusinessRegistrationNumber());
        verify(academyRepository, atLeastOnce()).save(any(Academy.class));
    }
}
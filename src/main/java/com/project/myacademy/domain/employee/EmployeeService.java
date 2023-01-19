package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.employee.dto.*;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.global.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final EmployeeRole DEFAULT_EMPLOYEE_ROLE = EmployeeRole.ROLE_USER;

    @Value("${jwt.token.secret}")
    private String secretKey;
    private long expiredTimeMs = 1000 * 60 * 60;

    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeRequest request) {
        String account = request.getAccount();
        employeeRepository.findByAccount(account)
                .ifPresent(employee -> {
                    throw new AppException(ErrorCode.DUPLICATED_ACCOUNT, ErrorCode.DUPLICATED_ACCOUNT.getMessage());
                });
        String encryptedPassword = bCryptPasswordEncoder.encode(request.getPassword());
        Employee savedEmployee = employeeRepository.save(request.toEmployee(account, encryptedPassword, DEFAULT_EMPLOYEE_ROLE));
        return savedEmployee.toEmployeeDto();
    }

    public LoginEmployeeResponse loginEmployee(LoginEmployeeRequest request) {
        String account = request.getAccount();
        String password = request.getPassword();
        Employee foundEmployee = employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));
        if (!bCryptPasswordEncoder.matches(password, foundEmployee.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD, ErrorCode.INVALID_PASSWORD.getMessage());
        }
        return new LoginEmployeeResponse(JwtTokenUtil.createToken(account, secretKey, expiredTimeMs), "login succeeded");
    }

    public FindAccountEmployeeResponse findAccountEmployee(FindAccountEmployeeRequest request) {
        String name = request.getName();
        String email = request.getEmail();
        Employee foundEmployee = employeeRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));
        String account = foundEmployee.getAccount();
        return new FindAccountEmployeeResponse(account, "Account found : " + account);
    }

    /**
     * requestdto에 직원 계정, 이메일, 변경하고싶은 비밀번호
     * 직원 계정으로 db에 있는지 확인 -> 없으면 에러처리
     * 직원계정 + 이메일 2개가 동시에 일치하는 데이터가 있는지? -> 없으면 에러처리
     */
    @Transactional
    public EmployeeDto changePasswordEmployee(ChangePasswordEmployeeRequest request) {
        String account = request.getAccount();
        Employee foundEmployee = employeeRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));

        employeeRepository.findByAccount(account)
                .ifPresent(employee -> {
                    throw new AppException(ErrorCode.DUPLICATED_ACCOUNT, ErrorCode.DUPLICATED_ACCOUNT.getMessage());
                });

        foundEmployee.update(request);
        Employee updatedEmployee = employeeRepository.save(foundEmployee);
        return updatedEmployee.toEmployeeDto();
    }

    /**
     * ADMIN 혹은 STAFF 계정은 ADMIN을 제외한 다른 계정을 삭제할 수 있다.
     * 자기 자신을 삭제 요청할 시, 에러 처리 ( 본인 탈퇴 기능은 따로 구현 )
     * ADMIN 계정을 삭제하려고 할 시, 에러 처리
     * @param requestAccount 삭제 요청한 직원 계정
     * @param employeeId 삭제를 할 직원 기본키 id
     * @return
     */
    @Transactional
    public DeleteEmployeeResponse deleteEmployee(String requestAccount, Long employeeId) {

        // 삭제하려는 계정이 존재하지 않으면 에러 처리
        Employee foundEmployee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));

        // 삭제하려는 계정이 자기 자신인 경우 에러 처리
        if (foundEmployee.getAccount().equals(requestAccount)) {
            throw new AppException(ErrorCode.BAD_DELETE_REQUEST, ErrorCode.BAD_DELETE_REQUEST.getMessage());
        }

        EmployeeRole foundEmployeeRole = foundEmployee.getEmployeeRole();
        log.info(" ❌ 삭제가 될 사용자 계정 [{}] || 삭제가 될 사용자 등급 [{}]", foundEmployee.getAccount(), foundEmployeeRole);

        // 삭제하려는 계정이 ADMIN 인 경우 에러처리
        if (foundEmployeeRole.equals(EmployeeRole.ROLE_ADMIN)) {
            throw new AppException(ErrorCode.NOT_ALLOWED_CHANGE, ErrorCode.NOT_ALLOWED_CHANGE.getMessage());
        }

        employeeRepository.delete(foundEmployee);

        return new DeleteEmployeeResponse(employeeId, foundEmployee.getAccount() + " 계정이 삭제되었습니다. ");
    }


    public ReadEmployeeResponse readEmployee(Long employeeId) {
        Employee foundEmployee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));
        return new ReadEmployeeResponse(foundEmployee);
    }

    /**
     * JwtTokenFilter 에서 사용하기 위해 만든 메서드 ( 계정 찾아와서 권한 부여하기 위함 )
     */
    public EmployeeRole findRoleByAccount(String account) {
        return employeeRepository.findByAccount(account).get().getEmployeeRole();
    }

    /**
     * 관리자(ADMIN)는 모든 회원 정보를 조회할 수 있다.
     *
     * @param requestAccount 조회를 요청한 사용자 계정
     * @param pageable
     * @return 모든 회원 목록 반환
     */
    public Page<ReadEmployeeResponse> readAllEmployees(String requestAccount, Pageable pageable) {

        // 요청한 회원이 존재하지 않는 경우 에러 처리
        employeeRepository.findByAccount(requestAccount)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));


        return employeeRepository.findAll(pageable).map(employee -> new ReadEmployeeResponse(employee));
    }

    /**
     * 관리자(ADMIN) 혹은 직원(STAFF) 등급은 다른 직원의 등급을 USER -> STAFF 혹은 STAFF -> USER 로 변경할 수 있다.
     * 변경하려는 계정이 ADMIN 인 경우는 에러 처리
     *
     * @param requestAccount 등급 변경을 요청한 직원의 계정
     * @param employeeId     등급 변경이 될 직원의 기본키(id)
     * @return
     */
    @Transactional
    public ChangeRoleEmployeeResponse changeRoleEmployee(String requestAccount, Long employeeId) {

        // 요청한 직원이 존재하지 않는 경우 에러 처리
        employeeRepository.findByAccount(requestAccount)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));

        // 등급을 변경하려는 직원이 존재하지 않는 경우 에러 처리
        Employee foundEmployee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));

        // 등급을 변경하려는 직원의 변경하기 전 등급
        EmployeeRole foundEmployeeRole = foundEmployee.getEmployeeRole();
        log.info("🛠 등급 변경이 변경될 사용자 계정 [{}] || 현재 등급 [{}] ", foundEmployee.getAccount(), foundEmployeeRole);


        EmployeeRole changedRole = EmployeeRole.ROLE_STAFF;

        // USER 등급인 회원인 경우 STAFF로 바꿔준다.
        if (foundEmployeeRole.equals(EmployeeRole.ROLE_USER)) {
            foundEmployee.changeRole(changedRole);

            // STAFF 등급인 회원인 경우 USER로 바꿔준다.
        } else if (foundEmployeeRole.equals(EmployeeRole.ROLE_STAFF)) {
            changedRole = EmployeeRole.ROLE_USER;
            foundEmployee.changeRole(changedRole);

            // ADMIN 등급인 회원을 변경하려는 경우 권한 없음 에러처리한다.
        } else {
            throw new AppException(ErrorCode.NOT_ALLOWED_CHANGE, ErrorCode.NOT_ALLOWED_CHANGE.getMessage());
        }

        return new ChangeRoleEmployeeResponse(employeeId, foundEmployee.getAccount() + " 계정의 권한을 " + changedRole + "로 변경했습니다");

    }
}

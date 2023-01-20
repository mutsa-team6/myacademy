package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.academy.AcademyService;
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

    private final AcademyRepository academyRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final EmployeeRole DEFAULT_EMPLOYEE_ROLE = EmployeeRole.ROLE_USER;

    @Value("${jwt.token.secret}")
    private String secretKey;
    private long expiredTimeMs = 1000 * 60 * 60;

    /**
     * 학원이 존재하지 않는 경우 예외 처리
     * 가입 요청한 계정명이 이미 그 학원에 존재하는 경우 예외 처리
     * 계정명이 admin이고 학원 대표자명과 회원가입을 요청한 실명이 동일하면 USER_ADMIN 권한을 준다.
     * 계정명이 admin이지만, 학원 대표자명과 일치 하지 않는 경우 예외 처리
     * 그 외 일반적인 경우는 ROLE_USER 권한을 준다.
     *
     * @param request   회원가입을 요청한 사용자의 정보
     * @param academyId 회원가입을 요청한 사용자의 학원
     * @return
     */
    @Transactional
    public CreateEmployeeResponse createEmployee(CreateEmployeeRequest request, Long academyId) {

        //학원이 존재하지 않는 경우
        Academy foundAcademy = validateAcademy(academyId);


        String requestAccount = request.getAccount();

        // 가입 요청한 계정명이 이미 그 학원에 존재하는 경우 예외 처리
        employeeRepository.findByAccountAndAcademy(requestAccount, foundAcademy)
                .ifPresent(employee -> {
                    throw new AppException(ErrorCode.DUPLICATED_ACCOUNT);
                });

        // 계정명이 admin 이고 학원 대표자명과 회원가입을 요청한 실명이 동일하면 admin 계정을 준다.
        String requestRealName = request.getName();
        String ownerName = foundAcademy.getOwner();
        log.info("⭐ 회원가입 요청한 사용자의 실명 [{}] || 학원 대표자명 [{}]", requestRealName, ownerName);

        String encryptedPassword = bCryptPasswordEncoder.encode(request.getPassword());

        // 계정 이름을 admin으로 했지만, 대표자명과 일치하지 않는 경우 예외 처리
        if (requestAccount.equals("admin") && !requestRealName.equals(ownerName)) {
            throw new AppException(ErrorCode.NOT_MATCH_OWNER);
        }

        // 계정 이름도 admin이고 대표자명과 가입 요청한 사용자의 이름이 같은 경우 admin 권한 부여
        if (requestAccount.equals("admin") && requestRealName.equals(ownerName)) {

            Employee employee = Employee.builder()
                    .name(request.getName())
                    .employeeRole(EmployeeRole.ROLE_ADMIN)
                    .account("admin")
                    .phoneNum(request.getPhoneNum())
                    .email(request.getEmail())
                    .address(request.getAddress())
                    .academy(foundAcademy)
                    .password(encryptedPassword)
                    .build();
            Employee saved = employeeRepository.save(employee);
            return new CreateEmployeeResponse(saved, foundAcademy.getName());
        }

        //그 외는 일반 USER 등급 && 요청한 아이디로 가입
        Employee employee = Employee.builder()
                .name(request.getName())
                .employeeRole(DEFAULT_EMPLOYEE_ROLE)
                .account(requestAccount)
                .phoneNum(request.getPhoneNum())
                .email(request.getEmail())
                .address(request.getAddress())
                .academy(foundAcademy)
                .password(encryptedPassword)
                .build();

        Employee saved = employeeRepository.save(employee);

        return new CreateEmployeeResponse(saved, foundAcademy.getName());

    }

    /**
     * 학원이 존재하지 않는 경우 에러 처리
     * 로그인을 요청한 회원이 해당 학원에 존재하지 않는 경우 에러 처리
     * 입력한 비밀번호와 저장되어 있는 비밀번호가 다른 경우 예외 처리
     *
     * @param request   로그인을 요청한 사용자의 정보
     * @param academyId 로그인을 요청한 사용자의 학원 id
     * @return
     */
    public LoginEmployeeResponse loginEmployee(LoginEmployeeRequest request, Long academyId) {

        //학원이 존재하지 않는 경우
        Academy foundAcademy = validateAcademy(academyId);

        //로그인 요청한 계정
        String requestAccount = request.getAccount();

        // 로그인을 요청한 회원이 해당 학원에 존재하지 않는 경우 예외 처리
        Employee requestEmployee = validateRequestEmployee(requestAccount, foundAcademy);

        String password = request.getPassword();

        if (!bCryptPasswordEncoder.matches(password, requestEmployee.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        return new LoginEmployeeResponse(JwtTokenUtil.createToken(requestAccount, secretKey, expiredTimeMs), requestAccount + " 계정 로그인 성공");
    }

    // 이메일 인증 기능 완성 후 구현
    public FindAccountEmployeeResponse findAccountEmployee(FindAccountEmployeeRequest request) {
        String name = request.getName();
        String email = request.getEmail();
        Employee foundEmployee = employeeRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        String account = foundEmployee.getAccount();
        return new FindAccountEmployeeResponse(account, "Account found : " + account);
    }

    // 이메일 인증 기능 완성 후 구현

    /**
     * requestdto에 직원 계정, 이메일, 변경하고싶은 비밀번호
     * 직원 계정으로 db에 있는지 확인 -> 없으면 에러처리
     * 직원계정 + 이메일 2개가 동시에 일치하는 데이터가 있는지? -> 없으면 에러처리
     */
    @Transactional
    public EmployeeDto changePasswordEmployee(ChangePasswordEmployeeRequest request) {
        String account = request.getAccount();
        Employee foundEmployee = employeeRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employeeRepository.findByAccount(account)
                .ifPresent(employee -> {
                    throw new AppException(ErrorCode.DUPLICATED_ACCOUNT);
                });

//        foundEmployee.update(request);
        Employee updatedEmployee = employeeRepository.save(foundEmployee);
        return updatedEmployee.toEmployeeDto();
    }

    /**
     * ADMIN 혹은 STAFF 계정은 ADMIN을 제외한 다른 계정을 삭제할 수 있다.
     * 접근하려는 학원이 존재하지 않는 경우 에러 처리
     * 삭제를 요청한 계정이 해당 학원에 존재하지 않는 경우 에러 처리
     * 삭제해버릴 계정이 해당 학원에 존재하지 않는 경우 에러 처리
     * 자기 자신을 삭제 요청할 시, 에러 처리 ( 본인 탈퇴 기능은 따로 구현 )
     * ADMIN 계정을 삭제하려고 할 시, 에러 처리
     * USER 가 삭제하려고하는 경우는 security로 에러 처리
     *
     * @param requestAccount 삭제 요청한 직원 계정
     * @param employeeId     삭제를 할 직원 기본키 id
     * @return
     */
    @Transactional
    public DeleteEmployeeResponse deleteEmployee(String requestAccount, Long academyId, Long employeeId) {

        //학원이 존재하지 않는 경우
        Academy foundAcademy = validateAcademy(academyId);

        // 삭제를 요청한 계정이 해당 학원에 존재하지 않은 경우 에러 처리
        Employee requestEmployee = validateRequestEmployee(requestAccount, foundAcademy);

        // 삭제하려는 계정이 해당 학원에 존재하지 않으면 에러 처리
        Employee foundEmployee = validateEmployee(employeeId, foundAcademy);


        // 삭제하려는 계정이 자기 자신인 경우 에러 처리
        if (foundEmployee.getAccount().equals(requestAccount)) {
            throw new AppException(ErrorCode.BAD_DELETE_REQUEST);
        }

        EmployeeRole foundEmployeeRole = foundEmployee.getEmployeeRole();
        log.info(" ❌ 삭제가 될 사용자 계정 [{}] || 삭제가 될 사용자 등급 [{}]", foundEmployee.getAccount(), foundEmployeeRole);

        // 삭제하려는 계정이 ADMIN 인 경우 에러처리
        if (foundEmployeeRole.equals(EmployeeRole.ROLE_ADMIN)) {
            throw new AppException(ErrorCode.NOT_ALLOWED_CHANGE);
        }

        employeeRepository.delete(foundEmployee);

        return new DeleteEmployeeResponse(employeeId, foundEmployee.getAccount() + " 계정이 삭제되었습니다. ");
    }

    /**
     * 본인 인적사항은 jwt 토큰으로 추출하기 때문에, 다른 사람이 접근할 수 없음
     *
     * @param academyId      학원 기본키
     * @param requestAccount 본인 인적사항을 확인할 계정
     * @return
     */

    public ReadEmployeeResponse readEmployee(Long academyId, String requestAccount) {

        //학원이 존재하는지 확인
        Academy foundAcademy = validateAcademy(academyId);

        //마이페이지 조회를 요청한 회원이 해당 학원에 존재하는지 확인
        Employee RequestEmployee = validateRequestEmployee(requestAccount, foundAcademy);


        return new ReadEmployeeResponse(RequestEmployee);
    }

    /**
     * JwtTokenFilter 에서 사용하기 위해 만든 메서드 ( 계정 찾아와서 권한 부여하기 위함 )
     */
    public EmployeeRole findRoleByAccount(String account) {
        return employeeRepository.findByAccount(account).get().getEmployeeRole();
    }

    /**
     * 관리자(ADMIN)는 모든 회원 정보를 조회할 수 있다.
     * 정보를 조회하려는 학원이 존재하지 않는 경우 에러 처리
     * 조회를 요청한 회원이 해당 학원에 존재하지 않는 경우 에러 처리
     * ADMIN 이 아니면 접근할 수 없는 에러처리는 security 단 에서 진행
     *
     * @param requestAccount 조회를 요청한 사용자 계정
     * @param pageable
     * @return 모든 회원 목록 반환
     */
    public Page<ReadEmployeeResponse> readAllEmployees(String requestAccount, Long academyId, Pageable pageable) {

        //학원이 존재하지 않는 경우
        Academy foundAcademy = validateAcademy(academyId);


        // 조회를 요청한 회원이 해당 학원에 존재하지 않는 경우 에러 처리
        validateRequestEmployee(requestAccount, foundAcademy);


        return employeeRepository.findAll(pageable).map(employee -> new ReadEmployeeResponse(employee));
    }

    /**
     * 관리자(ADMIN) 혹은 직원(STAFF) 등급은 다른 직원의 등급을 USER -> STAFF 혹은 STAFF -> USER 로 변경할 수 있다.
     * 접근하려는 학원이 존재하지 않는 경우 에러 처리
     * 등급 수정을 요청한 계정이 해당 학원에 존재하지 않는 경우 에러 처리
     * 수정할 계정이 해당 학원에 존재하지 않는 경우 에러 처리
     * 수정할 계정이 ADMIN 인 경우는 에러 처리
     * 본인 계정을 변경하려고 요청하면 에러 처리
     * USER 접근 제어는 Security 단에서 처리
     *
     * @param requestAccount 등급 변경을 요청한 직원의 계정
     * @param employeeId     등급 변경이 될 직원의 기본키(id)
     * @return
     */
    @Transactional
    public ChangeRoleEmployeeResponse changeRoleEmployee(String requestAccount, Long academyId, Long employeeId) {

        //학원이 존재하지 않는 경우
        Academy foundAcademy = validateAcademy(academyId);

        // 등급 수정을 요청한 계정이 해당 학원에 존재하지 않은 경우 에러 처리
        Employee requestEmployee = validateRequestEmployee(requestAccount, foundAcademy);

        // 수정하려는 계정이 해당 학원에 존재하지 않으면 에러 처리
        Employee foundEmployee = validateEmployee(employeeId, foundAcademy);

        // 변경하려는 계정이 자기 자신인 경우 에러 처리
        if (foundEmployee.getAccount().equals(requestAccount)) {
            throw new AppException(ErrorCode.BAD_CHANGE_REQUEST);
        }

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
            throw new AppException(ErrorCode.NOT_ALLOWED_CHANGE);
        }

        return new ChangeRoleEmployeeResponse(employeeId, foundEmployee.getAccount() + " 계정의 권한을 " + changedRole + "로 변경했습니다");

    }

    /**
     * ADMIN 회원은 본인 탈퇴 불가
     *
     * @param requestAccount 탈퇴 요청한 계정명
     * @param academyId
     * @return
     */
    @Transactional
    public DeleteEmployeeResponse selfDeleteEmployee(String requestAccount, Long academyId) {

        //해당 학원이 존재하는지 확인
        Academy foundAcademy = validateAcademy(academyId);

        // 본인 탈퇴를 요청한 회원이 해당 학원에 존재하는지 확인
        Employee requestEmployee = validateRequestEmployee(requestAccount, foundAcademy);

        EmployeeRole requestEmployeeRole = requestEmployee.getEmployeeRole();
        log.info(" ❌ 본인 탈퇴를 요청한 사용자 권한 [{}] ", requestEmployeeRole);

        // ADMIN 계정은 본인 탈퇴 불가
        if (requestEmployeeRole.equals(EmployeeRole.ROLE_ADMIN)) {
            throw new AppException(ErrorCode.NOT_ALLOWED_CHANGE);
        }

        employeeRepository.delete(requestEmployee);

        return new DeleteEmployeeResponse(requestEmployee.getId(), requestAccount + " 계정이 삭제되었습니다. ");

    }

    /**
     * 계정명, 등급은 본인이 변경 불가
     *
     * @param requestAccount
     * @param academyId
     * @return
     */
    @Transactional
    public UpdateEmployeeResponse updateEmployee(UpdateEmployeeRequest request, String requestAccount, Long academyId) {

        //해당 학원이 존재하는지 확인
        Academy foundAcademy = validateAcademy(academyId);

        // 본인 정보 수정을 요청한 회원이 해당 학원에 존재하는지 확인
        Employee requestEmployee = validateRequestEmployee(requestAccount, foundAcademy);

        //정보 수정
        requestEmployee.update(request);

        return new UpdateEmployeeResponse(requestEmployee.getId(), requestAccount + "계정 정보를 수정했습니다");
    }

    // 접근하려는 학원이 존재하는지 확인
    private Academy validateAcademy(Long academyId) {
        Academy validateAcademy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
        return validateAcademy;
    }

    // 특정 요청을 한 회원이 특정 요청이 적용될 학원에 존재하지 않는 경우 예외 처리 (다른 학원 직원이라는 의미)
    private Employee validateRequestEmployee(String requestAccount, Academy academy) {

        Employee validateRequestEmployee = employeeRepository.findByAccountAndAcademy(requestAccount, academy)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND));

        return validateRequestEmployee;
    }

    // 특정 요청이 적용될 회원이 학원에 존재하지 않는 경우 예외 처리

    private Employee validateEmployee(Long employeeId, Academy academy) {

        Employee validateEmployee = employeeRepository.findByIdAndAcademy(employeeId, academy)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        return validateEmployee;
    }


}

package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.dto.*;
import com.project.myacademy.global.configuration.refreshToken.RefreshToken;
import com.project.myacademy.global.configuration.refreshToken.RefreshTokenRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.global.util.EmailUtil;
import com.project.myacademy.global.util.JwtTokenUtil;
import com.querydsl.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AcademyRepository academyRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailUtil emailUtil;

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.token.secret}")
    private String secretKey;
    private long expiredTimeMs = 1000 * 60 * 30;

    /**
     * 직원 등록
     *
     * @param request   회원가입을 요청한 사용자의 정보
     * @param academyId 회원가입을 요청한 사용자의 학원
     */
    @Transactional
    public CreateEmployeeResponse createEmployee(CreateEmployeeRequest request, Long academyId) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy foundAcademy = validateAcademyById(academyId);

        String requestAccount = request.getAccount();
        String requestEmail = request.getEmail();
        String requestRealName = request.getName();
        log.info("⭐ 회원가입 요청한 사용자의 계정 [{}] || 이메일 [{}]", requestAccount, requestEmail);

        // 가입을 요청한 계정과 학원으로 직원을 조회 - 있을시 DUPLICATED_ACCOUNT 에러발생
        ifPresentAccountInAcademy(requestAccount, foundAcademy);
        // 이메일로 직원을 조회 - 있을시 DUPLICATED_EMAIL 에러발생
        ifPresentEmailInEmployee(requestEmail);

        // 계정명이 admin 이고 학원 대표자명과 회원가입을 요청한 실명이 동일하면 admin 계정을 준다.
        String ownerName = foundAcademy.getOwner();
        log.info("⭐ 회원가입 요청한 사용자의 실명 [{}] || 학원 대표자명 [{}]", requestRealName, ownerName);

        String encryptedPassword = bCryptPasswordEncoder.encode(request.getPassword());

        // 계정 이름을 admin으로 했지만, 대표자명과 일치하지 않는 경우 예외 처리
        if (requestAccount.equals("admin") && !requestRealName.equals(ownerName)) {
            throw new AppException(ErrorCode.NOT_MATCH_OWNER);
        }

        String requestEmployeeType = request.getEmployeeType();
        String requestSubject = request.getSubject();
        log.info("⭐ 회원가입 요청할 때 체크한 직원 유형 [{}] || 과목 명 [{}]", requestEmployeeType, requestSubject);

        if (requestEmployeeType.equals("0")) {
            throw new AppException(ErrorCode.EMPTY_EMPLOYEE_TYPE);
        }

        // 계정 이름도 admin이고 대표자명과 가입 요청한 사용자의 이름이 같은 경우 admin 권한 부여
        if (requestAccount.equals("admin") && requestRealName.equals(ownerName)) {

            // 원장으로 체크했는데, 과목명 입력 안했을 시, 예외 처리
            if (StringUtils.isNullOrEmpty(requestSubject)) {
                throw new AppException(ErrorCode.EMPTY_SUBJECT_FORBIDDEN);
            }

            //ADMIN 권한의 Employee 객체 생성
            Employee AdminEmployee = Employee.createAdminEmployee(request, foundAcademy, encryptedPassword);

            Employee saved = employeeRepository.save(AdminEmployee);
            return new CreateEmployeeResponse(saved, foundAcademy.getName());
        }
        //그 외는 일반 USER 등급 && 요청한 아이디로 가입

        // 강사로 체크한 경우 (USER)
        if (requestEmployeeType.equals("USER")) {
            // 선생으로 체크했는데, 과목명 입력 안했을 시, 예외 처리
            if (StringUtils.isNullOrEmpty(requestSubject)) {
                throw new AppException(ErrorCode.EMPTY_SUBJECT_FORBIDDEN);
            }
            //USER 권한의 Employee 객체 생성
            Employee UserEmployee = Employee.createUserEmployee(request, foundAcademy, encryptedPassword);

            Employee saved = employeeRepository.save(UserEmployee);
            return new CreateEmployeeResponse(saved, foundAcademy.getName());
        }

        //STAFF 권한의 Employee 객체 생성
        Employee staffEmployee = Employee.createStaffEmployee(request, foundAcademy, encryptedPassword);

        Employee saved = employeeRepository.save(staffEmployee);
        return new CreateEmployeeResponse(saved, foundAcademy.getName());
    }

    /**
     * 로그인 기능
     *
     * @param request   로그인을 요청한 사용자의 정보
     * @param academyId 로그인을 요청한 사용자의 학원 id
     */
    public LoginEmployeeResponse loginEmployee(LoginEmployeeRequest request, Long academyId) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy foundAcademy = validateAcademyById(academyId);

        //로그인 요청한 계정
        String requestAccount = request.getAccount();

        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee requestEmployee = validateRequestEmployeeByAccount(requestAccount, foundAcademy);

        String password = request.getPassword();

        if (!bCryptPasswordEncoder.matches(password, requestEmployee.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        //리프레시 토큰은 난수로 생성, access 토큰은, 사용자 정보로 생성
        String accessToken = JwtTokenUtil.createToken(requestAccount, requestEmployee.getEmail(), secretKey);
        String refreshToken = JwtTokenUtil.createRefreshToken(secretKey);

        //레디스에 저장 Refresh 토큰을 저장한다. (사용자 기본키 Id, refresh 토큰, access 토큰 저장)
        refreshTokenRepository.save(new RefreshToken(String.valueOf(requestEmployee.getId()), refreshToken, accessToken));

        return new LoginEmployeeResponse(accessToken, requestEmployee.getName());
    }

    /**
     * 직원 계정 찾기
     *
     * @param request 계정을 찾을 계정의 이메일과 이름
     */
    public FindAccountEmployeeResponse findAccountEmployee(FindAccountEmployeeRequest request) {

        String requestEmployeeName = request.getName();
        String requestEmployeeEmail = request.getEmail();

        log.info("🔎 아이디 찾기를 요청한 사용자 실명 [{}]  || 사용자 이메일 [{}] ", requestEmployeeName, requestEmployeeEmail);

        // 이메일로 직원을 조회 - 없을시 EMPLOYEE_NOT_FOUND 에러발생
        Employee foundEmployee = validateEmployeeByEmail(requestEmployeeEmail);

        String account = foundEmployee.getAccount();

        log.info("🔎 찾은 계정 [{}] ", account);

        return new FindAccountEmployeeResponse(foundEmployee.getId(), account);
    }

    /**
     * 직원 비밀번호 찾기
     *
     * @param request 비밀번호를 찾을 직원의 이름, 계정, 이메일
     */
    @Transactional
    public FindPasswordEmployeeResponse findPasswordEmployee(FindPasswordEmployeeRequest request) {

        String name = request.getName();
        String email = request.getEmail();

        // 이메일로 직원을 조회 - 없을시 EMAIL_NOT_FOUND 에러발생
        Employee foundEmployee = validateEmployeeByEmail(email);

        String tempPassword = getTempPassword();
        String encodedTempPassword = bCryptPasswordEncoder.encode(tempPassword);

        foundEmployee.updatePasswordOnly(encodedTempPassword);

        Employee changedEmployee = employeeRepository.save(foundEmployee);

        String title = String.format("%s님의 임시 비밀번호 안내 메일입니다.", name);
        String body = String.format("안녕하세요.%n%nMyAcademy 임시 비밀번호 안내 관련 메일입니다.%n%n%s님의 임시 비밀번호는 %s입니다.%n%n발급된 임시 비밀번호로 로그인해서 새 비밀번호로 변경 후 이용바랍니다.%n%n감사합니다.", name, tempPassword);


        try {
            emailUtil.sendEmail(email, title, body);
        }catch (MessagingException e) {
            log.info("이메일 전송 에러 발생 [{}]", e.getMessage());

        }

        return FindPasswordEmployeeResponse.of(changedEmployee);
    }

    /**
     * 직원 비밀번호 변경 (Old/New 비밀번호를 입력받아 변경해줌)
     *
     * @param request   기존, 새로운 비밀번호가 담긴 request
     * @param academyId 학원 id
     * @param account   jwt에 담긴 직원 account
     */
    @Transactional
    public ChangePasswordEmployeeResponse changePasswordEmployee(ChangePasswordEmployeeRequest request, Long academyId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);

        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAccount(account, academy);

        //request에 담긴 기존 패스워드가 employee에 저장되어있는 패스워드와 다르면 에러발생
        if (!bCryptPasswordEncoder.matches(request.getOldPassword(), employee.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        // 새로운 패스워드와 기존 패스워드와 같으면 에러발생
        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new AppException(ErrorCode.SAME_PASSWORD);
        }

        String encodedNewPassword = bCryptPasswordEncoder.encode(request.getNewPassword());

        employee.updatePasswordOnly(encodedNewPassword);

        return new ChangePasswordEmployeeResponse(employee.getAccount(), "%n 님의 비밀번호 변경을 성공했습니다.");
    }

    /**
     * 직원 삭제
     *
     * @param requestAccount 삭제 요청한 직원 계정
     * @param employeeId     삭제를 할 직원 기본키 id
     */
    @Transactional
    public DeleteEmployeeResponse deleteEmployee(String requestAccount, Long academyId, Long employeeId) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy foundAcademy = validateAcademyById(academyId);

        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee requestEmployee = validateRequestEmployeeByAccount(requestAccount, foundAcademy);

        // 적용될 계정과 학원으로 직원을 조회 - 없을시 ACCOUNT_NOT_FOUND 에러발생
        Employee foundEmployee = validateEmployeeById(employeeId, foundAcademy);

        // 삭제 요청자 권한이 ADMIN 아니면 - INVALID_PERMISSION 에러발생
        if (!requestEmployee.getEmployeeRole().equals(EmployeeRole.ROLE_ADMIN)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 삭제하려는 계정이 자기 자신인 경우 - BAD_DELETE_REQUEST 에러발생
        if (foundEmployee.getAccount().equals(requestAccount)) {
            throw new AppException(ErrorCode.BAD_DELETE_REQUEST);
        }

        EmployeeRole foundEmployeeRole = foundEmployee.getEmployeeRole();
        log.info(" ❌ 삭제가 될 사용자 계정 [{}] || 삭제가 될 사용자 등급 [{}]", foundEmployee.getAccount(), foundEmployeeRole);

        employeeRepository.delete(foundEmployee);

        return new DeleteEmployeeResponse(employeeId, foundEmployee.getAccount() + " 계정이 삭제되었습니다. ");
    }

    /**
     * 본인 인적사항은 jwt 토큰으로 추출하기 때문에, 다른 사람이 접근할 수 없음
     *
     * @param academyId      학원 id
     * @param requestAccount 본인 인적사항을 확인할 계정
     */
    public ReadEmployeeResponse readEmployee(Long academyId, String requestAccount) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy foundAcademy = validateAcademyById(academyId);

        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee RequestEmployee = validateRequestEmployeeByAccount(requestAccount, foundAcademy);

        return new ReadEmployeeResponse(RequestEmployee);
    }

    /**
     * JwtTokenFilter 에서 사용하기 위해 만든 메서드 ( 계정 찾아와서 권한 부여하기 위함 )
     */
    public Employee findByEmail(String email) {

        return validateEmployeeByEmail(email);
    }

    /**
     * 관리자가 모든 직원 정보를 조회
     *
     * @param requestAccount 조회를 요청한 사용자 계정
     */
    public Page<ReadAllEmployeeResponse> readAllEmployees(String requestAccount, Long academyId, Pageable pageable) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy foundAcademy = validateAcademyById(academyId);

        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employeeAdmin = validateRequestEmployeeByAccount(requestAccount, foundAcademy);

        // 조회를 요청한 회원의 권한이 admin 이 아닐경우 - NOT_ALLOWED_ROLE 에러발생
        if (!employeeAdmin.getEmployeeRole().equals(EmployeeRole.ROLE_ADMIN)) {
            throw new AppException(ErrorCode.NOT_ALLOWED_ROLE);
        }

        return employeeRepository.findAllEmployee(foundAcademy, pageable).map(ReadAllEmployeeResponse::of);
    }


    /**
     * ADMIN 회원은 본인 탈퇴 불가
     *
     * @param requestAccount 탈퇴 요청한 계정명
     * @param academyId      학원 Id
     */
    @Transactional
    public DeleteEmployeeResponse selfDeleteEmployee(String requestAccount, Long academyId) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy foundAcademy = validateAcademyById(academyId);

        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee requestEmployee = validateRequestEmployeeByAccount(requestAccount, foundAcademy);

        EmployeeRole requestEmployeeRole = requestEmployee.getEmployeeRole();
        log.info(" ❌ 본인 탈퇴를 요청한 사용자 권한 [{}] ", requestEmployeeRole);

        // ADMIN 계정이 탈퇴를 시도 할 경우 - NOT_ALLOWED_CHANGE 에러발생
        if (requestEmployeeRole.equals(EmployeeRole.ROLE_ADMIN)) {
            throw new AppException(ErrorCode.NOT_ALLOWED_CHANGE);
        }

        employeeRepository.delete(requestEmployee);

        return new DeleteEmployeeResponse(requestEmployee.getId(), requestAccount + " 계정이 삭제되었습니다. ");

    }

    /**
     * 직원 정보 변경
     * 계정명, 등급은 본인이 변경 불가
     *
     * @param requestAccount 정보변경을 요청한 직원 계정
     * @param academyId      학원 Id
     */
    @Transactional
    public UpdateEmployeeResponse updateEmployee(UpdateEmployeeRequest request, String requestAccount, Long academyId) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy foundAcademy = validateAcademyById(academyId);

        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee requestEmployee = validateRequestEmployeeByAccount(requestAccount, foundAcademy);

        //정보 수정
        requestEmployee.updateEmployeeInfo(request);

        return new UpdateEmployeeResponse(requestEmployee.getId(), requestAccount + "계정 정보를 수정했습니다");
    }

    /**
     * UI 용 메서드
     * 회원가입한 사용자 들 중에서, 특정 학원의 강사들만 추출하는 메서드
     */
    public Page<ReadEmployeeResponse> findAllTeachers(String requestAccount, Long academyId, Pageable pageable) {
        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy foundAcademy = validateAcademyById(academyId);

        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee requestEmployee = validateRequestEmployeeByAccount(requestAccount, foundAcademy);
        return employeeRepository.findAllTeacher(foundAcademy, pageable).map(employee -> new ReadEmployeeResponse(employee));
    }

    /**
     * UI 용 메서드
     * 강좌 등록 시에 강사 정보를 보여주기 위함
     */
    public ReadEmployeeResponse findOneTeacher(String requestAccount, Long academyId, Long teacherId) {
        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy foundAcademy = validateAcademyById(academyId);

        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee requestEmployee = validateRequestEmployeeByAccount(requestAccount, foundAcademy);

        // 해당 강사가 해당 학원에 존재하는지 확인
        Employee foundTeacher = validateEmployeeById(teacherId, foundAcademy);

        // 강사가 맞는지 체크 - 아니면 NOT_TEACHER 에러발생
        if (foundTeacher.getEmployeeRole().equals(EmployeeRole.ROLE_STAFF)) {
            throw new AppException(ErrorCode.NOT_TEACHER);
        }

        ReadEmployeeResponse response = new ReadEmployeeResponse(foundTeacher);

        return response;
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

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy foundAcademy = validateAcademyById(academyId);

        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        validateRequestEmployeeByAccount(requestAccount, foundAcademy);

        // 적용될 계정과 학원으로 직원을 조회 - 없을시 ACCOUNT_NOT_FOUND 에러발생
        Employee foundEmployee = validateEmployeeById(employeeId, foundAcademy);

        // 변경하려는 계정이 자기 자신인 경우 - BAD_CHANGE_REQUEST 에러발생
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
     * 학원 별, 직원 수 구하는 메서드 (UI 용)
     */

    public Long countEmployeesByAcademy(Long academyId) {
        Academy academy = validateAcademyById(academyId);
        return employeeRepository.countByAcademy(academy);
    }

    // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
    private Academy validateAcademyById(Long academyId) {
        Academy validateAcademy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
        return validateAcademy;
    }

    // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
    private Employee validateRequestEmployeeByAccount(String requestAccount, Academy academy) {
        Employee validateRequestEmployee = employeeRepository.findByAccountAndAcademy(requestAccount, academy)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND));
        return validateRequestEmployee;
    }

    // 특정 요청이 적용될 Id와 학원으로 직원을 조회 - 없을시 EMPLOYEE_NOT_FOUND 에러발생
    private Employee validateEmployeeById(Long employeeId, Academy academy) {
        Employee validateEmployee = employeeRepository.findByIdAndAcademy(employeeId, academy)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        return validateEmployee;
    }

    // 계정이 등록된 계정인지 확인 - 없을시 NAME_NOT_FOUND 에러발생
    private void validateEmployeeByName(String name) {
        employeeRepository.findByName(name)
                .orElseThrow(() -> new AppException(ErrorCode.NAME_NOT_FOUND));
    }

    // 가입을 요청한 계정과 학원으로 직원을 조회 - 있을시 DUPLICATED_ACCOUNT 에러발생
    private void ifPresentAccountInAcademy(String requestAccount, Academy foundAcademy) {
        employeeRepository.findByAccountAndAcademy(requestAccount, foundAcademy)
                .ifPresent(employee -> {
                    throw new AppException(ErrorCode.DUPLICATED_ACCOUNT);
                });
    }

    // 이메일로 직원을 조회 - 없을시 EMPLOYEE_NOT_FOUND 에러발생
    private Employee validateEmployeeByEmail(String email) {
        Employee foundEmployee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        return foundEmployee;
    }

    // 이메일로 직원을 조회 - 있을시 DUPLICATED_EMAIL 에러발생
    private void ifPresentEmailInEmployee(String email) {
        employeeRepository.findByEmail(email)
                .ifPresent(employee -> {
                    throw new AppException(ErrorCode.DUPLICATED_EMAIL);
                });
    }

    public String getTempPassword() {
        char[] charSet = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

        String str = "";

        int idx = 0;
        for (int i = 0; i < 10; i++) {
            idx = (int) (charSet.length * Math.random());
            str += charSet[idx];
        }
        return str;
    }

}


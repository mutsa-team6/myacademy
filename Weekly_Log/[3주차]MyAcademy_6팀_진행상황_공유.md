# [3주차] MyAcademy팀 진행상황 공유

<br>

## 👨‍👩‍👧‍👦팀 구성원, 개인 별 역할

------

- 윤인규 (PM) : 프로젝트 일정 관리,  개발
- 권오석 (CTO) : ERD 제작, 코드 컨벤션 관리,  개발
- 이가현 (인프라) : GitLab CI/CD 파이프라인 구축, EC2 인스턴스 관리,  개발
- 박태근 (테스트) : 테스트 코드 환경 세팅,  개발
- 최승호 (기획) : 추가 기능 기획, UI 디자인 제안,  개발

<br>

## 🕐 팀 내부 회의 진행 회차 및 일자

------

### 1월 30일 월요일

파일 기능 사용처 생각해보기

- **공지사항**에 첨부파일 올리는거 → file이랑 연결되어있는 (공지사항 게시글id — 파일 id )
- 게시글 상세보기 → 게시글 내용, 어떤 파일첨부했는지 보여야함 → 누르면 다운 받을 수 있어야 함 → <a onclick="file.get()"> get() → 다운로드 겟요청
- **직원** → 프사

서브모듈

- 테스트 환경에서는 비공개 레포지토리에 환경변수를 담은 설정파일(yml)을 생성하여 테스트 프로젝트에 적용할 수 있었으나, 로컬환경에서 -Dspring.profiles.active={설정파일의 프로필}을 적용하는 것을 CI/CD과정에 어떻게 적용할 수 있을지에 대한 고민을 마주했다.

→ 인텔리제이에서 어떤 설정을 하면 깃허브 프라이빗 레포지토리에서 환경변수를 가져온다.

- 서브모듈하는 이유? (민감 정보 yml 파일에 작성안할 수 있고, 배포시 환경변수 넣는 작업 편해짐)
- 방법 찾아보기 → ci&cd 까지 다되는 방법 있어야함

→ 서브모듈 사용하려면 gitlab ci&cd 로 변경해야 어느정도 가능성이 보이는데 지금 상황에서는 필요한 기능구현에 집중하고, 여유 있을 때 적용 고민해보는 걸로 결정 땅땅!

erd 수정 사항

- teacher 테이블 사라짐 → teacher 테이블에 있던 subject 속성이 employee 테이블로 이동 → 일반 직원은 subject 속성에 `"직원"` 이 입력될 것, 그 외는 직접 입력한 과목 명이 들어감. → 직원(STAFF) 가 아닌 사용자는 마이페이지에서 과목 내용을 수정할 수 있음

## 1월 31일 화요일

- 권한별 접근 페이지 & 엔드포인트 정리하기 (일단 UI 중심으로 생각해보기)

### 1️⃣ 모든 권한 접근 가능

- `제일 처음 index화면` · `로그인` · `회원가입` · `학원등록 페이지` · `아이디 찾기` · `비밀번호 찾기`

**학원 관련**

POST : `/api/v1/academies`

DELETE : `/api/v1/academies/{academyId}/delete`

**직원관련**

POST : `/api/v1/academies/{academyId}/employees/signup`

POST : `/api/v1/academies/{academyId}/employees/login`

POST : `/api/v1/academies/employee/findAccount`

PUT: `/api/v1/academies/employee/findPassword`

### 2️⃣ USER · STAFF · ADMIN 가능 (authentication 인증된 사람)

- `메인 페이지` · `학생 & 부모 데이터 조회` · `공지사항 조회페이지` · `학생 특이사항 crud 가능`

**직원관련**

GET : `/api/v1/academies/{academyId}/my`

PUT : `/api/v1/academies/{academyId}`

DELETE : `/api/v1/academies/{academyId}`

**공지사항 관련**

GET : `/api/v1/academies/{academyId}/announcements/{announcementId}`

GET : `/api/v1/academies/{academyId}/announcements`

**부모 관련**

GET : `/api/v1/academies/{academyId}/parents/{parentId}`

**학생 관련**

GET : `/api/v1/academies/{academyId}/students/{studentId}`

**특이사항 관련**

GET : `/api/v1/academies/{academyId}/students/{studentId}/uniqueness`

POST : `/api/v1/academies/{academyId}/students/{studentId}/uniqueness`

PUT : `/api/v1/academies/{academyId}/students/{studentId}/uniqueness/{uniquenessId}`

DELETE : `/api/v1/academies/{academyId}/students/{studentId}/uniqueness/{uniquenessId}`

**강좌 관련**

GET : `/api/v1/academies/{academyId}/lectures`

**수강신청 관련**

GET : `/api/v1/academies/{academyId}/enrollments`

**대기번호 관련**

GET : `/api/v1/academies/{academyId}/waitinglists`

**할인정책 관련**

GET : `/api/v1/academies/{academyId}discounts`

GET : `/api/v1/academies/{academyId}/enrollments/{enrollmentId}/dicounts`

### 3️⃣ STAFF · ADMIN 가능

- `학생 & 부모 조회페이지에서 데이터 수정 삭제 버튼 가능` · `학생 등록 페이지 접속 가능`

**직원 관련**

PUT : `/api/v1/academies/{academyId}/changeRole/{employeeId}`

DELETE : `/api/v1/academies/{academyId}/employees/{employeeId}`

**공지사항 관련**

POST : `/api/v1/academies/{academyId}/announcements`

PUT : `/api/v1/academies/{academyId}/announcements/{announcementId}`

DELETE : `/api/v1/academies/{academyId}/announcements/{announcementId}`

**부모 관련**

POST : `/api/v1/academies/{academyId}/parents`

PUT : `/api/v1/academies/{academyId}/parents/{parentId}`

DELETE : `/api/v1/academies/{academyId}/parents/{parentId}`

**학생 관련**

POST : `/api/v1/academies/{academyId}/students/{studentId}`

PUT : `/api/v1/academies/{academyId}/students/{studentId}`

DELETE : `/api/v1/academies/{academyId}/students/{studentId}`

**강좌 관련**

POST : `/api/v1/academies/{academyId}/employees/{employeeId}/lectures`

PUT : `/api/v1/academies/{academyId}/lectures/{lectureId}`

DELETE : `/api/v1/academies/{academyId}/lectures/{lectureId}`

**수강신청 관련**

POST : `/api/v1/academies/{academyId}/students/{studentId}/lectures/{lectureId}/enrollments/{enrollmentId}`

POST : `/api/v1/academies/{academyId}/students/{studentId}/lectures/{lectureId}/enrollments`

PUT : `/api/v1/academies/{academyId}/students/{studentId}/lectures/{lectureId}/enrollments/{enrollmentId}`

**대기번호 관련**

POST : `/api/v1/academies/{academyId}/students/{studentId}/lectures/{lectureId}/waitinglists`

**결제관련 관련 엔드포인트 모두**

**할인정책 관련**

POST : `/api/v1/academies/{academyId}discounts/check`

POST : `/api/v1/academies/{academyId}discounts`

DELETE : `/api/v1/academies/{academyId}discounts/{discountId}`

### 4️⃣ ADMIN 가능

**직원관련**

GET : `/api/v1/academies/{academyId}/employees` (원장이 직원 목록 조회하는거)

### 2월 1일 수요일

- 삭제 관계 정리

```groovy
1. 학원 삭제 -> hard delete 가능

===========================
2. 직원 삭제 -> 직원 데이터만 soft delete
	-> 강좌 삭제
	-> 직원 파일 삭제 

2.1 강좌(lecture) -> 수강신청(enrollment) 삭제
	-> 대기번호 삭제

=================

4. 할인 정책 삭제

================

5. 공지 사항 삭제 -> 공지사항 파일 삭제
```

### 2월 2일

- GitActions CI/CD 1차 적용 가능 → Nginx까지 추가해서 테스트해보고 추후 적용
- 학원등록 시, 사업자 등록번호 입력 받아서 검증하는 로직 추가하기
- 다른직원 삭제 기능 (현재 직원 원장 가능 → 원장만 가능)      본인 탈퇴 기능 사라짐
- 원장 등급변경 (사용안할듯) →
- UI용 메서드라고 표시해놓은거 테스트 코드 짜지말기 (나중에 최적화 시킬예정이라)
- 마이페이지 프로필 사진 등록 기능 추가 (오석 인규) → 원장으로 로그인 시 사이드바에 직원 관리 버튼 추가(직원 정보가 있는 리스트와 삭제 버튼이 있음)

<br>



## 🌈 개인별 역할과 현재까지 개발 과정 요약

------

### 권오석

- 초기 ERD 작성 및 수정되는 부분 계속 반영 담당
- 강사, 강사 프로필 테이블 직원 테이블에 통합
- 강좌-직원 연관관계 매핑 및 직원 테이블에 과목 필드 추가
- 할인정책 적용, 등록, 전체 조회, 수강에 적용된 할인정책 단건 조회, 삭제 구현
- 강좌, 수강 등록 요청 부분 수정
- 강좌, 수강, 대기번호, 할인정책 test code 작성
- UI 적용에 따른 직원 프로필 등록 로직 수정
- GitLab →  GitActions CI/CD Migration test 완료 → 곧

<br>

### 윤인규

- 수강 신청 화면 구현
- 할인 정책 화면 구현
- 결제 페이지 화면 구현
- 마이페이지 화면 구현 ( 프로필 사진 업로드 기능 구현 & 비밀번호 변경 기능 구현)
- 직원 관리 페이지 화면 구현
- 학생 상세 정보 조회 기능 구현 + 특이사항 입력 구현
- 구현할 UI 가 많아서 힘들었다.
- 결제 관련 페이지에는 결제 정보 뿐만 아니라 연관관계가 되어있는 강좌, 수강신청 테이블의 데이터도 포함시켜야 해서 어려웠다.

<br>

### 이가현

- 결제 기능 구현을 완료함 ( 결제 요청 시, Toss Pay 창 나타남)
- 결제 성공 시, 응답값 핸들링 및 데이터 저장 성공
- 할인 정책 적용 시, 예외 처리 적용

<br>

### 박태근

개발과정 요약 :

- 도메인별 service 테스트코드 작성 학원 / 학생 / 공지사항 / 직원
- 강사, 직원 테이블 병합으로 인한 스웨거 수정
- Employee 비밀번호 변경하는 기능 추가
- 공지사항 Type 생성 (ANNOUNCEMENT / ADMISSION)

어려웠던 점 :

- Controller 테스트, cookie를 사용하는 인증인가방식을 선택했는데, mockMvc테스트를 진행할때, cookie가 적용되지 않았음
- employee  Service 테스트를 진행할때, EmailUtil을 테스트 하는 방법을 찾지 못했음

<br>

### 최승호

- 비공개 원격저장소를 만들고 서브모듈 테스트를 진행하였고, 로컬환경에서 적용되는 것을 확인
- 서브모듈을 기존의 프로젝트에 적용하려고 시도하였으나, 현재 깃허브에서 작업 후 Git Action Flow(Sync with GitLab)를 통해 깃랩에서 도커이미지가 빌드되도록 CI가 구축되어 있어서 적용 실패
- GitLab Runner에서 GitHub login 후 서브모듈 동기화를 시도하였으나 GitLab Runner에서 로그인한 정보로는 외부 GitHub Private Remote Repository에 접근 불가(GitLab Docker Image session Layerize)
- 특이사항 컨트롤러 테스트코드 작성 시 쿠키가 주입은 되나, 테스트 실행 시 쿠키가 삭제되는 현상으로 컨트롤러 테스트 잠정 중지
- 서비스 테스트코드(특이사항, 부모 정보) 작성 완료
- 작성한 서비스 테스트코드(특이사항, 부모 정보) 관련 테스트 시나리오 노션 작성 예정
- 스프링 이메일 기능 관련 내용 및 서브모듈 적용에 대한 내용 정리 예정

<br>

## 개발 과정에서 나왔던 질문 (최소 200자 이상)

------

### 1. 배포 서버에서 쿠키가 왜 저장이 안될까?

```java
// 기존 코드
        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieName("token");
        cookieGenerator.setCookieHttpOnly(true);
        cookieGenerator.setSecure(true);
        cookieGenerator.addCookie(httpServletResponse, token);
        cookieGenerator.setCookieMaxAge(60 * 60);//1시간
```

기존 코드는 위와 같이 로그인 성공 시, HttpOnly 와 Secure 옵션을 부여해서 저장하도록 했었다.

localhost 로 테스트 할때는 문제가 없지만, ec2 인스턴스 배포 서버로 로그인을 테스트할때는 쿠키가 저장되지 않는 이유는

우리가 사용하는 ec2 인스턴스의 경우 https:// 가 아닌, http:// 이다.

쿠키의 Secure 속성은 `웹브라우저와 웹서버가 HTTPS로 통신하는 경우에만 웹브라우저가 쿠키를 서버로 전송하는 옵션` 이므로, 쿠키 저장이 안되는 것이다.

따라서 `cookieGenerator.setSecure(true);` 라인을 제거하니, 정상적으로 동작하게 되는 것이다.

<br>

## 개발 결과물 공유

------

Github Repository URL : [https://github.com/mutsa-team6/myacademy](http://ec2-3-39-187-138.ap-northeast-2.compute.amazonaws.com:8080/swagger-ui/index.html)

Swagger URL : [http://ec2-3-39-187-138.ap-northeast-2.compute.amazonaws.com:8080/swagger-ui/index.html](http://ec2-3-39-187-138.ap-northeast-2.compute.amazonaws.com:8080/swagger-ui/index.html)

배포 서버 URL : [http://ec2-3-39-187-138.ap-northeast-2.compute.amazonaws.com:8080/](http://ec2-3-39-187-138.ap-northeast-2.compute.amazonaws.com:8080/)

![image-20230120150737040](https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230120150737040.png)
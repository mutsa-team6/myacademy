# [2주차] MyAcademy팀 진행상황 공유

<br>

## 👨‍👩‍👧‍👦팀 구성원, 개인 별 역할

<br>

------

- 윤인규 (PM) : 프로젝트 일정 관리,  개발
- 권오석 (CTO) : ERD 제작, 코드 컨벤션 관리,  개발
- 이가현 (인프라) : GitLab CI/CD 파이프라인 구축, EC2 인스턴스 관리,  개발
- 박태근 (테스트) : 테스트 코드 환경 세팅,  개발
- 최승호 (기획) : 추가 기능 기획, UI 디자인 제안,  개발

## 🕐 팀 내부 회의 진행 회차 및 일자

<br>

------

### 1월 25일 수요일

<br>

- 승호님 → 이메일 인증 (회원가입 · 비밀번호 변경 [비밀번호 자체를 까먹어서 로그인 못하는 경우]) → 이번주 안에 무조건 끝내기 (기능구현까지 금요일안에 무조건 끝내기 못끝내면 주말에도 일하기)

- 태근님 → 채팅 (로그인한 사용자끼리 채팅)

- 학생 테이블에서 → 직원 등록 (토큰) → 토큰으로 직원 어디학원 다니는지 구할 수 있음 → 학생도  어디 학원인지 앎

  ∴ 학생테이블에 학원 id만 추가

- 가현님 → 결제 (무조건 이번주 , 못하면 주말에 일하기) → 가격 → 결제 방식은 크게 상관 없이 이루어지면 굳

- 오석님 → 만든 로직 체크 & 파일 첨부 기능 → 채팅? → 사용자 프로필 사진 & 공지사항(announcement에 첨부파일올리는거처럼)

- 인규님 → 화면 구성

### 화면 구성 진행 사항

<br>

- 학원등록 페이지 → 학원 이름 검색 기능

- 완료

  - 메인페이지, 직원(사용자) 로그인, 회원가입 페이지, 학원 등록 페이지

- 미완료

  - 아이디 찾기, 비밀번호 바꾸기, 마이페이지,
  - 강좌 등록 페이지
  - 수강 신청 페이지 ( 신청페이지에서 신청 버튼은 강좌 인원 찰때까지 활성화되어있고, 다 찼다 → 대기번호 발급 받는 버튼으로 바뀜)
  - 수강 신청 내역이 보이는 페이지
    - 삭제 가능, 삭제 시 해당 강좌의 다음 대기자 수강신청으로 올리고 대기번호 삭제까지 가능
    - 강좌 이름별로 필터링
  - 학생 등록 부모 정보 등록 (if ROLE_USER 면 예외처리)
    - 부모의 전화번호를 입력하세요 → 부모데이터가 있나없나
      - 살아계신 부모님이 db에 아직 없다 → 부모 이름, 유형(임직원이냐 …) , 전화번호, 주소 입력하도록 구현
      - 부모님이 이미 등록되어있다 → 만약에 get 해서 부모 입력 란에 폼에 넣을 수 있는 방법 있으면 페이지 분할 X 근데 잘 안되면 분할
  - 학원에 다니는 학생 목록 볼 수 있는 페이지 (학생 사진 (파일 되면)) → 특이사항 입력할 수 있는  기능 → 학생 수정, 삭제→
  - 공지사항(announcement) → 로그인 하면 메인페이지에 보임 (강사 빼고 등록 가능) → 댓글 기능 X → 수정됨 표시 기능?



### 1월 26일 목요일

<br>


- 야간 멘토님 중간 점검 일정 → 2월 1일 오후 6시
- yaml → 가짜데이터로 넣어놓기 (항목)

→ username 계정(@ 앞 부분), 비밀번호는 노션적어놈 (이메일인증 관련 세팅)

- payment/success 로 성공 다이렉트 보내줌 (19개) → 멘토님께 질문 예정



### 1월 27일 금요일

<br>


- 쿠키에 토큰이 있으므로 포스트맨 사용시 쿠키 추가 + 스웨거 쿠키 설정하는 방법 알아보기 (태근님)

## 🌈 개인별 역할과 현재까지 개발 과정 요약


<br>

------

### 권오석

- 초기 ERD 작성 및 수정되는 부분 계속 반영 담당
- 강사, 강좌 , 기능 부분 수정
- 수강 등록, 수정, 삭제 기능 구현
  - 수강 등록 시 최대 수강정원 넘으면 에러
  - 수강 삭제 시 대기번호 자동으로 수강등록되고, 대기번호 삭제되게끔 구현
- 대기번호 조회, 등록 기능 구현
- S3 버킷 연동을 통해 파일 업로드, 삭제, 다운로드 기능 구현
  - 다중 파일 업로드 가능
- 파일 관련 기능 강사와 일단 연결시킴

### 윤인규

- 메인 UI 화면 구현
- 로그인, 회원가입, 학원 등록 화면 구현해놓은 것에 기능 연결
- 소셜 로그인 기능 구현 ( db에 소셜에서 가져온 실명과 이메일이 동일한 회원이 존재하는 경우 동작)
- 학원 검색 기능 및 UI 적용 ( 회원가입, 로그인 시 사용)
- 아이디 찾기 기능 구현 및 UI 구현
- 비밀번호 찾기 UI 구현 (승호님이 기능 구현 해놓으신 것에 연결)
- jwt 토큰 발급 후, 쿠키에 저장
- 어려웠던 점 : 소셜 인증 이후 핸들링하는 과정이 많이 까다로웠었다.

### 이가현

- 백엔드에서 결제 요청, 검증, 승인과정 코드 작성
- 승인과정 에러 해결 중
- [결제 과정을 팀원과 공유를 위해서 노션 정리](https://www.notion.so/204c1d78b2d04dee9623be05e4648f01)
- 결제 취소 코드 작성 예정
- 어려웠던 점 : 결제 과정 프로세스를 이해하는데 시간이 오래걸렸고, 외부 api와 요청, 응답 받는 과정에서 오류가 많이나서 시간이 오래걸렸습니다. 지금도 승인과정 요청을 보내는 과정에서 요청보내는 url이 문제인지 404 반환으로 와서 좀더 고민해봐야겠다.

### 박태근

개발과정 요약 :

- 엔드포인트 수정으로 인한 전체 수정
  - 학생, 부모, 특이사항, 공지사항
- 학생이 두학원 이상다닐 경우 처리
- 채팅기능 WebSocket + Stomp를 사용한 채팅 구현 포기 RocketChat  unbuntu22.04환경에서 설치하는거 시도중

어려웠던 점 :

- 모든 학원의 학생, 학부모, 특이사항, 공지사항을 같은 db에서 관리를 하다보니, 한 학생이 2가지 학원에 등록할 경우, 기존에 unique 로 사용하려했던 전화번호가 역할을 하지 못하게 되었다. ⇒ 학생, 학부모 필드에 academyId를 생성하여, 전화번호+academyId로 값들을 확인 하게 해 해결 할 수 있었다.
- 채팅 구현중 블로그를 참고해서, WebSocket과 STOMP를 사용해 pub/sub 구조, messageBroker를 이용해 이루어지는 전송방식등에 대해 공부하고, 우리 프로젝트에 적용해보려 했지만, 채팅 내역, 채팅방 들을 db로 저장하는 방법 및 채팅창 화면을 구성하는 프론트같은 경우 프로젝트 기간안에 제작하기 힘들 것 같다고 생각되어, 포기했음..
- Rocketchat 적용 개인 인스턴스에서 unbuntu 환경에  rocketchat을 설치하는 과정을 시도 하는데, 구동이 안됨.

### 최승호

- 스프링에서 제공하는 메일 라이브러리를 사용하여 이메일 전송기능을 구현했다.
- 직원이 비밀번호 찾기 요청시, 임의의 임시 비밀번호를 생성하여 DB에 저장하고 이메일로 전송하도록 했다.
- 민감 정보들이 환경변수로 들어가는 것에 대한 고민사항을 해결하기 위해 Git submodule을 프로젝트에 적용하기 위해 테스트환경에서 테스트하였다.
- 테스트 환경에서는 비공개 레포지토리에 환경변수를 담은 설정파일(yml)을 생성하여 테스트 프로젝트에 적용할 수 있었으나, 로컬환경에서 -Dspring.profiles.active={설정파일의 프로필}을 적용하는 것을 CI/CD과정에 어떻게 적용할 수 있을지에 대한 고민을 마주했다.

## 개발 과정에서 나왔던 질문 (최소 200자 이상)

<br>


------

### 1. 포스트맨 쿠키 설정 방법


<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/Untitled.png" alt="img" style="zoom: 80%;" />

1. 오른쪽 `cookies` 클릭

<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230127102345875.png" alt="image-20230127102345875" style="zoom:50%;" />

2. 쿠키 이름을 `token` 으로 한 뒤, 발급 받은 토큰을 붙여 넣으면 끝

### 2. 템플릿 엔진 오류

```groovy
2023-01-26 02:26:35.748 ERROR 1 --- [nio-8080-exec-6] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is org.thymeleaf.exceptions.TemplateInputException: Error resolving template [/employee/login], template might not exist or might not be accessible by any of the configured Template Resolvers] with root cause

org.thymeleaf.exceptions.TemplateInputException: Error resolving template [/employee/login], template might not exist or might not be accessible by any of the configured Template Resolvers
```

- 위와 같은 에러가 발생해서 html 파일이 연결되지 않았다. 왜 이런 오류가 발생했을까?

→ controller 에서 return 쪽에 “/” 로 템플릿을 연결해주면 (ex. return “/employee/login”)

localhost 에서는 정상 동작하지만, 리눅스로 넘어가면서 문자 인코딩이 다르게 작동해서 에러가 발생할 수 있다.

따라서 앞에 / 를 제거한 return “employee/login” 으로 바꿔주어야한다.

## 개발 결과물 공유

<br>


------

Github Repository URL : https://github.com/mutsa-team6/myacademy

Swagger URL :

[Swagger UI](http://ec2-3-39-195-170.ap-northeast-2.compute.amazonaws.com:8080/swagger-ui/index.html#/)

<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230127102501486.png" alt="image-20230127102501486" style="zoom:80%;" />
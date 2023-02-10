# [4주차] MyAcademy팀 진행상황 공유

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

### 2월 6일

승호님

- 레디스 DB(캐쉬방식) 데이터 저장 및 조회하는 기능 구현 테스트 완료
- redis db(cache)의 특성상 duration이 지나면 key-value pair에서 value가 사라지고 key가 남아있어, 추후 재인증을 시도할 경우 key가 중복되는 경우 발생
- redis template를 이용하여 키도 남지 않고 삭제시킬 수 있는지 테스트하다가 인증번호 구현은 도전과제로 분류

인규님 & 오석님

- 공지사항 게시판 첨부파일 등록

오석

- GitLab → GitActions Migration 테스트 중

<br>



### 2월 9일

- 리드미 작성 시작하기 (항목 정리해보기)

    - 프로젝트 이름 작성

    - 배포 주소 서버

    - 프로젝트 목표 작성

        - 이 프로젝트는 무엇을 위한 것인가
        - 어떤 문제를 해결할 수 있는가
        - 왜 이 프로젝트가 유용한가
        - 어떤 사람들이 이 프로젝트를 사용하면 좋은가
        - 이 프로젝트는 어떻게 작동하는가

    - 프로젝트 개발환경 작성

    - 프로젝트 워크플로우 첨부

    - 프로젝트 erd 첨부

    - 프로젝트에 사용한 기술 작성 및 설명 작성 (서브모듈, oauth, 등등등)

    - 엔드포인트 (스웨거 링크 첨부)

    - 프로젝트 도메인별 기능 작성

        - Academy
        - Announcement
        - Discount
        - Employee
            - 회원가입 기능
                - 학원 대표자명과 회원 가입을 요청한 사용자의 실명이 일치하고 계정명이 `admin` 인 경우 회원 등급 `ROLE_ADMIN` 로 가입
        - Enrollment
        - Lecture
        - Waitinglist
        - Parent
        - Student
        - Uniqueness
        - Payment

### 2월 10일

- 프로젝트 요구사항 정의서 수정 및 점검
- 프로젝트 UI 개발 사항 및 설명서 작성
- 프로젝트 ERD 수정 및 점검
- 에러코드 정리


  <br>



## 🌈 개인별 역할과 현재까지 개발 과정 요약

------

### 권오석

- ERD 변동 사항 지속적인 반영
- 대기번호 삭제 기능 추가, 등록 로직 수정
- 부모, 특이사항, 수강, 대기번호, 할인정책 Service Test 커버리지 100% 완성
- GiActions CI/CD Migration 성공
- NGINX 추가 적용
- docker-compose로 NGINX, MySQL, Redis, Springboot 통합 CI/CD
- SonarQube 적용

<br>



### 윤인규

- 수강신청 취소 페이지 구현
- 회원 가입 시, 이메일 인증 및 강사 마이페이지 출석부 기능 추가
- 수강 신청 대기자 명단 페이지 구현
- 공지사항(게시판) 페이지 구현
- 국세청 API 이용하여 사업자 등록번호로 학원 인증 로직 추가
- 공지사항(게시판) 첨부파일 기능 추가
- Redis를 사용한 리프레시 토큰 기능 구현

<br>



### 이가현

- @RestClientTest 사용해서 외부 api 사용한 service 로직 테스트 코드 작성

<br>



### 박태근

- validate 리팩토링 진행 완료
- AOP 적용 시도
- payment 유저권한 제한 추가
- 테스트코드 assertThat 잘못사용한거 추가

<br>

### 최승호

- 수강 등록/취소, 결제 완료/취소, 대기신청 완료/취소 시 안내 메일 전송 기능
- 레디스 DB(캐쉬방식) 데이터 저장 및 조회하는 기능 구현 테스트
- Redis db(cache)의 특성상 duration이 지나면 key-value pair에서 value가 사라지고 key가 잔존하기 때문에, 추후 재인증을 시도할 경우 key가 중복되는 경우 발생하여 Redis db(session)으로 데이터 저장 방식을 변경하여 테스트
- Redis template를 이용하여 키도 남지 않고 삭제시킬 수 있는지 테스트하다가 인증번호 구현은 도전과제로 분류

<br>

## 개발 과정에서 나왔던 질문 (최소 200자 이상)

------

### 1. 리프레시 토큰을 사용하는 이유가 뭘까?

Refresh 토큰을 사용하는 이유는, Access Token의 유효기간을 짧게하여 보안도 높이고, 편의성도 챙기는 방법이다.

로그인을 완료하면, **유효기간이 짧은 Access Token**과 **유효기간이 긴 Refresh Token**을 발급해준다.

Access Token은 기존에 사용하던 JWT 토큰이라고 생각하면 되고, Refresh Token은 Access Token이 만료되었을 때, 새로 발급해주는 토큰이라고 생각하면 된다.

### 2. GitActions CI/CD와 NGINX

[GitActions CI/CD(1) - git submodule 포함시키키](https://percyfrank.github.io/infra/Infra01/)

[GitActions CI/CD(2) - submodule 변경 후 메인 프로젝트 반영하기](https://percyfrank.github.io/infra/Infra02/)

[GitActions CI/CD(4) - NGINX 적용](https://percyfrank.github.io/infra/Infra04/)

### 2-1. GitActions 적용 중에 발생했던 에러

[GitActions CI/CD(3) - 마주쳤던 에러들 처리](https://percyfrank.github.io/infra/Infra03/)

### 3. 반복되는 검증코드를 줄이고 싶어요

Spring AOP의 @after 기능을 이용해, 특정 코드가 끝날때, 검증로직이 실행되도록 할 수 있습니다.

AOP 는 관점 지향 프로그래밍으로, 특정 로직의 핵심적, 부가적인 관점으로 보고 그기준으로 모듈화를 하는 것을 의미합니다.

저희의 경우에는 대체로 모든코드에서 사용되는 academy를 검증하는 코드와,  employee를 검증하는코드, employee의 권한을 검증하는 코드가 해당한는 관심사가 될 수 있습니다.

<br>

## 개발 결과물 공유

------

Github Repository URL : [https://github.com/mutsa-team6/myacademy](http://ec2-13-209-97-187.ap-northeast-2.compute.amazonaws.com/swagger-ui/index.html)

서비스 URL : [http://ec2-13-209-97-187.ap-northeast-2.compute.amazonaws.com/](http://ec2-13-209-97-187.ap-northeast-2.compute.amazonaws.com/swagger-ui/index.html)

Swagger URL :  [http://ec2-13-209-97-187.ap-northeast-2.compute.amazonaws.com/swagger-ui/index.html](http://ec2-13-209-97-187.ap-northeast-2.compute.amazonaws.com/swagger-ui/index.html)

<p align="center">
<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230210150507445.png" alt="image-20230210150507445" style="zoom: 67%;" />
</p>
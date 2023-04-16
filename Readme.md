
  <div align = "center">

  <h1> 🏫 학원 관리 웹 서비스 MyAcademy </h1>

  </div>

<br>
  <div align = "center">

 <h2> 🎥 시연 영상 </h2>
 <a href="https://www.youtube.com/watch?v=tKeKN3qd58k">
<img  src="https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230216015248482.png" alt="image-20230216015248482" width="640" height="360" />
</a>

</div>

  <br>


## 결과물

- [배포 서버 주소](http://ec2-52-78-184-7.ap-northeast-2.compute.amazonaws.com/)
- [Swagger 주소](http://ec2-52-78-184-7.ap-northeast-2.compute.amazonaws.com/swagger-ui/index.html)
- [요구사항 정의서](https://www.notion.so/5bb77489934c4912b755290672791299)
- [테스트 코드](https://myacademy-test-report.netlify.app/)
- [UI 개발 사항 및 설명](https://www.notion.so/UI-d9f0d36c370d481c8dde19ed3f7ed141)
- [코드 컨벤션](https://www.notion.so/77f0216deb454980ae1a88f056fbe13c)

<br>

## 프로젝트 목표

  <br>

대형학원의 경우 학원관리 프로그램을 이용하는 경우가 많습니다.

그러나, 중 · 소형 학원이나 개인 레슨을 운영하는 경영자분들은 학원 관리 프로그램의 비용 부담 때문에 사용하지 못하는 경우가 많습니다.

**My Academy**는, 그런 부분들에 착안하여 학원 규모와 상관없이 학원의 모든 부분(직원, 학생, 강좌, 결제 관리)에 편의성을 제공하는 프로젝트를 제작하게 되었습니다.

  <br>

### My Academy를 사용하면 얻을 수 있는 기대효과

1. 학원 관리 기능 통합 및 전산화로 인적 오류를 최소화
2. 교육에 대한 비즈니스에 집중할 수 있어 교육 서비스 질적 향상 기대
3. 직원들은 학생 상세 정보를 쉽게 파악 및 특이사항을 작성 · 조회
4. 원장 혹은 직원은, 학원에 근무하는 임직원들에게 필요한 정보를 공유 및 공지
5. 강사는 본인 강의와 관련된 수강생의 정보를 쉽게 파악
6. 원장 혹은 직원은, 간편 결제(QR) 기능 사용 가능
7. 학생들은 이메일로 수강신청 · 결제 정보 확인 가능

  <br>

## 워크 플로우

  <p align = "center">
  <img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230212171146274.png" alt="image-20230212171146274"  width="640" height="360" />
  </p>


  <br>

## ERD ([ERDcloud](https://www.erdcloud.com/d/D4aseYJCw98Parnw8))

  <p align = "center">
  <img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230212171337333.png" alt="image-20230212171337333"  width="640" height="360" />
  </p>


  <br>

## 개발 환경

- **Java 11**
- **Build Tool** : Gradle 7.5.1
- **WAS** : AWS EC2, Docker
- **Web Container Framework** : Springboot 2.7.7
- **Web Server** : Nginx
- **Database** : MySQL 8.0, Redis
- **CI / CD** : GitHub Actions, Docker Hub, Docker Compose, Docker Container
- **IDE** : IntelliJ
- **Library**
  - Spring Boot DevTools
  - Spring Configuration Processor
  - Lombok
  - Spring Web
  - Spring Security
  - Spring OAuth2
  - Spring Data JDBC
  - Spring Data JPA
  - MySQL JDBC Driver
  - Spring Data Redis(Access Driver)
  - Spring Bean Validation

  <br>

## 프로젝트에 적용한 기능

- Swagger for Documentation
- Oauth 2.0 from Google · Naver for Authorization with Spring Security
- Json Web Token for Authentication
- Redis for Managing refresh token
- Git Submodule for Private Environment Variables
- CI on GitHub Actions with Gradle, Docker Hub
- CD on GitHub Actions with Docker Compose
- Nginx for Web server
- Toss Payment API for Payment System
- AWS S3, Spring-MultipartFile for File up/down loading
- Google SMTP, Spring-Mail for Sending mail
- JUnit, Mokito for Unit Test
- JaCoCo for Test Code Coverage
- Thymleaf template, Vue.JS for User Interface

  <br>

<br>



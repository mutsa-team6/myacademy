package com.project.myacademy;

//import com.project.myacademy.domain.academy.AcademyRepository;
//import com.project.myacademy.domain.employee.EmployeeRepository;
//import com.project.myacademy.domain.enrollment.EnrollmentRepository;
//import com.project.myacademy.domain.lecture.LectureRepository;
//import com.project.myacademy.domain.parent.ParentRepository;
//import com.project.myacademy.domain.payment.PaymentRepository;
//import com.project.myacademy.domain.student.StudentRepository;
//import com.project.myacademy.domain.teacher.TeacherRepository;
//import com.project.myacademy.domain.uniqueness.UniquenessRepository;
//import com.project.myacademy.domain.waitinglist.WaitinglistRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//class MyacademyApplicationTests {
//
//	@Autowired
//	AcademyRepository academyRepository;
//	@Autowired
//	EmployeeRepository employeeRepository;
//	@Autowired
//	LectureRepository lectureRepository;
//	@Autowired
//	ParentRepository parentRepository;
//	@Autowired
//	PaymentRepository paymentRepository;
//	@Autowired
//	EnrollmentRepository enrollmentRepository;
//	@Autowired
//	StudentRepository studentRepository;
//	@Autowired
//	TeacherRepository teacherRepository;
//	@Autowired
//	UniquenessRepository uniquenessRepository;
//	@Autowired
//	WaitinglistRepository waitinglistRepository;

//	@Test
//	@DisplayName("가짜 데이터 넣기")
//	@Transactional
//	@Rollback(value = false)
//	void addExampleData() {
//		Academy academy = Academy.builder()
//				.name("Academy name")
//				.businessRegistrationNumber("123-456")
//				.address("Academy address")
//				.owner("Academy owner")
//				.phoneNum("Academy phone number")
//				.build();
//
//		Academy academy2 = Academy.builder()
//				.name("Academy name")
//				.businessRegistrationNumber("123-789")
//				.address("Academy address")
//				.owner("Academy owner")
//				.phoneNum("Academy phone number")
//				.build();
//
//		Employee employee = Employee.builder()
//				.academy(academy)
//				.account("Employee account")
//				.password("Employee password")
//				.address("Employee address")
//				.email("Employee email")
//				.phoneNum("Employee phone number")
//				.name("Employee name")
//				.employeeRole(EmployeeRole.ROLE_USER)
//				.build();
//
//		Teacher teacher = Teacher.builder()
//				.name("Teacher name")
//				.subject("Teacher subject")
//				.employee(employee)
//				.build();
//
//		Lecture lecture = Lecture.builder()
//				.teacher(teacher)
//				.name("lecture name")
//				.LectureDay("Lecture day")
//				.LectureTime("Lecture Time")
//				.price(300000)
//				.maximumCapacity(30)
//				.minimumCapacity(5)
//				.startDate(LocalDate.now())
//				.finishDate(LocalDate.now().plusDays(10))
//				.currentEnrollmentNumber(1)
//				.build();
//
//		Parent parent =Parent.builder()
//				.address("Parent address")
//				.phoneNum("Parent phone number")
//				.parentRecognizedCode(0)
//				.name("Parent name")
//				.build();
//
//		Student student = Student.builder()
//				.school("Student school")
//				.parent(parent)
//				.name("Student name")
//				.email("Student email")
//				.phoneNum("Student phone number")
//				.build();
//
//		Payment payment = Payment.builder()
//				.student(student)
//				.lecture(lecture)
//				.employee(employee)
//				.build();
//
//		Enrollment enrollment = Enrollment.builder()
//				.student(student)
//				.lecture(lecture)
//				.memo("Student Lecture memo")
//				.build();
//
//		Uniqueness uniqueness = Uniqueness.builder()
//				.body("Uniqueness body")
//				.student(student)
//				.build();
//
//		Waitinglist waitingList = Waitinglist.builder()
//				.lecture(lecture)
//				.student(student)
//				.memo("memo")
//				.build();
//
//
//		academyRepository.save(academy);
//		academyRepository.save(academy2);
//		employeeRepository.save(employee);
//		lectureRepository.save(lecture);
//		parentRepository.save(parent);
//		paymentRepository.save(payment);
//		enrollmentRepository.save(enrollment);
//		studentRepository.save(student);
//		teacherRepository.save(teacher);
//		uniquenessRepository.save(uniqueness);
//		waitinglistRepository.save(waitingList);
//	}
//
//}

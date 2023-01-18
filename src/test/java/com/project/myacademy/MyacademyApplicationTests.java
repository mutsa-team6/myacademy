package com.project.myacademy;

import com.project.myacademy.domain.academy.entity.Academy;
import com.project.myacademy.domain.academy.repository.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.lecture.LectureRepository;
import com.project.myacademy.domain.parent.Parent;
import com.project.myacademy.domain.parent.ParentRepository;
import com.project.myacademy.domain.payment.Payment;
import com.project.myacademy.domain.payment.PaymentRepository;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.domain.studentlecture.StudentLecture;
import com.project.myacademy.domain.studentlecture.StudentLectureRepository;
import com.project.myacademy.domain.teacher.Teacher;
import com.project.myacademy.domain.teacher.TeacherRepository;
import com.project.myacademy.domain.uniqueness.Uniqueness;
import com.project.myacademy.domain.uniqueness.UniquenessRepository;
import com.project.myacademy.domain.waitinglist.WaitingList;
import com.project.myacademy.domain.waitinglist.WaitingListRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@SpringBootTest
class MyacademyApplicationTests {

	@Autowired
	AcademyRepository academyRepository;
	@Autowired
	EmployeeRepository employeeRepository;
	@Autowired
	LectureRepository lectureRepository;
	@Autowired
	ParentRepository parentRepository;
	@Autowired
	PaymentRepository paymentRepository;
	@Autowired
	StudentLectureRepository studentLectureRepository;
	@Autowired
	StudentRepository studentRepository;
	@Autowired
	TeacherRepository teacherRepository;
	@Autowired
	UniquenessRepository uniquenessRepository;
	@Autowired
	WaitingListRepository waitingListRepository;

	@Test
	@DisplayName("가짜 데이터 넣기")
	@Transactional
	@Rollback(value = false)
	void addExampleData() {
		Academy academy = Academy.builder()
				.name("Academy name")
				.address("Academy address")
				.owner("Academy owner")
				.phoneNum("Academy phone number")
				.build();

		Employee employee = Employee.builder()
				.academy(academy)
				.account("Employee account")
				.password("Employee password")
				.address("Employee address")
				.email("Employee email")
				.phoneNum("Employee phone number")
				.name("Employee name")
				.employeeRole(EmployeeRole.ROLE_USER)
				.build();

		Teacher teacher = Teacher.builder()
				.name("Teacher name")
				.subject("Teacher subject")
				.employee(employee)
				.build();

		Lecture lecture = Lecture.builder()
				.teacher(teacher)
				.name("lecture name")
				.LectureDay("Lecture day")
				.LectureTime("Lecture Time")
				.price(300000)
				.maximumCapacity(30)
				.minimumCapacity(5)
				.startDate(LocalDate.now())
				.finishDate(LocalDate.now().plusDays(10))
				.build();

		Parent parent =Parent.builder()
				.address("Parent address")
				.phoneNum("Parent phone number")
				.parentRecognizedCode(0)
				.name("Parent name")
				.build();

		Student student = Student.builder()
				.address("Student address")
				.school("Student school")
				.parent(parent)
				.name("Student name")
				.email("Student email")
				.phoneNum("Student phone number")
				.build();

		Payment payment = Payment.builder()
				.student(student)
				.parent(parent)
				.build();

		StudentLecture studentLecture = StudentLecture.builder()
				.student(student)
				.lecture(lecture)
				.memo("Student Lecture memo")
				.build();

		Uniqueness uniqueness = Uniqueness.builder()
				.body("Uniqueness body")
				.student(student)
				.build();

		WaitingList waitingList = WaitingList.builder()
				.lecture(lecture)
				.student(student)
				.build();


		academyRepository.save(academy);
		employeeRepository.save(employee);
		lectureRepository.save(lecture);
		parentRepository.save(parent);
		paymentRepository.save(payment);
		studentLectureRepository.save(studentLecture);
		studentRepository.save(student);
		teacherRepository.save(teacher);
		uniquenessRepository.save(uniqueness);
		waitingListRepository.save(waitingList);
	}

}

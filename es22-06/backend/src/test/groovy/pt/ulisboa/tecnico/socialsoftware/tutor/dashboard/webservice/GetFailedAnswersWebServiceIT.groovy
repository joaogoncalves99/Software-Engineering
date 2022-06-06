package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service.FailedAnswersSpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Teacher


import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService

import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetFailedAnswersWebServiceIT extends FailedAnswersSpockTest {
    @LocalServerPort
    private int port

    def response
    def quiz
    def quizQuestion
    def dashboardDto
    def failedAnswerDto

    def setup() {
        given:
        restClient = new RESTClient("http://localhost:" + port)
        and:
        createExternalCourseAndExecution()
        userRepository.deleteAll()
        and:
        student = new Student(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, false, AuthUser.Type.EXTERNAL)
        student.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        student.addCourse(externalCourseExecution)
        userRepository.save(student)
        and:
        dashboard = new Dashboard(externalCourseExecution, student)
        dashboardRepository.save(dashboard)

        and:
        quiz = createQuiz(1)
        quizQuestion = createQuestion(1, quiz)
        def questionAnswer = answerQuiz(true, false, true, quizQuestion, quiz)
        createFailedAnswer(questionAnswer, LocalDateTime.now())
    
    }

    def "student gets failed answers"() {
        given: 'a student'
        createdUserLogin(USER_1_USERNAME,USER_1_PASSWORD)
        

        when: 'the web service is invoked'
        response = restClient.get(
                path: '/students/failedAnswers/dashboards/' + dashboard.getId(),
                requestContentType: 'application/json'
        )

        then: "the request returns 200"
        response.status == 200
        and: 'it is in the database'
        failedAnswerRepository.findAll().size() == 1

    }

    def "teacher can't get student's failed answers"() {
        given: 'a teacher'
        def teacher = new Teacher(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, false, AuthUser.Type.EXTERNAL)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        userRepository.save(teacher)
        createdUserLogin(USER_2_USERNAME,USER_2_PASSWORD)

        when: 'the web service is invoked'
        response = restClient.get(
                path: '/students/failedAnswers/dashboards/' + dashboard.getId(),
                requestContentType: 'application/json'
        )

        then: "the server understands the request but refuses to authorize it"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: 'database is clean'
        failedAnswerRepository.findAll().size() == 1

        cleanup: 'clean database'
        userRepository.deleteById(teacher.getId())
    }

    def "student can't get another student's failed answers"() {
        given: 'a student'
        def student1 = new Student(USER_3_NAME, USER_3_USERNAME, USER_3_EMAIL, false, AuthUser.Type.EXTERNAL)
        student1.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student1.addCourse(externalCourseExecution)
        userRepository.save(student1)
        createdUserLogin(USER_3_USERNAME,USER_2_PASSWORD)

        when: 'the web service is invoked'
        response = restClient.get(
                path: '/students/failedAnswers/dashboards/' + dashboard.getId(),
                requestContentType: 'application/json'
        )

        then: "the server understands the request but refuses to authorize it"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: 'database is clean'
        failedAnswerRepository.findAll().size() == 1

        cleanup: 'clean database'
        userRepository.deleteById(student1.getId())

    }

    def cleanup() {
        failedAnswerRepository.deleteAll()
        dashboardRepository.deleteAll()
        externalCourseExecution.getUsers().remove(userRepository.findById(student.getId()))
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

}
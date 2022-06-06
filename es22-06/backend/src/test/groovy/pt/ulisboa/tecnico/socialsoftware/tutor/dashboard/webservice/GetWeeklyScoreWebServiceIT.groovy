package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetWeeklyScoreWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def response

    def authUserDto
    def courseExecutionDto
    def dashboardDto

    def setup() {
        given:
        restClient = new RESTClient("http://localhost:" + port)
        and:
        courseExecutionDto = courseService.getDemoCourse()
        authUserDto = authUserService.demoStudentAuth(false).getUser()
        dashboardDto = dashboardService.getDashboard(courseExecutionDto.getCourseExecutionId(), authUserDto.getId())
        weeklyScoreService.updateWeeklyScore(dashboardDto.getId())
    }

    def "demo student gets weekly scores"() {

        given: 'a demon student'
        demoStudentLogin()

        when: 'the web service is invoked'
        response = restClient.get(
                path: '/students/dashboard/weeklyscore/get/' + dashboardDto.getId(),
                requestContentType: 'application/json'
        )

        then: "the request returns 200"
        response.status == 200

        and: "has weekly scores"
        response.data.size() != 0L
        and: "it is the same weeklyScore"
        response.data[0].id != null
        and: "has value"
        response.data[0].numberAnswered == 0
        and: "has value"
        response.data[0].uniquelyAnswered == 0
        and: "has value"
        response.data[0].percentageCorrect ==0

    }

    def "demo teacher does not have access"() {
        given: 'demo teacher'
        demoTeacherLogin()

        when: 'the web service is invoked'
        response = restClient.get(
                path: '/students/dashboard/weeklyscore/get/' + dashboardDto.getId(),
                requestContentType: 'application/json'
        )

        then: "the server understands the request but refuses to authorize it"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

    }

    def "new demo student does not have access"() {

        given: "new demo student"

        def loginResponse = restClient.get(
                path: '/auth/demo/student',
                query: ['createNew': true]
        )
        restClient.headers['Authorization']  = "Bearer " + loginResponse.data.token

        when: 'the web service is invoked'
        response = restClient.get(
                path: '/students/dashboard/weeklyscore/get/' + dashboardDto.getId(),
                requestContentType: 'application/json'
        )

        then: "the server understands the request but refuses to authorize it"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        cleanup:
        weeklyScoreRepository.deleteAll()
        dashboardRepository.deleteAll()

    }

}

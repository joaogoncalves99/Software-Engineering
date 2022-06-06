package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UpdateWeeklyScoreWebServiceIT extends SpockTest {
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
    }

    def "demo student gets its weekly scores"() {


        given: 'a demon student'
        demoStudentLogin()
        and: "weeklyRepository is empty"
        weeklyScoreRepository.count() == 0L

        when: 'the web service is invoked'
        response = restClient.put(
                path: '/students/dashboard/weeklyscore/update/' + dashboardDto.getId(),
                requestContentType: 'application/json'
        )

        then: "the request returns 200"
        response.status == 200

        and: "weeklyRepository is not empty"
        weeklyScoreRepository.findAll().size() == 1

        cleanup:
        weeklyScoreRepository.deleteAll()


    }

    def "demo teacher does not have access"() {


        given: 'a demon student'
        demoTeacherLogin()
        and: "weeklyRepository is empty"
        weeklyScoreRepository.findAll().size() == 0

        when: 'the web service is invoked'
        response = restClient.put(
                path: '/students/dashboard/weeklyscore/update/' + dashboardDto.getId(),
                requestContentType: 'application/json'
        )

        then: "the server understands the request but refuses to authorize it"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        and: "weeklyRepository is not empty"
        weeklyScoreRepository.count() == 0L
    }

    def "student cant update another students failed answers"() {

        given: "new student"

        def loginResponse = restClient.get(
                path: '/auth/demo/student',
                query: ['createNew': true]
        )
        restClient.headers['Authorization']  = "Bearer " + loginResponse.data.token


        and: "weeklyRepository is empty"
        weeklyScoreRepository.findAll().size() == 0

        when: 'the web service is invoked'
        response = restClient.put(
                path: '/students/dashboard/weeklyscore/update/' + dashboardDto.getId(),
                requestContentType: 'application/json'
        )

        then: "the server understands the request but refuses to authorize it"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        and: "weeklyRepository is not empty"
        weeklyScoreRepository.findAll().size() == 0


    }


}
package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import net.bytebuddy.asm.Advice.Local;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.FailedAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.FailedAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.DashboardRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.FailedAnswerRepository;


import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Predicate;


import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class FailedAnswerService {

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private FailedAnswerRepository failedAnswerRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public FailedAnswerDto createFailedAnswer(int dashboardId, int questionAnswerId) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));
        QuestionAnswer questionAnswer = questionAnswerRepository.findById(questionAnswerId)
                .orElseThrow(() -> new TutorException(QUESTION_ANSWER_NOT_FOUND, questionAnswerId));

        FailedAnswer failedAnswer = new FailedAnswer(dashboard, questionAnswer, DateHandler.now());
        
        failedAnswerRepository.save(failedAnswer);

        return new FailedAnswerDto(failedAnswer);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED) 
    public void removeFailedAnswer(Integer failedAnswerId) {
        if (failedAnswerId == null)
            throw new TutorException(FAILED_ANSWER_NOT_FOUND);      

        FailedAnswer failedAnswer = failedAnswerRepository.findById(failedAnswerId)
                .orElseThrow(() -> new TutorException(ErrorMessage.FAILED_ANSWER_NOT_FOUND, failedAnswerId));
        
        failedAnswer.remove();
        failedAnswerRepository.delete(failedAnswer);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<FailedAnswerDto> getFailedAnswers(Integer dashboardId){

        Dashboard dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));

        return dashboard.getFailedAnswers().stream()
        .sorted((c1,c2)-> c2.getId().compareTo(c1.getId()))
        .map(failedAnswer -> new FailedAnswerDto(failedAnswer)).collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateFailedAnswers(int dashboardId, String startDate, String endDate) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime start, end;
        if (startDate == null) start = getLastCheckDate(dashboard, now);
        else start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
        if (endDate == null) end = now;
        else end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);

        List<QuizAnswer> studentAnswers = dashboard.getStudent().getQuizAnswers().stream()
                .filter(quizAnswer -> quizAnswer.getQuiz().getCourseExecution() == dashboard.getCourseExecution()
                        && (quizAnswer.getAnswerDate().isAfter(start) && quizAnswer.getAnswerDate().isBefore(end)))
                .collect(Collectors.toList());
        
        studentAnswers.stream()
                .filter(quizAnswer -> quizAnswer.canResultsBePublic(dashboard.getCourseExecution().getId()))
                .flatMap(quizAnswer -> quizAnswer.getQuestionAnswers().stream())
                .filter(Predicate.not(QuestionAnswer::isCorrect))
                .filter(qa -> dashboard.getFailedAnswers().stream().noneMatch(fa -> Objects.equals(fa.getQuestionAnswer().getId(), qa.getId())))
                .forEach(questionAnswer -> createFailedAnswer(dashboardId, questionAnswer.getId()));

        dashboard.setLastCheckFailedAnswers(studentAnswers.stream()
                .filter(quizAnswer -> !quizAnswer.canResultsBePublic(dashboard.getCourseExecution().getId()))
                .map(QuizAnswer::getCreationDate)
                .sorted()
                .findFirst()
                .map(localDateTime -> localDateTime.minusSeconds(1))
                .orElse(now));
    }

    private LocalDateTime getLastCheckDate(Dashboard dashboard, LocalDateTime now) {
        LocalDateTime startCheckDate;
        if (dashboard.getLastCheckFailedAnswers() == null) {
            startCheckDate = dashboard.getStudent().getQuizAnswers().stream()
                    .filter(quizAnswer -> quizAnswer.getQuiz().getCourseExecution() == dashboard.getCourseExecution())
                    .map(QuizAnswer::getCreationDate)
                    .sorted()
                    .findFirst()
                    .map(localDateTime -> localDateTime.minusSeconds(1))
                    .orElse(now);
        } else {
            startCheckDate = dashboard.getLastCheckFailedAnswers();
        }

        return startCheckDate;
    }
}
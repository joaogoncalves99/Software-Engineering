package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.api;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.WeeklyScoreService;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.WeeklyScoreDto;

import java.security.Principal;
import java.util.List;

@RestController
public class WeeklyScoreController {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyScoreController.class);

    @Autowired
    private WeeklyScoreService weeklyScoreService;

    WeeklyScoreController(WeeklyScoreService weeklyScoreService) {
        this.weeklyScoreService = weeklyScoreService;
    }

    @GetMapping("/students/dashboard/weeklyscore/get/{dashboardId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#dashboardId, 'DASHBOARD.ACCESS')")
    public List<WeeklyScoreDto> getWeeklyScores(@PathVariable int dashboardId) {
        List<WeeklyScoreDto> list = this.weeklyScoreService.getWeeklyScores(dashboardId);
        return list;
    }

    @DeleteMapping("/students/weeklyScore/detele/{weeklyScoredId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#weeklyScoredId, 'WEEKLYSCORE.ACCESS')")
    public void deleteWeeklyScore(@PathVariable int weeklyScoredId) {
        this.weeklyScoreService.removeWeeklyScore(weeklyScoredId);
    }

    @PutMapping("/students/dashboard/weeklyscore/update/{dashboardId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#dashboardId, 'DASHBOARD.ACCESS')")
    public void updateWeeklyScores(@PathVariable int dashboardId) {
        this.weeklyScoreService.updateWeeklyScore(dashboardId);

    }
    
}



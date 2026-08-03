package com.telusko.aigeminiapp.web.dto;

import java.util.List;

public record StudyPlan(
        String goal,
        String recommendedCourseId,
        int totalWeeks,
        List<Week> schedule
) {

    public record Week(int week, String focus, List<String> topics, String practiceTask) {
    }
}
